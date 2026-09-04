package com.montagegold.stock.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "application.invoice-import")
@Getter
@Setter
public class InvoiceImportProperties {

    private boolean enabled = true;

    private Ollama ollama = new Ollama();

    @Getter
    @Setter
    public static class Ollama {

        private String baseUrl = "http://localhost:11434";

        private String model = "qwen2.5:7b";

        private int timeoutSeconds = 120;
    }
}
