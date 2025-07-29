package com.example.forum.dto;

import java.util.Set;

public record AuthResponse(
        String token,
        String nome,
        String email,
        Set<String> perfis
) {}