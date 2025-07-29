package com.example.forum.exception;

public class UnauthorizedOperationException extends RuntimeException {
    
    public UnauthorizedOperationException(String message) {
        super(message);
    }
    
    public UnauthorizedOperationException() {
        super("Você não tem permissão para realizar esta operação");
    }
}