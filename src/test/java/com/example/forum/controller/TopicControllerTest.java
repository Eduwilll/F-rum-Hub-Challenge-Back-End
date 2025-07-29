package com.example.forum.controller;

import com.example.forum.domain.*;
import com.example.forum.dto.CreateTopicRequest;
import com.example.forum.dto.UpdateTopicRequest;
import com.example.forum.service.TopicService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TopicController.class)
class TopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TopicService topicService;

    @Autowired
    private ObjectMapper objectMapper;

    private User author;
    private Course course;
    private Topic topic;

    @BeforeEach
    void setUp() {
        Profile userProfile = new Profile("USUARIO");
        author = new User("João Silva", "joao@email.com", "senha123", Set.of(userProfile));
        course = new Course("Spring Boot", "Backend");
        topic = new Topic("Título", "Mensagem", author, course);
    }

    @Test
    @WithMockUser
    void shouldCreateTopic() throws Exception {
        // Given
        CreateTopicRequest request = new CreateTopicRequest("Novo Título", "Nova mensagem", 1L);
        when(topicService.createTopic(eq(request), any(User.class))).thenReturn(topic);

        // When & Then
        mockMvc.perform(post("/topicos")
                .with(user(author))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Título"))
                .andExpect(jsonPath("$.mensagem").value("Mensagem"))
                .andExpect(jsonPath("$.autorNome").value("João Silva"))
                .andExpect(jsonPath("$.cursoNome").value("Spring Boot"));

        verify(topicService).createTopic(eq(request), any(User.class));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenCreateTopicWithInvalidData() throws Exception {
        // Given
        CreateTopicRequest request = new CreateTopicRequest("", "", null);

        // When & Then
        mockMvc.perform(post("/topicos")
                .with(user(author))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(topicService, never()).createTopic(any(), any());
    }

    @Test
    @WithMockUser
    void shouldListTopics() throws Exception {
        // Given
        Page<Topic> topicsPage = new PageImpl<>(List.of(topic), PageRequest.of(0, 10), 1);
        when(topicService.findAll(any())).thenReturn(topicsPage);

        // When & Then
        mockMvc.perform(get("/topicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].titulo").value("Título"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(topicService).findAll(any());
    }

    @Test
    @WithMockUser
    void shouldListTopicsByStatus() throws Exception {
        // Given
        Page<Topic> topicsPage = new PageImpl<>(List.of(topic), PageRequest.of(0, 10), 1);
        when(topicService.findByStatus(eq(TopicStatus.OPEN), any())).thenReturn(topicsPage);

        // When & Then
        mockMvc.perform(get("/topicos")
                .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(topicService).findByStatus(eq(TopicStatus.OPEN), any());
    }

    @Test
    @WithMockUser
    void shouldGetTopicById() throws Exception {
        // Given
        when(topicService.findById(1L)).thenReturn(topic);

        // When & Then
        mockMvc.perform(get("/topicos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Título"))
                .andExpect(jsonPath("$.mensagem").value("Mensagem"))
                .andExpect(jsonPath("$.autorNome").value("João Silva"));

        verify(topicService).findById(1L);
    }

    @Test
    @WithMockUser
    void shouldUpdateTopic() throws Exception {
        // Given
        UpdateTopicRequest request = new UpdateTopicRequest("Título Atualizado", "Mensagem atualizada");
        Topic updatedTopic = new Topic("Título Atualizado", "Mensagem atualizada", author, course);
        when(topicService.updateTopic(eq(1L), eq(request), any(User.class))).thenReturn(updatedTopic);

        // When & Then
        mockMvc.perform(put("/topicos/1")
                .with(user(author))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Título Atualizado"))
                .andExpect(jsonPath("$.mensagem").value("Mensagem atualizada"));

        verify(topicService).updateTopic(eq(1L), eq(request), any(User.class));
    }

    @Test
    @WithMockUser
    void shouldDeleteTopic() throws Exception {
        // Given
        doNothing().when(topicService).deleteTopic(eq(1L), any(User.class));

        // When & Then
        mockMvc.perform(delete("/topicos/1")
                .with(user(author)))
                .andExpect(status().isNoContent());

        verify(topicService).deleteTopic(eq(1L), any(User.class));
    }

    @Test
    @WithMockUser
    void shouldCloseTopic() throws Exception {
        // Given
        Topic closedTopic = new Topic("Título", "Mensagem", author, course);
        closedTopic.close();
        when(topicService.closeTopic(eq(1L), any(User.class))).thenReturn(closedTopic);

        // When & Then
        mockMvc.perform(put("/topicos/1/close")
                .with(user(author)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        verify(topicService).closeTopic(eq(1L), any(User.class));
    }

    @Test
    @WithMockUser
    void shouldOpenTopic() throws Exception {
        // Given
        when(topicService.openTopic(eq(1L), any(User.class))).thenReturn(topic);

        // When & Then
        mockMvc.perform(put("/topicos/1/open")
                .with(user(author)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(topicService).openTopic(eq(1L), any(User.class));
    }
}