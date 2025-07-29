package com.example.forum.service;

import com.example.forum.domain.Profile;
import com.example.forum.domain.User;
import com.example.forum.exception.EmailAlreadyExistsException;
import com.example.forum.exception.UserNotFoundException;
import com.example.forum.repository.ProfileRepository;
import com.example.forum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário com email " + email + " não encontrado"));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User createUser(String nome, String email, String senha, Set<String> profileNames) {
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        // Get profiles
        Set<Profile> profiles = profileNames.stream()
                .map(profileName -> profileRepository.findByNome(profileName)
                        .orElseThrow(() -> new RuntimeException("Profile " + profileName + " not found")))
                .collect(java.util.stream.Collectors.toSet());

        // Create user
        User user = new User(nome, email, passwordEncoder.encode(senha), profiles);
        return userRepository.save(user);
    }

    @Transactional
    public User addProfileToUser(Long userId, String profileName) {
        User user = findById(userId);
        Profile profile = profileRepository.findByNome(profileName)
                .orElseThrow(() -> new RuntimeException("Profile " + profileName + " not found"));

        user.getPerfis().add(profile);
        return userRepository.save(user);
    }

    @Transactional
    public User removeProfileFromUser(Long userId, String profileName) {
        User user = findById(userId);
        Profile profile = profileRepository.findByNome(profileName)
                .orElseThrow(() -> new RuntimeException("Profile " + profileName + " not found"));

        user.getPerfis().remove(profile);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isUserModerator(User user) {
        return user.hasRole("MODERADOR") || user.hasRole("ADMIN");
    }

    public boolean isUserAdmin(User user) {
        return user.hasRole("ADMIN");
    }
}