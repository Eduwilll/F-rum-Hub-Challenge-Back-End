package com.example.forum.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "responses")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Response {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "TEXT")
    private String mensagem;
    
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
    
    private Boolean solucao = false;
    
    @ManyToOne
    @JoinColumn(name = "topico_id")
    private Topic topico;
    
    @ManyToOne
    @JoinColumn(name = "autor_id")
    private User autor;
    
    public Response(String mensagem, Topic topico, User autor) {
        this.mensagem = mensagem;
        this.topico = topico;
        this.autor = autor;
        this.dataCriacao = LocalDateTime.now();
        this.solucao = false;
    }
    
    public void markAsSolution() {
        this.solucao = true;
    }
    
    public void unmarkAsSolution() {
        this.solucao = false;
    }
    
    public boolean isAuthor(User user) {
        return this.autor.equals(user);
    }
}