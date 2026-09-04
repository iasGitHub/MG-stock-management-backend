package com.montagegold.stock.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.montagegold.stock.dto.invoiceimport.InvoiceDraft;
import com.montagegold.stock.dto.invoiceimport.InvoiceItemDraft;
import com.montagegold.stock.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LlmInvoiceParser {

    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;

    /**
     * Parses raw PDF text into an InvoiceDraft using the local Ollama model.
     */
    public InvoiceDraft parse(String fileName, String rawText) {
        if (rawText == null || rawText.isBlank()) {
            throw new BusinessException(fileName + ": empty text, cannot parse", HttpStatus.BAD_REQUEST);
        }

        String trimmed = rawText.trim();
        if (trimmed.length() > 15000) {
            trimmed = trimmed.substring(0, 15000);
        }

        String responseText;
        try {
            responseText = ollamaClient.chat(trimmed);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(fileName + ": LLM error - " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY);
        }

        JsonNode root = extractJson(responseText, fileName);
        InvoiceDraft draft = toDraft(fileName, root);
        if (draft.getItems() == null || draft.getItems().isEmpty()) {
            throw new BusinessException(fileName
                    + ": the model did not find any line items in this invoice", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        return draft;
    }

    private JsonNode extractJson(String text, String fileName) {
        // Strip any markdown code fences the model might have added.
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new BusinessException(fileName + ": the model did not return valid JSON", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        String json = text.substring(start, end + 1);
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new BusinessException(fileName + ": could not parse model JSON - " + e.getMessage(),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private InvoiceDraft toDraft(String fileName, JsonNode root) {
        List<InvoiceItemDraft> items = new ArrayList<>();
        JsonNode itemsNode = root.path("items");
        if (itemsNode.isArray()) {
            for (JsonNode n : itemsNode) {
                if (n == null || n.isNull() || n.isMissingNode()) continue;
                String name = text(n, "product_name");
                Integer qty = intOr(n, "quantity", 1);
                Double price = doubleOr(n, "unit_price", null);
                String category = text(n, "category");
                if (name == null || name.isBlank()) continue;
                items.add(InvoiceItemDraft.builder()
                        .productName(name.trim())
                        .quantity(qty)
                        .unitPrice(price)
                        .category(category)
                        .build());
            }
        }

        return InvoiceDraft.builder()
                .fileName(fileName)
                .supplierNif(text(root, "supplier_nif"))
                .supplierName(text(root, "supplier_name"))
                .supplierPhone(text(root, "supplier_phone"))
                .supplierAddress(text(root, "supplier_address"))
                .invoiceNumber(text(root, "invoice_number"))
                .invoiceDate(text(root, "invoice_date"))
                .items(items)
                .build();
    }

    private String text(JsonNode node, String field) {
        JsonNode n = node.path(field);
        if (n.isNull() || n.isMissingNode()) return null;
        String v = n.asText();
        return v == null || v.isBlank() ? null : v.trim();
    }

    private Integer intOr(JsonNode node, String field, int defaultValue) {
        JsonNode n = node.path(field);
        if (n.isMissingNode() || n.isNull()) return defaultValue;
        if (n.isInt() || n.isLong()) return n.asInt();
        try {
            return (int) Double.parseDouble(n.asText().replace(",", "."));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Double doubleOr(JsonNode node, String field, Double defaultValue) {
        JsonNode n = node.path(field);
        if (n.isMissingNode() || n.isNull()) return defaultValue;
        if (n.isNumber()) return n.asDouble();
        String cleaned = n.asText().replace(" ", "").replace(",", ".");
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
