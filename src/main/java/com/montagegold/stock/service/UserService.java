package com.montagegold.stock.service;

import com.montagegold.stock.dto.auth.UserRequest;
import com.montagegold.stock.dto.auth.UserResponse;
import com.montagegold.stock.entity.User;
import com.montagegold.stock.exception.BusinessException;
import com.montagegold.stock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(
                        "User not found: " + username, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("This username already exists", HttpStatus.CONFLICT);
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
    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "User not found (id=" + id + ")", HttpStatus.NOT_FOUND));

        userRepository.findByUsername(request.getUsername())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new BusinessException("This username already exists", HttpStatus.CONFLICT);
                });

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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "User not found (id=" + id + ")", HttpStatus.NOT_FOUND));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException("User not found (id=" + id + ")", HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
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
