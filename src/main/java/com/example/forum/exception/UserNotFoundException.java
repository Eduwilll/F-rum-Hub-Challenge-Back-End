package com.example.forum.exception;

public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(Long id) {
        super("Usuário com ID " + id + " não encontrado");
    }
    
    public UserNotFoundException() {
        super("Usuário não encontrado");
    }
}