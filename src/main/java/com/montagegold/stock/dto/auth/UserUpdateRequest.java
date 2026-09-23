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

    @NotBlank(message = "Le nom d'utilisateur est requis")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    private String username;

    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    @NotBlank(message = "Le nom complet est requis")
    private String fullName;

    @NotNull(message = "Le rôle est requis")
    private Role role;

    private boolean active = true;
}
