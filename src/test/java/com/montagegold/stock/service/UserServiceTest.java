package com.montagegold.stock.service;

import com.montagegold.stock.dto.auth.UserRequest;
import com.montagegold.stock.dto.auth.UserResponse;
import com.montagegold.stock.dto.auth.UserUpdateRequest;
import com.montagegold.stock.entity.User;
import com.montagegold.stock.enums.Role;
import com.montagegold.stock.exception.BusinessException;
import com.montagegold.stock.repository.StockMovementRepository;
import com.montagegold.stock.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private User user(long id, String username, Role role, boolean active) {
        return User.builder()
                .id(id)
                .username(username)
                .password("encoded")
                .fullName("Full Name")
                .role(role)
                .active(active)
                .mustChangePassword(false)
                .build();
    }

    private void authenticateAs(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, "n/a"));
    }

    @Test
    void updateWithoutPasswordKeepsCurrentPassword() {
        User admin = user(1L, "admin", Role.ADMIN, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("admin");
        request.setFullName("Administrator");
        request.setRole(Role.ADMIN);
        request.setActive(true);
        // password absent : doit conserver le mot de passe actuel (regression P0-1)

        userService.update(1L, request);

        verify(passwordEncoder, never()).encode(anyString());
        assertThat(admin.getPassword()).isEqualTo("encoded");
        assertThat(admin.isMustChangePassword()).isFalse();
    }

    @Test
    void updateWithPasswordEncodesAndForcesChange() {
        User admin = user(1L, "admin", Role.ADMIN, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("newpass123")).thenReturn("encoded-new");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("admin");
        request.setFullName("Administrator");
        request.setRole(Role.ADMIN);
        request.setActive(true);
        request.setPassword("newpass123");

        userService.update(1L, request);

        assertThat(admin.getPassword()).isEqualTo("encoded-new");
        assertThat(admin.isMustChangePassword()).isTrue();
    }

    @Test
    void updateRejectsSelfDeactivation() {
        authenticateAs("admin");
        User admin = user(1L, "admin", Role.ADMIN, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("admin");
        request.setFullName("Administrator");
        request.setRole(Role.ADMIN);
        request.setActive(false);

        assertThatThrownBy(() -> userService.update(1L, request))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void deleteRejectsSelfDeletion() {
        authenticateAs("admin");
        User admin = user(1L, "admin", Role.ADMIN, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> userService.delete(1L))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(userRepository, never()).deleteById(1L);
    }

    @Test
    void deleteRejectsUserWithMovements() {
        User manager = user(2L, "manager", Role.MANAGEMENT, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(stockMovementRepository.existsByUserId(2L)).thenReturn(true);

        assertThatThrownBy(() -> userService.delete(2L))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT));
        verify(userRepository, never()).deleteById(2L);
    }

    @Test
    void deleteRejectsLastActiveAdmin() {
        User admin = user(2L, "admin2", Role.ADMIN, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(stockMovementRepository.existsByUserId(2L)).thenReturn(false);
        when(userRepository.countByRoleAndActive(Role.ADMIN, true)).thenReturn(1L);

        assertThatThrownBy(() -> userService.delete(2L))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(userRepository, never()).deleteById(2L);
    }

    @Test
    void deleteRemovesUserWhenAllowed() {
        User manager = user(2L, "manager", Role.MANAGEMENT, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(stockMovementRepository.existsByUserId(2L)).thenReturn(false);

        userService.delete(2L);

        verify(userRepository).deleteById(2L);
    }

    @Test
    void toggleActiveRejectsSelfDeactivation() {
        authenticateAs("admin");
        User admin = user(1L, "admin", Role.ADMIN, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> userService.toggleActive(1L))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        assertThat(admin.isActive()).isTrue();
    }

    @Test
    void createEncodesPassword() {
        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(passwordEncoder.encode("secret1")).thenReturn("encoded-bob");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserRequest request = new UserRequest();
        request.setUsername("bob");
        request.setPassword("secret1");
        request.setFullName("Bob");
        request.setRole(Role.MANAGEMENT);
        request.setActive(true);

        UserResponse response = userService.create(request);

        assertThat(response.getUsername()).isEqualTo("bob");
        verify(passwordEncoder).encode("secret1");
    }
}
