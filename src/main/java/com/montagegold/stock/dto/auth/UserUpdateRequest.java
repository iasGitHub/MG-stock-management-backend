package com.montagegold.stock.dto.auth;

import com.montagegold.stock.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO de mise a jour : le mot de passe devient optionnel.
 * Champ absent/null => le mot de passe actuel est conserve.
 */
@Getter
@Setter
public class UserUpdateRequest {

    @NotBlank(message = "The username is required")
    @Size(min = 3, max = 50, message = "The username must be between 3 and 50 characters")
    private String username;

    @Size(min = 6, message = "The password must contain at least 6 characters")
    private String password;

    @NotBlank(message = "The full name is required")
    private String fullName;

    @NotNull(message = "The role is required")
    private Role role;

    private boolean active = true;
}
