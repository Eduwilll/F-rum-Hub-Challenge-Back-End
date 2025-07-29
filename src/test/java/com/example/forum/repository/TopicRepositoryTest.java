package com.example.forum.repository;

import com.example.forum.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TopicRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TopicRepository topicRepository;

    @Test
    void shouldFindAllTopicsOrderedByDataCriacaoDesc() {
        // Given
        Profile userProfile = createAndPersistProfile("USUARIO");
        User author = createAndPersistUser("João Silva", "joao@email.com", userProfile);
        Course course = createAndPersistCourse("Spring Boot", "Backend");
        
        Topic topic1 = createAndPersistTopic("Primeiro Tópico", "Primeira mensagem", author, course);
        Topic topic2 = createAndPersistTopic("Segundo Tópico", "Segunda mensagem", author, course);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Topic> result = topicRepository.findAllByOrderByDataCriacaoDesc(pageable);
        
        // Then
        assertEquals(2, result.getTotalElements());
        // The second topic should come first (newer)
        assertTrue(result.getContent().get(0).getDataCriacao()
                .isAfter(result.getContent().get(1).getDataCriacao()) ||
                result.getContent().get(0).getDataCriacao()
                .equals(result.getContent().get(1).getDataCriacao()));
    }

    @Test
    void shouldFindTopicsByStatusOrderedByDataCriacaoDesc() {
        // Given
        Profile userProfile = createAndPersistProfile("USUARIO");
        User author = createAndPersistUser("João Silva", "joao@email.com", userProfile);
        Course course = createAndPersistCourse("Spring Boot", "Backend");
        
        Topic openTopic = createAndPersistTopic("Tópico Aberto", "Mensagem aberta", author, course);
        Topic closedTopic = createAndPersistTopic("Tópico Fechado", "Mensagem fechada", author, course);
        closedTopic.close();
        entityManager.persistAndFlush(closedTopic);
        
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<Topic> openTopics = topicRepository.findByStatusOrderByDataCriacaoDesc(TopicStatus.OPEN, pageable);
        Page<Topic> closedTopics = topicRepository.findByStatusOrderByDataCriacaoDesc(TopicStatus.CLOSED, pageable);
        
        // Then
        assertEquals(1, openTopics.getTotalElements());
        assertEquals(1, closedTopics.getTotalElements());
        assertEquals(TopicStatus.OPEN, openTopics.getContent().get(0).getStatus());
        assertEquals(TopicStatus.CLOSED, closedTopics.getContent().get(0).getStatus());
    }

    @Test
    void shouldCheckIfTopicExistsByTituloAndMensagem() {
        // Given
        Profile userProfile = createAndPersistProfile("USUARIO");
        User author = createAndPersistUser("João Silva", "joao@email.com", userProfile);
        Course course = createAndPersistCourse("Spring Boot", "Backend");
        
        createAndPersistTopic("Tópico Único", "Mensagem única", author, course);
        
        // When & Then
        assertTrue(topicRepository.existsByTituloAndMensagem("Tópico Único", "Mensagem única"));
        assertFalse(topicRepository.existsByTituloAndMensagem("Tópico Inexistente", "Mensagem inexistente"));
        assertFalse(topicRepository.existsByTituloAndMensagem("Tópico Único", "Mensagem diferente"));
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
}