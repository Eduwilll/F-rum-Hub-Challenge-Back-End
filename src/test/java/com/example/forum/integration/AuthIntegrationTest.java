package com.example.forum.integration;

import com.example.forum.domain.Profile;
import com.example.forum.dto.AuthResponse;
import com.example.forum.dto.LoginRequest;
import com.example.forum.dto.RegisterRequest;
import com.example.forum.repository.ProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@Testcontainers
@Import(IntegrationTestConfig.class)
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProfileRepository profileRepository;

    @BeforeEach
    void setUp() {
        // Create default profiles if they don't exist
        if (profileRepository.findByNome("USUARIO").isEmpty()) {
            profileRepository.save(new Profile("USUARIO"));
        }
        if (profileRepository.findByNome("MODERADOR").isEmpty()) {
            profileRepository.save(new Profile("MODERADOR"));
        }
        if (profileRepository.findByNome("ADMIN").isEmpty()) {
            profileRepository.save(new Profile("ADMIN"));
        }
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest("João Silva", "joao@email.com", "senha123");

        // When & Then
        MvcResult result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andExpect(jsonPath("$.perfis").isArray())
                .andReturn();

        // Verify JWT token is valid
        String responseBody = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);
        assertNotNull(authResponse.token());
        assertFalse(authResponse.token().isEmpty());
        assertTrue(authResponse.perfis().contains("USUARIO"));
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        // Given - Register first user
        RegisterRequest firstRequest = new RegisterRequest("João Silva", "joao@email.com", "senha123");
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // When - Try to register with same email
        RegisterRequest duplicateRequest = new RegisterRequest("Maria Silva", "joao@email.com", "senha456");

        // Then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email joao@email.com já está em uso"));
    }

    @Test
    void shouldReturnBadRequestForInvalidRegistrationData() throws Exception {
        // Given
        RegisterRequest invalidRequest = new RegisterRequest("", "invalid-email", "123");

        // When & Then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[*].field").exists());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // Given - Register user first
        RegisterRequest registerRequest = new RegisterRequest("João Silva", "joao@email.com", "senha123");
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // When - Login with correct credentials
        LoginRequest loginRequest = new LoginRequest("joao@email.com", "senha123");

        // Then
        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andReturn();

        // Verify JWT token is valid
        String responseBody = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);
        assertNotNull(authResponse.token());
        assertFalse(authResponse.token().isEmpty());
    }

    @Test
    void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        // Given - Register user first
        RegisterRequest registerRequest = new RegisterRequest("João Silva", "joao@email.com", "senha123");
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // When - Login with wrong password
        LoginRequest loginRequest = new LoginRequest("joao@email.com", "wrongPassword");

        // Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email ou senha incorretos"));
    }

    @Test
    void shouldReturnUnauthorizedForNonExistentUser() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("nonexistent@email.com", "senha123");

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnBadRequestForInvalidLoginData() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest("", "");

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }
}