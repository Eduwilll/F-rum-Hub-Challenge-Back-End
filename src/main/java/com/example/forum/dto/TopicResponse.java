package com.example.forum.dto;

import com.example.forum.domain.Topic;
import com.example.forum.domain.TopicStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TopicResponse(
        Long id,
        String titulo,
        String mensagem,
        LocalDateTime dataCriacao,
        TopicStatus status,
        String autorNome,
        String cursoNome,
        List<ResponseResponse> respostas
) {
    public TopicResponse(Topic topic) {
        this(
                topic.getId(),
                topic.getTitulo(),
                topic.getMensagem(),
                topic.getDataCriacao(),
                topic.getStatus(),
                topic.getAutor().getNome(),
                topic.getCurso().getNome(),
                topic.getRespostas() != null ? 
                    topic.getRespostas().stream().map(ResponseResponse::new).toList() : 
                    List.of()
        );
    }
}