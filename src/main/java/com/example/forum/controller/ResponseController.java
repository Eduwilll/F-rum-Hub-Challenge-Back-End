package com.example.forum.controller;

import com.example.forum.domain.Response;
import com.example.forum.domain.User;
import com.example.forum.dto.CreateResponseRequest;
import com.example.forum.dto.ErrorResponse;
import com.example.forum.dto.ResponseResponse;
import com.example.forum.service.ResponseService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/topicos")
@RequiredArgsConstructor
@Tag(name = "Respostas", description = "Endpoints para gerenciamento de respostas aos tópicos")
@SecurityRequirement(name = "Bearer Authentication")
public class ResponseController {

    private final ResponseService responseService;

    @PostMapping("/{topicId}/respostas")
    @Operation(summary = "Criar resposta", 
               description = "Cria uma nova resposta para um tópico específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Resposta criada com sucesso",
                    content = @Content(schema = @Schema(implementation = ResponseResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Tópico não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ResponseResponse> createResponse(
            @Parameter(description = "ID do tópico") @PathVariable Long topicId,
            @Valid @RequestBody CreateResponseRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        Response response = responseService.createResponse(topicId, request, currentUser);
        ResponseResponse responseDto = new ResponseResponse(response);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("/{topicId}/respostas")
    public ResponseEntity<List<ResponseResponse>> listTopicResponses(@PathVariable Long topicId) {
        List<Response> responses = responseService.findByTopic(topicId);
        List<ResponseResponse> responseDtos = responses.stream()
                .map(ResponseResponse::new)
                .toList();
        
        return ResponseEntity.ok(responseDtos);
    }

    @PutMapping("/respostas/{responseId}/solucao")
    public ResponseEntity<ResponseResponse> markAsSolution(
            @PathVariable Long responseId,
            @AuthenticationPrincipal User currentUser) {
        
        Response response = responseService.markAsSolution(responseId, currentUser);
        ResponseResponse responseDto = new ResponseResponse(response);
        
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/respostas/{responseId}/solucao")
    public ResponseEntity<ResponseResponse> unmarkAsSolution(
            @PathVariable Long responseId,
            @AuthenticationPrincipal User currentUser) {
        
        Response response = responseService.unmarkAsSolution(responseId, currentUser);
        ResponseResponse responseDto = new ResponseResponse(response);
        
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/respostas/{responseId}")
    public ResponseEntity<Void> deleteResponse(
            @PathVariable Long responseId,
            @AuthenticationPrincipal User currentUser) {
        
        responseService.deleteResponse(responseId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{topicId}/solucao")
    public ResponseEntity<ResponseResponse> getTopicSolution(@PathVariable Long topicId) {
        return responseService.findSolutionByTopic(topicId)
                .map(response -> ResponseEntity.ok(new ResponseResponse(response)))
                .orElse(ResponseEntity.notFound().build());
    }
}