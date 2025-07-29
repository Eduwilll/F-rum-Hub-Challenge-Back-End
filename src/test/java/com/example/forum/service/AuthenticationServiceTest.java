package com.example.forum.service;

import com.example.forum.domain.Profile;
import com.example.forum.domain.User;
import com.example.forum.dto.AuthResponse;
import com.example.forum.dto.LoginRequest;
import com.example.forum.dto.RegisterRequest;
import com.example.forum.exception.EmailAlreadyExistsException;
import com.example.forum.exception.InvalidCredentialsException;
import com.example.forum.repository.ProfileRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private Profile userProfile;
    private User user;

    @BeforeEach
    void setUp() {
        userProfile = new Profile("USUARIO");
        user = new User("João Silva", "joao@email.com", "encodedPassword", Set.of(userProfile));
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Given
        RegisterRequest request = new RegisterRequest("João Silva", "joao@email.com", "senha123");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(profileRepository.findByNome("USUARIO")).thenReturn(Optional.of(userProfile));
        when(passwordEncoder.encode(request.senha())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(user)).thenReturn("jwt-token");

        // When
        AuthResponse result = authenticationService.register(request);

        // Then
        assertNotNull(result);
        assertEquals("jwt-token", result.token());
        assertEquals("João Silva", result.nome());
        assertEquals("joao@email.com", result.email());
        assertTrue(result.perfis().contains("USUARIO"));

        verify(userRepository).existsByEmail(request.email());
        verify(profileRepository).findByNome("USUARIO");
        verify(passwordEncoder).encode(request.senha());
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(user);
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        RegisterRequest request = new RegisterRequest("João Silva", "joao@email.com", "senha123");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // When & Then
        assertThrows(EmailAlreadyExistsException.class, 
                () -> authenticationService.register(request));

        verify(userRepository).existsByEmail(request.email());
        verify(profileRepository, never()).findByNome(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginSuccessfully() {
        // Given
        LoginRequest request = new LoginRequest("joao@email.com", "senha123");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(user)).thenReturn("jwt-token");

        // When
        AuthResponse result = authenticationService.login(request);

        // Then
        assertNotNull(result);
        assertEquals("jwt-token", result.token());
        assertEquals("João Silva", result.nome());
        assertEquals("joao@email.com", result.email());
        assertTrue(result.perfis().contains("USUARIO"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(user);
    }

    @Test
    void shouldThrowExceptionWhenLoginFails() {
        // Given
        LoginRequest request = new LoginRequest("joao@email.com", "wrongPassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThrows(InvalidCredentialsException.class, 
                () -> authenticationService.login(request));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any());
    }
}