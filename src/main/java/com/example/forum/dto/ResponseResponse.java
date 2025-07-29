package com.example.forum.dto;

import com.example.forum.domain.Response;

import java.time.LocalDateTime;

public record ResponseResponse(
        Long id,
        String mensagem,
        LocalDateTime dataCriacao,
        Boolean solucao,
        String autorNome
) {
    public ResponseResponse(Response response) {
        this(
                response.getId(),
                response.getMensagem(),
                response.getDataCriacao(),
                response.getSolucao(),
                response.getAutor().getNome()
        );
    }
}