package com.example.sashabf.exception;

//Para recursos no encontrados (404)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
