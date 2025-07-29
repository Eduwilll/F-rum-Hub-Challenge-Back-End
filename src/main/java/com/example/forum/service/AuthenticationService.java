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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        // Get default user profile
        Profile userProfile = profileRepository.findByNome("USUARIO")
                .orElseThrow(() -> new RuntimeException("Profile USUARIO not found"));

        // Create new user
        User user = new User(
                request.nome(),
                request.email(),
                passwordEncoder.encode(request.senha()),
                Set.of(userProfile)
        );

        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser);

        return new AuthResponse(
                token,
                savedUser.getNome(),
                savedUser.getEmail(),
                savedUser.getPerfis().stream()
                        .map(Profile::getNome)
                        .collect(Collectors.toSet())
        );
    }

    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.senha()
                    )
            );

            User user = (User) authentication.getPrincipal();

            // Generate JWT token
            String token = jwtUtil.generateToken(user);

            return new AuthResponse(
                    token,
                    user.getNome(),
                    user.getEmail(),
                    user.getPerfis().stream()
                            .map(Profile::getNome)
                            .collect(Collectors.toSet())
            );

        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Email ou senha incorretos");
        }
    }
}