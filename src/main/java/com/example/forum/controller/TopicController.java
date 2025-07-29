package com.example.forum.controller;

import com.example.forum.domain.Topic;
import com.example.forum.domain.TopicStatus;
import com.example.forum.domain.User;
import com.example.forum.dto.CreateTopicRequest;
import com.example.forum.dto.ErrorResponse;
import com.example.forum.dto.TopicResponse;
import com.example.forum.dto.UpdateTopicRequest;
import com.example.forum.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/topicos")
@RequiredArgsConstructor
@Tag(name = "Tópicos", description = "Endpoints para gerenciamento de tópicos do fórum")
@SecurityRequirement(name = "Bearer Authentication")
public class TopicController {

    private final TopicService topicService;

    @PostMapping
    @Operation(summary = "Criar novo tópico", 
               description = "Cria um novo tópico no fórum")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tópico criado com sucesso",
                    content = @Content(schema = @Schema(implementation = TopicResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Tópico duplicado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TopicResponse> createTopic(
            @Valid @RequestBody CreateTopicRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        Topic topic = topicService.createTopic(request, currentUser);
        TopicResponse response = new TopicResponse(topic);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar tópicos", 
               description = "Lista todos os tópicos com paginação e filtro opcional por status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tópicos retornada com sucesso")
    })
    public ResponseEntity<Page<TopicResponse>> listTopics(
            @Parameter(description = "Filtrar por status do tópico")
            @RequestParam(required = false) TopicStatus status,
            @Parameter(description = "Parâmetros de paginação")
            @PageableDefault(size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<Topic> topics;
        if (status != null) {
            topics = topicService.findByStatus(status, pageable);
        } else {
            topics = topicService.findAll(pageable);
        }
        
        Page<TopicResponse> response = topics.map(TopicResponse::new);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TopicResponse> getTopicById(@PathVariable Long id) {
        Topic topic = topicService.findById(id);
        TopicResponse response = new TopicResponse(topic);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TopicResponse> updateTopic(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTopicRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        Topic topic = topicService.updateTopic(id, request, currentUser);
        TopicResponse response = new TopicResponse(topic);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        topicService.deleteTopic(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<TopicResponse> closeTopic(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        Topic topic = topicService.closeTopic(id, currentUser);
        TopicResponse response = new TopicResponse(topic);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/open")
    public ResponseEntity<TopicResponse> openTopic(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        
        Topic topic = topicService.openTopic(id, currentUser);
        TopicResponse response = new TopicResponse(topic);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TopicResponse> updateTopicStatus(
            @PathVariable Long id,
            @RequestParam TopicStatus status,
            @AuthenticationPrincipal User currentUser) {
        
        Topic topic = topicService.updateTopicStatus(id, status, currentUser);
        TopicResponse response = new TopicResponse(topic);
        
        return ResponseEntity.ok(response);
    }
}