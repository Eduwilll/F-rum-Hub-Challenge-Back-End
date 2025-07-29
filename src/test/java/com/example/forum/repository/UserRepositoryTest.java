package com.example.forum.repository;

import com.example.forum.domain.Profile;
import com.example.forum.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByEmail() {
        // Given
        Profile userProfile = createAndPersistProfile("USUARIO");
        User user = createAndPersistUser("João Silva", "joao@email.com", userProfile);
        
        // When
        Optional<User> result = userRepository.findByEmail("joao@email.com");
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(user.getId(), result.get().getId());
        assertEquals("João Silva", result.get().getNome());
        assertEquals("joao@email.com", result.get().getEmail());
    }

    @Test
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        // When
        Optional<User> result = userRepository.findByEmail("inexistente@email.com");
        
        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void shouldCheckIfUserExistsByEmail() {
        // Given
        Profile userProfile = createAndPersistProfile("USUARIO");
        createAndPersistUser("João Silva", "joao@email.com", userProfile);
        
        // When & Then
        assertTrue(userRepository.existsByEmail("joao@email.com"));
        assertFalse(userRepository.existsByEmail("inexistente@email.com"));
    }

    private Profile createAndPersistProfile(String nome) {
        Profile profile = new Profile(nome);
        return entityManager.persistAndFlush(profile);
    }

    private User createAndPersistUser(String nome, String email, Profile profile) {
        User user = new User(nome, email, "senha123", Set.of(profile));
        return entityManager.persistAndFlush(user);
    }
}