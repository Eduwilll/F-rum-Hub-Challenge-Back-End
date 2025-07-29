package com.example.forum.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nome;
    
    @Column(unique = true)
    private String email;
    
    private String senha;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_profiles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "profile_id"))
    private Set<Profile> perfis;
    
    @OneToMany(mappedBy = "autor")
    private List<Topic> topics;
    
    @OneToMany(mappedBy = "autor")
    private List<Response> respostas;
    
    public User(String nome, String email, String senha, Set<Profile> perfis) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfis = perfis;
        this.createdAt = LocalDateTime.now();
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return perfis.stream()
                .map(profile -> new SimpleGrantedAuthority("ROLE_" + profile.getNome()))
                .collect(Collectors.toList());
    }
    
    @Override
    public String getPassword() {
        return senha;
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    public boolean hasRole(String roleName) {
        return perfis.stream()
                .anyMatch(profile -> profile.getNome().equals(roleName));
    }
}