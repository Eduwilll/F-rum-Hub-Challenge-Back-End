package com.example.forum.exception;

public class CourseNotFoundException extends RuntimeException {
    
    public CourseNotFoundException(String message) {
        super(message);
    }
    
    public CourseNotFoundException(Long id) {
        super("Curso com ID " + id + " não encontrado");
    }
}