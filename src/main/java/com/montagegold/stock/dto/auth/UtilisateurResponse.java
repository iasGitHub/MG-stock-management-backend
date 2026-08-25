package com.montagegold.stock.dto.auth;

import com.montagegold.stock.enums.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UtilisateurResponse {

    private Long id;
    private String username;
    private String nomComplet;
    private Role role;
    private boolean actif;
}
