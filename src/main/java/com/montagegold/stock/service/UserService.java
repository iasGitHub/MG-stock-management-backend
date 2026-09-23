package com.montagegold.stock.service;

import com.montagegold.stock.dto.auth.UserRequest;
import com.montagegold.stock.dto.auth.UserResponse;
import com.montagegold.stock.dto.auth.UserUpdateRequest;
import com.montagegold.stock.entity.User;
import com.montagegold.stock.enums.Role;
import com.montagegold.stock.exception.BusinessException;
import com.montagegold.stock.repository.StockMovementRepository;
import com.montagegold.stock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StockMovementRepository stockMovementRepository;

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(
                        "Utilisateur introuvable : " + username, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Ce nom d'utilisateur existe déjà", HttpStatus.CONFLICT);
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole())
                .active(request.isActive())
                .build();
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = getById(id);

        userRepository.findByUsername(request.getUsername())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new BusinessException("Ce nom d'utilisateur existe déjà", HttpStatus.CONFLICT);
                });

        // Interdiction de se desactiver soi-meme ni de retirer le dernier administrateur actif.
        boolean selfDeactivation = !request.isActive() && user.isActive()
                && user.getId().equals(currentUserId());
        if (selfDeactivation) {
            throw new BusinessException(
                    "Vous ne pouvez pas désactiver votre propre compte", HttpStatus.BAD_REQUEST);
        }

        boolean losesActiveAdmin = user.getRole() == Role.ADMIN && user.isActive()
                && (request.getRole() != Role.ADMIN || !request.isActive());
        if (losesActiveAdmin && remainingActiveAdmins(user) == 0) {
            throw new BusinessException(
                    "Au moins un compte administrateur doit rester actif", HttpStatus.BAD_REQUEST);
        }

        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());
        user.setActive(request.isActive());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setMustChangePassword(true);
        }
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void toggleActive(Long id) {
        User user = getById(id);

        if (user.isActive() && user.getId().equals(currentUserId())) {
            throw new BusinessException(
                    "Vous ne pouvez pas désactiver votre propre compte", HttpStatus.BAD_REQUEST);
        }
        if (user.isActive() && user.getRole() == Role.ADMIN && remainingActiveAdmins(user) == 0) {
            throw new BusinessException(
                    "Au moins un compte administrateur doit rester actif", HttpStatus.BAD_REQUEST);
        }

        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = getById(id);

        if (user.getId().equals(currentUserId())) {
            throw new BusinessException(
                    "Vous ne pouvez pas supprimer votre propre compte", HttpStatus.BAD_REQUEST);
        }
        if (stockMovementRepository.existsByUserId(id)) {
            throw new BusinessException(
                    "Impossible de supprimer cet utilisateur : il a saisi des mouvements de stock",
                    HttpStatus.CONFLICT);
        }
        if (user.isActive() && user.getRole() == Role.ADMIN && remainingActiveAdmins(user) == 0) {
            throw new BusinessException(
                    "Au moins un compte administrateur doit rester actif", HttpStatus.BAD_REQUEST);
        }
        userRepository.deleteById(id);
    }

    /** Nombre d'administrateurs actifs autres que le compte passe en parametre. */
    private long remainingActiveAdmins(User user) {
        long activeAdmins = userRepository.countByRoleAndActive(Role.ADMIN, true);
        boolean counted = user.getRole() == Role.ADMIN && user.isActive();
        return activeAdmins - (counted ? 1 : 0);
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName())
                .map(User::getId)
                .orElse(null);
    }

    private User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Utilisateur introuvable (id=" + id + ")", HttpStatus.NOT_FOUND));
    }

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .fullName(u.getFullName())
                .role(u.getRole())
                .active(u.isActive())
                .build();
    }
}
