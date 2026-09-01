package com.montagegold.stock.dto.auth;

import com.montagegold.stock.enums.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String fullName;
    private Role role;
    private boolean active;
}
