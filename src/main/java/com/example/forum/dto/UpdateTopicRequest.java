package com.example.forum.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateTopicRequest(
        @NotBlank(message = "Título é obrigatório")
        String titulo,
        
        @NotBlank(message = "Mensagem é obrigatória")
        String mensagem
) {}