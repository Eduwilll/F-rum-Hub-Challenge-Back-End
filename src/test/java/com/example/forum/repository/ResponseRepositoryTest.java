package com.example.forum.repository;

import com.example.forum.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ResponseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ResponseRepository responseRepository;

    @Test
    void shouldFindResponsesByTopicoOrderedByDataCriacaoAsc() {
        // Given
        Profile userProfile = createAndPersistProfile("USUARIO");
        User author = createAndPersistUser("João Silva", "joao@email.com", userProfile);
        User topicAuthor = createAndPersistUser("Maria Silva", "maria@email.com", userProfile);
        Course course = createAndPersistCourse("Spring Boot", "Backend");
        Topic topic = createAndPersistTopic("Título", "Mensagem", topicAuthor, course);
        
        Response response1 = createAndPersistResponse("Primeira resposta", topic, author);
        Response response2 = createAndPersistResponse("Segunda resposta", topic, author);
        
        // When
        List<Response> responses = responseRepository.findByTopicoOrderByDataCriacaoAsc(topic);
        
        // Then
        assertEquals(2, responses.size());
        // First response should come first (older)
        assertTrue(responses.get(0).getDataCriacao()
                .isBefore(responses.get(1).getDataCriacao()) ||
                responses.get(0).getDataCriacao()
                .equals(responses.get(1).getDataCriacao()));
    }

    @Test
    void shouldFindSolutionResponseByTopico() {
        // Given
        Profile userProfile = createAndPersistProfile("USUARIO");
        User author = createAndPersistUser("João Silva", "joao@email.com", userProfile);
        User topicAuthor = createAndPersistUser("Maria Silva", "maria@email.com", userProfile);
        Course course = createAndPersistCourse("Spring Boot", "Backend");
        Topic topic = createAndPersistTopic("Título", "Mensagem", topicAuthor, course);
        
        Response normalResponse = createAndPersistResponse("Resposta normal", topic, author);
        Response solutionResponse = createAndPersistResponse("Resposta solução", topic, author);
        solutionResponse.markAsSolution();
        entityManager.persistAndFlush(solutionResponse);
        
        // When
        Optional<Response> result = responseRepository.findByTopicoAndSolucaoTrue(topic);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(solutionResponse.getId(), result.get().getId());
        assertTrue(result.get().getSolucao());
    }

    @Test
    void shouldReturnEmptyWhenNoSolutionExists() {
        // Given
        Profile userProfile = createAndPersistProfile("USUARIO");
        User author = createAndPersistUser("João Silva", "joao@email.com", userProfile);
        User topicAuthor = createAndPersistUser("Maria Silva", "maria@email.com", userProfile);
        Course course = createAndPersistCourse("Spring Boot", "Backend");
        Topic topic = createAndPersistTopic("Título", "Mensagem", topicAuthor, course);
        
        createAndPersistResponse("Resposta normal", topic, author);
        
        // When
        Optional<Response> result = responseRepository.findByTopicoAndSolucaoTrue(topic);
        
        // Then
        assertFalse(result.isPresent());
    }

    private Profile createAndPersistProfile(String nome) {
        Profile profile = new Profile(nome);
        return entityManager.persistAndFlush(profile);
    }

    private User createAndPersistUser(String nome, String email, Profile profile) {
        User user = new User(nome, email, "senha123", Set.of(profile));
        return entityManager.persistAndFlush(user);
    }

    private Course createAndPersistCourse(String nome, String categoria) {
        Course course = new Course(nome, categoria);
        return entityManager.persistAndFlush(course);
    }

    private Topic createAndPersistTopic(String titulo, String mensagem, User author, Course course) {
        Topic topic = new Topic(titulo, mensagem, author, course);
        return entityManager.persistAndFlush(topic);
    }

    private Response createAndPersistResponse(String mensagem, Topic topic, User author) {
        Response response = new Response(mensagem, topic, author);
        return entityManager.persistAndFlush(response);
    }
}