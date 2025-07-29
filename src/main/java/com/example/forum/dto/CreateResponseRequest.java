package com.example.forum.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateResponseRequest(
        @NotBlank(message = "Mensagem é obrigatória")
        String mensagem
) {}