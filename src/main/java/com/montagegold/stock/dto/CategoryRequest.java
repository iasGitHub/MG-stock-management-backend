package com.montagegold.stock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {

    @NotBlank(message = "The category name is required")
    @Size(max = 50, message = "The category name must not exceed 50 characters")
    private String name;
}
