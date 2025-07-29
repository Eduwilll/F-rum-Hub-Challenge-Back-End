package com.example.forum.controller;

import com.example.forum.dto.AuthResponse;
import com.example.forum.dto.LoginRequest;
import com.example.forum.dto.RegisterRequest;
import com.example.forum.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterUser() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("João Silva", "joao@email.com", "senha123");
        AuthResponse response = new AuthResponse("jwt-token", "João Silva", "joao@email.com", Set.of("USUARIO"));
        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andExpect(jsonPath("$.perfis[0]").value("USUARIO"));

        verify(authenticationService).register(any(RegisterRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenRegisterWithInvalidData() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("", "invalid-email", "123");

        // When & Then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).register(any());
    }

    @Test
    void shouldLoginUser() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("joao@email.com", "senha123");
        AuthResponse response = new AuthResponse("jwt-token", "João Silva", "joao@email.com", Set.of("USUARIO"));
        when(authenticationService.login(any(LoginRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));

        verify(authenticationService).login(any(LoginRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenLoginWithInvalidData() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("", "");

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any());
    }
}