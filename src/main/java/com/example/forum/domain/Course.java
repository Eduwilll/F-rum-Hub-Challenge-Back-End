package com.example.forum.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nome;
    
    private String categoria;
    
    @OneToMany(mappedBy = "curso")
    private List<Topic> topics;
    
    public Course(String nome, String categoria) {
        this.nome = nome;
        this.categoria = categoria;
    }
}