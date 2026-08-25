package com.montagegold.stock.dto.auth;

import com.montagegold.stock.enums.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthResponse {

    private String token;
    private String type;
    private Long id;
    private String username;
    private String nomComplet;
    private Role role;
}
