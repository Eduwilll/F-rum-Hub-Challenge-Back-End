package com.example.forum.domain;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserWithCorrectProperties() {
        // Given
        Profile userProfile = new Profile("USUARIO");
        Set<Profile> profiles = Set.of(userProfile);
        
        // When
        User user = new User("João Silva", "joao@email.com", "senha123", profiles);
        
        // Then
        assertEquals("João Silva", user.getNome());
        assertEquals("joao@email.com", user.getEmail());
        assertEquals("senha123", user.getSenha());
        assertEquals(profiles, user.getPerfis());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void shouldImplementUserDetailsCorrectly() {
        // Given
        Profile userProfile = new Profile("USUARIO");
        User user = new User("João Silva", "joao@email.com", "senha123", Set.of(userProfile));
        
        // When & Then
        assertEquals("joao@email.com", user.getUsername());
        assertEquals("senha123", user.getPassword());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }

    @Test
    void shouldReturnCorrectAuthorities() {
        // Given
        Profile userProfile = new Profile("USUARIO");
        Profile moderatorProfile = new Profile("MODERADOR");
        User user = new User("João Silva", "joao@email.com", "senha123", 
                           Set.of(userProfile, moderatorProfile));
        
        // When
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        
        // Then
        assertEquals(2, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USUARIO")));
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_MODERADOR")));
    }

    @Test
    void shouldCheckRoleCorrectly() {
        // Given
        Profile userProfile = new Profile("USUARIO");
        Profile moderatorProfile = new Profile("MODERADOR");
        User user = new User("João Silva", "joao@email.com", "senha123", 
                           Set.of(userProfile, moderatorProfile));
        
        // When & Then
        assertTrue(user.hasRole("USUARIO"));
        assertTrue(user.hasRole("MODERADOR"));
        assertFalse(user.hasRole("ADMIN"));
    }
}