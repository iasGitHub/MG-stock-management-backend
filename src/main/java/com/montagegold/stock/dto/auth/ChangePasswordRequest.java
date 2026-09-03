package com.montagegold.stock.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "The current password is required")
    private String currentPassword;

    @NotBlank(message = "The new password is required")
    @Size(min = 6, message = "The new password must be at least 6 characters")
    private String newPassword;
}