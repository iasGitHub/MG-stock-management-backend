package com.montagegold.stock.service;

import com.montagegold.stock.config.InvoiceImportProperties;
import com.montagegold.stock.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OllamaClient {

    private static final String SYSTEM_PROMPT = """
            You are a global invoice parser. Extract invoice data from raw text.
            The invoice may be written in any language (French, Arabic, Chinese, English, etc.).
            Always respond in English field names, but keep product names and supplier names exactly
            as they appear in the source (do NOT translate them).
            Respond with a SINGLE valid JSON object (no markdown, no code fences, no comments) matching exactly:
            {
              "supplier_nif": "string or null",
              "supplier_name": "string or null",
              "supplier_phone": "string or null",
              "supplier_address": "string or null",
              "invoice_number": "string or null",
              "invoice_date": "string in yyyy-MM-dd or null",
              "items": [
                {
                  "product_name": "string",
                  "quantity": number,
                  "unit_price": number,
                  "category": "string or null"
                }
              ]
            }
            Rules:
            - Items = the invoice line items only (product/service name, quantity, unit price).
            - unit_price is the price of ONE unit, in the invoice currency. Do not include the total line amount.
            - quantity must be a positive integer if available, otherwise 1.
            - If a field is not present, use null.
            - Return ONLY the JSON object, nothing else.
            """;

    private final InvoiceImportProperties properties;

    public String chat(String userMessage) {
        if (!properties.isEnabled()) {
            throw new BusinessException(
                    "PDF invoice import is disabled. Set INVOICE_IMPORT_ENABLED=true to enable it.",
                    HttpStatus.BAD_REQUEST);
        }

        String body = """
                {
                  "model": "%s",
                  "stream": false,
                  "messages": [
                    {"role": "system", "content": %s},
                    {"role": "user", "content": %s}
                  ]
                }
                """.formatted(properties.getOllama().getModel(), jsonQuote(SYSTEM_PROMPT), jsonQuote(userMessage));

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(properties.getOllama().getBaseUrl() + "/api/chat"))
                .timeout(Duration.ofSeconds(properties.getOllama().getTimeoutSeconds()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new BusinessException("Ollama responded with HTTP " + response.statusCode()
                        + ": " + abbreviate(response.body(), 300), HttpStatus.BAD_GATEWAY);
            }
            return extractContent(response.body());
        } catch (ConnectException e) {
            throw new BusinessException("Cannot reach Ollama at "
                    + properties.getOllama().getBaseUrl()
                    + ". Is Ollama running? (" + properties.getOllama().getModel() + ")",
                    HttpStatus.SERVICE_UNAVAILABLE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Ollama call interrupted", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (IOException e) {
            throw new BusinessException("Error while calling Ollama: " + e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private String extractContent(String jsonResponse) {
        // Simple extraction of the assistant message content from Ollama's /api/chat response.
        // {"model":..., "message":{"role":"assistant","content":"..."}, ...}
        int idx = jsonResponse.indexOf("\"content\"");
        if (idx < 0) {
            return jsonResponse;
        }
        int colon = jsonResponse.indexOf(':', idx + 1);
        int start = jsonResponse.indexOf('"', colon + 1);
        if (start < 0) {
            return jsonResponse;
        }
        int end = start + 1;
        while (end < jsonResponse.length()) {
            char c = jsonResponse.charAt(end);
            if (c == '\\') {
                end += 2;
                continue;
            }
            if (c == '"') {
                break;
            }
            end++;
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(jsonResponse.substring(start, end + 1), String.class);
        } catch (Exception e) {
            return jsonResponse.substring(start, end);
        }
    }

    private String jsonQuote(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "") + "\"";
    }

    private String abbreviate(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max) + "...";
    }
}
