package com.montagegold.stock.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessExceptionKeepsItsStatusAndMessage() {
        BusinessException ex = new BusinessException("Product not found (id=5)", HttpStatus.NOT_FOUND);

        ResponseEntity<Map<String, Object>> response = handler.handleBusinessException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("status", 404)
                .containsEntry("message", "Product not found (id=5)");
    }

    @Test
    void unexpectedExceptionDoesNotLeakInternalDetails() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleUnexpected(new IllegalStateException("boom: secret table name"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("status", 500);
        assertThat(String.valueOf(response.getBody().get("message")))
                .doesNotContain("boom")
                .doesNotContain("secret table name");
    }

    @Test
    void accessDeniedIsRethrownSoSecurityCanAnswer403() {
        AccessDeniedException ex = new AccessDeniedException("denied");

        assertThatThrownBy(() -> handler.handleUnexpected(ex))
                .isSameAs(ex);
    }

    @Test
    void standardMvcErrorsKeepTheirOriginalStatus() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleUnexpected(new HttpRequestMethodNotSupportedException("POST"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody()).containsEntry("status", 405);
    }

    @Test
    void dataIntegrityViolationBecomesAConflict() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleDataIntegrity(new DataIntegrityViolationException("duplicate key"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("status", 409);
    }

    @Test
    void malformedBodyBecomesABadRequestWithoutInternals() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleMalformedBody(new HttpMessageNotReadableException("JSON parse error: x"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(String.valueOf(response.getBody().get("message")))
                .doesNotContain("JSON parse error");
    }
}
