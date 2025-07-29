package com.example.forum.exception;

public class ResponseNotFoundException extends RuntimeException {
    
    public ResponseNotFoundException(String message) {
        super(message);
    }
    
    public ResponseNotFoundException(Long id) {
        super("Resposta com ID " + id + " não encontrada");
    }
}