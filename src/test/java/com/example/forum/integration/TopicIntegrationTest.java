package com.example.forum.integration;

import com.example.forum.domain.Course;
import com.example.forum.domain.Profile;
import com.example.forum.domain.User;
import com.example.forum.dto.AuthResponse;
import com.example.forum.dto.CreateTopicRequest;
import com.example.forum.dto.RegisterRequest;
import com.example.forum.dto.UpdateTopicRequest;
import com.example.forum.repository.CourseRepository;
import com.example.forum.repository.ProfileRepository;
import com.example.forum.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@Testcontainers
@Import(IntegrationTestConfig.class)
@ActiveProfiles("test")
@Transactional
class TopicIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private String moderatorToken;
    private Course course;
    private User user;
    private User moderator;

    @BeforeEach
    void setUp() throws Exception {
        // Create profiles
        Profile userProfile = profileRepository.save(new Profile("USUARIO"));
        Profile moderatorProfile = profileRepository.save(new Profile("MODERADOR"));

        // Create course
        course = courseRepository.save(new Course("Spring Boot", "Backend"));

        // Create and save users
        user = userRepository.save(new User("João Silva", "joao@email.com", 
                passwordEncoder.encode("senha123"), Set.of(userProfile)));
        moderator = userRepository.save(new User("Admin User", "admin@email.com", 
                passwordEncoder.encode("senha123"), Set.of(moderatorProfile)));

        // Get JWT tokens by registering users
        userToken = registerAndGetToken("João User", "joao.user@email.com", "senha123");
        moderatorToken = registerAndGetToken("Admin User", "admin.user@email.com", "senha123");
        
        // Add moderator role to the second user
        User moderatorUser = userRepository.findByEmail("admin.user@email.com").orElseThrow();
        moderatorUser.getPerfis().add(moderatorProfile);
        userRepository.save(moderatorUser);
    }

    private String registerAndGetToken(String nome, String email, String senha) throws Exception {
        RegisterRequest request = new RegisterRequest(nome, email, senha);
        MvcResult result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);
        return authResponse.token();
    }

    @Test
    void shouldCreateTopicSuccessfully() throws Exception {
        // Given
        CreateTopicRequest request = new CreateTopicRequest("Novo Tópico", "Mensagem do tópico", course.getId());

        // When & Then
        mockMvc.perform(post("/topicos")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Novo Tópico"))
                .andExpect(jsonPath("$.mensagem").value("Mensagem do tópico"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.cursoNome").value("Spring Boot"))
                .andExpect(jsonPath("$.autorNome").exists());
    }

    @Test
    void shouldReturnUnauthorizedWhenCreatingTopicWithoutToken() throws Exception {
        // Given
        CreateTopicRequest request = new CreateTopicRequest("Novo Tópico", "Mensagem do tópico", course.getId());

        // When & Then
        mockMvc.perform(post("/topicos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnBadRequestForInvalidTopicData() throws Exception {
        // Given
        CreateTopicRequest request = new CreateTopicRequest("", "", null);

        // When & Then
        mockMvc.perform(post("/topicos")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void shouldReturnConflictForDuplicateTopic() throws Exception {
        // Given - Create first topic
        CreateTopicRequest request = new CreateTopicRequest("Tópico Único", "Mensagem única", course.getId());
        mockMvc.perform(post("/topicos")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // When - Try to create duplicate topic
        CreateTopicRequest duplicateRequest = new CreateTopicRequest("Tópico Único", "Mensagem única", course.getId());

        // Then
        mockMvc.perform(post("/topicos")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Já existe um tópico com o mesmo título e mensagem"));
    }

    @Test
    void shouldListTopicsWithPagination() throws Exception {
        // Given - Create some topics
        CreateTopicRequest request1 = new CreateTopicRequest("Primeiro Tópico", "Primeira mensagem", course.getId());
        CreateTopicRequest request2 = new CreateTopicRequest("Segundo Tópico", "Segunda mensagem", course.getId());

        mockMvc.perform(post("/topicos")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/topicos")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/topicos")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].titulo").exists())
                .andExpect(jsonPath("$.content[1].titulo").exists());
    }

    @Test
    void shouldGetTopicById() throws Exception {
        // Given - Create a topic
        CreateTopicRequest request = new CreateTopicRequest("Tópico Teste", "Mensagem teste", course.getId());
        MvcResult createResult = mockMvc.perform(post("/topicos")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponseBody = createResult.getResponse().getContentAsString();
        Long topicId = objectMapper.readTree(createResponseBody).get("id").asLong();

        // When & Then
        mockMvc.perform(get("/topicos/" + topicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(topicId))
                .andExpect(jsonPath("$.titulo").value("Tópico Teste"))
                .andExpect(jsonPath("$.mensagem").value("Mensagem teste"));
    }

    @Test
    void shouldReturnNotFoundForNonExistentTopic() throws Exception {
        // When & Then
        mockMvc.perform(get("/topicos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tópico com ID 999 não encontrado"));
    }

    @Test
    void shouldUpdateTopicWhenAuthorized() throws Exception {
        // Given - Create a topic
        CreateTopicRequest createRequest = new CreateTopicRequest("Tópico Original", "Mensagem original", course.getId());
        MvcResult createResult = mockMvc.perform(post("/topicos")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponseBody = createResult.getResponse().getContentAsString();
        Long topicId = objectMapper.readTree(createResponseBody).get("id").asLong();

        // When - Update the topic
        UpdateTopicRequest updateRequest = new UpdateTopicRequest("Tópico Atualizado", "Mensagem atualizada");

        // Then
        mockMvc.perform(put("/topicos/" + topicId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Tópico Atualizado"))
                .andExpect(jsonPath("$.mensagem").value("Mensagem atualizada"));
    }

    @Test
    void shouldDeleteTopicWhenAuthorized() throws Exception {
        // Given - Create a topic
        CreateTopicRequest createRequest = new CreateTopicRequest("Tópico para Deletar", "Mensagem para deletar", course.getId());
        MvcResult createResult = mockMvc.perform(post("/topicos")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponseBody = createResult.getResponse().getContentAsString();
        Long topicId = objectMapper.readTree(createResponseBody).get("id").asLong();

        // When & Then
        mockMvc.perform(delete("/topicos/" + topicId)
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        // Verify topic is deleted
        mockMvc.perform(get("/topicos/" + topicId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCloseTopicWhenAuthor() throws Exception {
        // Given - Create a topic
        CreateTopicRequest createRequest = new CreateTopicRequest("Tópico para Fechar", "Mensagem para fechar", course.getId());
        MvcResult createResult = mockMvc.perform(post("/topicos")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponseBody = createResult.getResponse().getContentAsString();
        Long topicId = objectMapper.readTree(createResponseBody).get("id").asLong();

        // When & Then
        mockMvc.perform(put("/topicos/" + topicId + "/close")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void shouldFilterTopicsByStatus() throws Exception {
        // Given - Create topics with different statuses
        CreateTopicRequest request1 = new CreateTopicRequest("Tópico Aberto", "Mensagem aberta", course.getId());
        CreateTopicRequest request2 = new CreateTopicRequest("Tópico para Fechar", "Mensagem para fechar", course.getId());

        mockMvc.perform(post("/topicos")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        MvcResult createResult = mockMvc.perform(post("/topicos")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponseBody = createResult.getResponse().getContentAsString();
        Long topicId = objectMapper.readTree(createResponseBody).get("id").asLong();

        // Close one topic
        mockMvc.perform(put("/topicos/" + topicId + "/close")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // When & Then - Filter by OPEN status
        mockMvc.perform(get("/topicos")
                .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value("OPEN"));

        // Filter by CLOSED status
        mockMvc.perform(get("/topicos")
                .param("status", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value("CLOSED"));
    }
}