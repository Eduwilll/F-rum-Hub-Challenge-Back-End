package com.example.forum.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void shouldCreateResponseWithCorrectProperties() {
        // Given
        Profile userProfile = new Profile("USUARIO");
        User author = new User("João Silva", "joao@email.com", "senha123", Set.of(userProfile));
        User topicAuthor = new User("Maria Silva", "maria@email.com", "senha123", Set.of(userProfile));
        Course course = new Course("Spring Boot", "Backend");
        Topic topic = new Topic("Título", "Mensagem", topicAuthor, course);
        
        // When
        Response response = new Response("Esta é minha resposta", topic, author);
        
        // Then
        assertEquals("Esta é minha resposta", response.getMensagem());
        assertEquals(topic, response.getTopico());
        assertEquals(author, response.getAutor());
        assertFalse(response.getSolucao());
        assertNotNull(response.getDataCriacao());
    }

    @Test
    void shouldMarkAsSolutionCorrectly() {
        // Given
        Profile userProfile = new Profile("USUARIO");
        User author = new User("João Silva", "joao@email.com", "senha123", Set.of(userProfile));
        User topicAuthor = new User("Maria Silva", "maria@email.com", "senha123", Set.of(userProfile));
        Course course = new Course("Spring Boot", "Backend");
        Topic topic = new Topic("Título", "Mensagem", topicAuthor, course);
        Response response = new Response("Esta é minha resposta", topic, author);
        
        // When
        response.markAsSolution();
        
        // Then
        assertTrue(response.getSolucao());
    }

    @Test
    void shouldUnmarkAsSolutionCorrectly() {
        // Given
        Profile userProfile = new Profile("USUARIO");
        User author = new User("João Silva", "joao@email.com", "senha123", Set.of(userProfile));
        User topicAuthor = new User("Maria Silva", "maria@email.com", "senha123", Set.of(userProfile));
        Course course = new Course("Spring Boot", "Backend");
        Topic topic = new Topic("Título", "Mensagem", topicAuthor, course);
        Response response = new Response("Esta é minha resposta", topic, author);
        response.markAsSolution();
        
        // When
        response.unmarkAsSolution();
        
        // Then
        assertFalse(response.getSolucao());
    }

    @Test
    void shouldCheckAuthorCorrectly() {
        // Given
        Profile userProfile = new Profile("USUARIO");
        User author = new User("João Silva", "joao@email.com", "senha123", Set.of(userProfile));
        User otherUser = new User("Maria Silva", "maria@email.com", "senha123", Set.of(userProfile));
        User topicAuthor = new User("Pedro Silva", "pedro@email.com", "senha123", Set.of(userProfile));
        Course course = new Course("Spring Boot", "Backend");
        Topic topic = new Topic("Título", "Mensagem", topicAuthor, course);
        Response response = new Response("Esta é minha resposta", topic, author);
        
        // When & Then
        assertTrue(response.isAuthor(author));
        assertFalse(response.isAuthor(otherUser));
    }
}