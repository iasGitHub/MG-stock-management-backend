package com.montagegold.stock.service;

import com.montagegold.stock.dto.auth.AuthResponse;
import com.montagegold.stock.dto.auth.LoginRequest;
import com.montagegold.stock.entity.Utilisateur;
import com.montagegold.stock.repository.UtilisateurRepository;
import com.montagegold.stock.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        Utilisateur utilisateur = utilisateurRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));

        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(utilisateur.getUsername())
                        .password(utilisateur.getPassword())
                        .roles(utilisateur.getRole().name())
                        .build());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(utilisateur.getId())
                .username(utilisateur.getUsername())
                .nomComplet(utilisateur.getNomComplet())
                .role(utilisateur.getRole())
                .build();
    }
}
