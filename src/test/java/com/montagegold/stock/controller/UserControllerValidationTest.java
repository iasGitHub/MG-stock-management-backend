package com.montagegold.stock.controller;

import com.montagegold.stock.dto.auth.UserUpdateRequest;
import com.montagegold.stock.exception.GlobalExceptionHandler;
import com.montagegold.stock.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Validation de la couche REST :
 * - la mise a jour sans mot de passe DOIT passer (regression P0-1 : l'edition etait impossible),
 * - la creation sans mot de passe (ou trop court) DOIT etre refusee en 400 avec l'erreur de champ.
 */
class UserControllerValidationTest {

    private MockMvc mockMvc;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void updateWithoutPasswordIsAccepted() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","fullName":"Administrator","role":"ADMIN","active":true}
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<UserUpdateRequest> captor = ArgumentCaptor.forClass(UserUpdateRequest.class);
        verify(userService).update(eq(1L), captor.capture());
        assertThat(captor.getValue().getPassword()).isNull();
    }

    @Test
    void updateWithShortPasswordIsRejected() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"abc","fullName":"Administrator","role":"ADMIN","active":true}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void createWithoutPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"bob","fullName":"Bob","role":"MANAGEMENT","active":true}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void createWithShortPasswordIsRejected() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"bob","password":"abc","fullName":"Bob","role":"MANAGEMENT","active":true}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void createWithValidPayloadIsAccepted() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"bob","password":"secret1","fullName":"Bob","role":"MANAGEMENT","active":true}
                                """))
                .andExpect(status().isOk());

        verify(userService).create(org.mockito.ArgumentMatchers.any());
    }
}
