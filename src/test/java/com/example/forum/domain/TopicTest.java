package com.example.forum.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TopicTest {

    @Test
    void shouldCreateTopicWithCorrectProperties() {
        // Given
        Profile userProfile = new Profile("USUARIO");
        User author = new User("João Silva", "joao@email.com", "senha123", Set.of(userProfile));
        Course course = new Course("Spring Boot", "Backend");
        
        // When
        Topic topic = new Topic("Título do Tópico", "Mensagem do tópico", author, course);
        
        // Then
        assertEquals("Título do Tópico", topic.getTitulo());
        assertEquals("Mensagem do tópico", topic.getMensagem());
        assertEquals(author, topic.getAutor());
        assertEquals(course, topic.getCurso());
        assertEquals(TopicStatus.OPEN, topic.getStatus());
        assertNotNull(topic.getDataCriacao());
    }

    @Test
    void shouldUpdateContentCorrectly() {
        // Given
        Profile userProfile = new Profile("USUARIO");
        User author = new User("João Silva", "joao@email.com", "senha123", Set.of(userProfile));
        Course course = new Course("Spring Boot", "Backend");
        Topic topic = new Topic("Título Original", "Mensagem original", author, course);
        
        // When
        topic.updateContent("Novo Título", "Nova mensagem");
        
        // Then
        assertEquals("Novo Título", topic.getTitulo());
        assertEquals("Nova mensagem", topic.getMensagem());
    }

    @Test
    void shouldCloseTopicCorrectly() {
        // Given
        Profile userProfile = new Profile("USUARIO");
        User author = new User("João Silva", "joao@email.com", "senha123", Set.of(userProfile));
        Course course = new Course("Spring Boot", "Backend");
        Topic topic = new Topic("Título", "Mensagem", author, course);
        
        // When
        topic.close();
        
        // Then
        assertEquals(TopicStatus.CLOSED, topic.getStatus());
    }

    @Test
    void shouldOpenTopicCorrectly() {
        // Given
        Profile userProfile = new Profile("USUARIO");
        User author = new User("João Silva", "joao@email.com", "senha123", Set.of(userProfile));
        Course course = new Course("Spring Boot", "Backend");
        Topic topic = new Topic("Título", "Mensagem", author, course);
        topic.close();
        
        // When
        topic.open();
        
        // Then
        assertEquals(TopicStatus.OPEN, topic.getStatus());
    }

    @Test
    void shouldCheckAuthorCorrectly() {
        // Given
        Profile userProfile = new Profile("USUARIO");
        User author = new User("João Silva", "joao@email.com", "senha123", Set.of(userProfile));
        User otherUser = new User("Maria Silva", "maria@email.com", "senha123", Set.of(userProfile));
        Course course = new Course("Spring Boot", "Backend");
        Topic topic = new Topic("Título", "Mensagem", author, course);
        
        // When & Then
        assertTrue(topic.isAuthor(author));
        assertFalse(topic.isAuthor(otherUser));
    }
}