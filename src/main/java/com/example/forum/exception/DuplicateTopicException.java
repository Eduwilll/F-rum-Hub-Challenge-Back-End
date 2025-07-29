package com.example.forum.exception;

public class DuplicateTopicException extends RuntimeException {
    
    public DuplicateTopicException(String message) {
        super(message);
    }
    
    public DuplicateTopicException() {
        super("Já existe um tópico com o mesmo título e mensagem");
    }
}