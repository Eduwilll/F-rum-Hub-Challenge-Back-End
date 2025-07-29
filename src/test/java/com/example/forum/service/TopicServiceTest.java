package com.example.forum.service;

import com.example.forum.domain.*;
import com.example.forum.dto.CreateTopicRequest;
import com.example.forum.dto.UpdateTopicRequest;
import com.example.forum.exception.CourseNotFoundException;
import com.example.forum.exception.DuplicateTopicException;
import com.example.forum.exception.TopicNotFoundException;
import com.example.forum.exception.UnauthorizedOperationException;
import com.example.forum.repository.CourseRepository;
import com.example.forum.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TopicService topicService;

    private User author;
    private User moderator;
    private Course course;
    private Topic topic;

    @BeforeEach
    void setUp() {
        Profile userProfile = new Profile("USUARIO");
        Profile moderatorProfile = new Profile("MODERADOR");
        
        author = new User("João Silva", "joao@email.com", "senha123", Set.of(userProfile));
        moderator = new User("Admin User", "admin@email.com", "senha123", Set.of(moderatorProfile));
        course = new Course("Spring Boot", "Backend");
        topic = new Topic("Título", "Mensagem", author, course);
    }

    @Test
    void shouldFindAllTopics() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Topic> expectedPage = new PageImpl<>(List.of(topic));
        when(topicRepository.findAllByOrderByDataCriacaoDesc(pageable)).thenReturn(expectedPage);

        // When
        Page<Topic> result = topicService.findAll(pageable);

        // Then
        assertEquals(expectedPage, result);
        verify(topicRepository).findAllByOrderByDataCriacaoDesc(pageable);
    }

    @Test
    void shouldFindTopicsByStatus() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Topic> expectedPage = new PageImpl<>(List.of(topic));
        when(topicRepository.findByStatusOrderByDataCriacaoDesc(TopicStatus.OPEN, pageable))
                .thenReturn(expectedPage);

        // When
        Page<Topic> result = topicService.findByStatus(TopicStatus.OPEN, pageable);

        // Then
        assertEquals(expectedPage, result);
        verify(topicRepository).findByStatusOrderByDataCriacaoDesc(TopicStatus.OPEN, pageable);
    }

    @Test
    void shouldFindTopicById() {
        // Given
        when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));

        // When
        Topic result = topicService.findById(1L);

        // Then
        assertEquals(topic, result);
        verify(topicRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenTopicNotFound() {
        // Given
        when(topicRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TopicNotFoundException.class, () -> topicService.findById(1L));
        verify(topicRepository).findById(1L);
    }

    @Test
    void shouldCreateTopic() {
        // Given
        CreateTopicRequest request = new CreateTopicRequest("Novo Título", "Nova mensagem", 1L);
        when(topicRepository.existsByTituloAndMensagem(request.titulo(), request.mensagem()))
                .thenReturn(false);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(topicRepository.save(any(Topic.class))).thenReturn(topic);

        // When
        Topic result = topicService.createTopic(request, author);

        // Then
        assertEquals(topic, result);
        verify(topicRepository).existsByTituloAndMensagem(request.titulo(), request.mensagem());
        verify(courseRepository).findById(1L);
        verify(topicRepository).save(any(Topic.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateTopic() {
        // Given
        CreateTopicRequest request = new CreateTopicRequest("Título", "Mensagem", 1L);
        when(topicRepository.existsByTituloAndMensagem(request.titulo(), request.mensagem()))
                .thenReturn(true);

        // When & Then
        assertThrows(DuplicateTopicException.class, 
                () -> topicService.createTopic(request, author));
        verify(topicRepository).existsByTituloAndMensagem(request.titulo(), request.mensagem());
        verify(courseRepository, never()).findById(any());
        verify(topicRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCourseNotFound() {
        // Given
        CreateTopicRequest request = new CreateTopicRequest("Título", "Mensagem", 1L);
        when(topicRepository.existsByTituloAndMensagem(request.titulo(), request.mensagem()))
                .thenReturn(false);
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CourseNotFoundException.class, 
                () -> topicService.createTopic(request, author));
        verify(courseRepository).findById(1L);
        verify(topicRepository, never()).save(any());
    }

    @Test
    void shouldUpdateTopicWhenAuthorized() {
        // Given
        UpdateTopicRequest request = new UpdateTopicRequest("Novo Título", "Nova mensagem");
        when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));
        when(topicRepository.existsByTituloAndMensagem(request.titulo(), request.mensagem()))
                .thenReturn(false);
        when(topicRepository.save(topic)).thenReturn(topic);

        // When
        Topic result = topicService.updateTopic(1L, request, author);

        // Then
        assertEquals(topic, result);
        verify(topicRepository).findById(1L);
        verify(topicRepository).existsByTituloAndMensagem(request.titulo(), request.mensagem());
        verify(topicRepository).save(topic);
    }

    @Test
    void shouldThrowExceptionWhenUnauthorizedToUpdate() {
        // Given
        UpdateTopicRequest request = new UpdateTopicRequest("Novo Título", "Nova mensagem");
        User otherUser = new User("Outro User", "outro@email.com", "senha123", Set.of(new Profile("USUARIO")));
        when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));
        when(userService.isUserModerator(otherUser)).thenReturn(false);

        // When & Then
        assertThrows(UnauthorizedOperationException.class, 
                () -> topicService.updateTopic(1L, request, otherUser));
        verify(topicRepository).findById(1L);
        verify(topicRepository, never()).save(any());
    }

    @Test
    void shouldDeleteTopicWhenAuthorized() {
        // Given
        when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));

        // When
        topicService.deleteTopic(1L, author);

        // Then
        verify(topicRepository).findById(1L);
        verify(topicRepository).delete(topic);
    }

    @Test
    void shouldCloseTopicWhenAuthor() {
        // Given
        when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));
        when(topicRepository.save(topic)).thenReturn(topic);

        // When
        Topic result = topicService.closeTopic(1L, author);

        // Then
        assertEquals(TopicStatus.CLOSED, result.getStatus());
        verify(topicRepository).findById(1L);
        verify(topicRepository).save(topic);
    }

    @Test
    void shouldThrowExceptionWhenNonAuthorTriesToClose() {
        // Given
        User otherUser = new User("Outro User", "outro@email.com", "senha123", Set.of(new Profile("USUARIO")));
        when(topicRepository.findById(1L)).thenReturn(Optional.of(topic));

        // When & Then
        assertThrows(UnauthorizedOperationException.class, 
                () -> topicService.closeTopic(1L, otherUser));
        verify(topicRepository).findById(1L);
        verify(topicRepository, never()).save(any());
    }
}