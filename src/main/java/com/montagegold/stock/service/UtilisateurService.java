package com.montagegold.stock.service;

import com.montagegold.stock.dto.auth.UtilisateurRequest;
import com.montagegold.stock.dto.auth.UtilisateurResponse;
import com.montagegold.stock.entity.Utilisateur;
import com.montagegold.stock.exception.BusinessException;
import com.montagegold.stock.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UtilisateurResponse> findAll() {
        return utilisateurRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public Utilisateur getByUsername(String username) {
        return utilisateurRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(
                        "Utilisateur introuvable : " + username, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public UtilisateurResponse create(UtilisateurRequest request) {
        if (utilisateurRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Ce nom d'utilisateur existe déjà", HttpStatus.CONFLICT);
        }
        Utilisateur utilisateur = Utilisateur.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nomComplet(request.getNomComplet())
                .role(request.getRole())
                .actif(request.isActif())
                .build();
        return toResponse(utilisateurRepository.save(utilisateur));
    }

    @Transactional
    public UtilisateurResponse update(Long id, UtilisateurRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Utilisateur introuvable (id=" + id + ")", HttpStatus.NOT_FOUND));

        utilisateurRepository.findByUsername(request.getUsername())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new BusinessException("Ce nom d'utilisateur existe déjà", HttpStatus.CONFLICT);
                });

        utilisateur.setUsername(request.getUsername());
        utilisateur.setNomComplet(request.getNomComplet());
        utilisateur.setRole(request.getRole());
        utilisateur.setActif(request.isActif());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            utilisateur.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return toResponse(utilisateurRepository.save(utilisateur));
    }

    @Transactional
    public void toggleActif(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Utilisateur introuvable (id=" + id + ")", HttpStatus.NOT_FOUND));
        utilisateur.setActif(!utilisateur.isActif());
        utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void delete(Long id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new BusinessException("Utilisateur introuvable (id=" + id + ")", HttpStatus.NOT_FOUND);
        }
        utilisateurRepository.deleteById(id);
    }

    private UtilisateurResponse toResponse(Utilisateur u) {
        return UtilisateurResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .nomComplet(u.getNomComplet())
                .role(u.getRole())
                .actif(u.isActif())
                .build();
    }
}
