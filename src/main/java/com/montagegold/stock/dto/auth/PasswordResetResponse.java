package com.montagegold.stock.dto.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Reponse a une reinitialisation de mot de passe par un administrateur.
 * Le mot de passe temporaire n'est renvoye qu'une seule fois, jamais stocke en clair.
 */
@Getter
@Setter
@Builder
public class PasswordResetResponse {

    private String temporaryPassword;
}
