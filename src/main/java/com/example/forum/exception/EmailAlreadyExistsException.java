package com.example.forum.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    
    public EmailAlreadyExistsException(String email) {
        super("Email " + email + " já está em uso");
    }
}