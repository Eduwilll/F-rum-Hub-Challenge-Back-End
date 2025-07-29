package com.example.forum.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "topics")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Topic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String titulo;
    
    @Column(columnDefinition = "TEXT")
    private String mensagem;
    
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
    
    @Enumerated(EnumType.STRING)
    private TopicStatus status;
    
    @ManyToOne
    @JoinColumn(name = "autor_id")
    private User autor;
    
    @ManyToOne
    @JoinColumn(name = "curso_id")
    private Course curso;
    
    @OneToMany(mappedBy = "topico", cascade = CascadeType.ALL)
    private List<Response> respostas;
    
    public Topic(String titulo, String mensagem, User autor, Course curso) {
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.autor = autor;
        this.curso = curso;
        this.dataCriacao = LocalDateTime.now();
        this.status = TopicStatus.OPEN;
    }
    
    public void updateContent(String titulo, String mensagem) {
        this.titulo = titulo;
        this.mensagem = mensagem;
    }
    
    public void close() {
        this.status = TopicStatus.CLOSED;
    }
    
    public void open() {
        this.status = TopicStatus.OPEN;
    }
    
    public boolean isAuthor(User user) {
        return this.autor.equals(user);
    }
}