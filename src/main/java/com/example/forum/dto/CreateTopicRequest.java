package com.example.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTopicRequest(
        @NotBlank(message = "Título é obrigatório")
        String titulo,
        
        @NotBlank(message = "Mensagem é obrigatória")
        String mensagem,
        
        @NotNull(message = "Curso é obrigatório")
        Long cursoId
) {}