package com.example.forum.exception;

public class TopicNotFoundException extends RuntimeException {
    
    public TopicNotFoundException(String message) {
        super(message);
    }
    
    public TopicNotFoundException(Long id) {
        super("Tópico com ID " + id + " não encontrado");
    }
}