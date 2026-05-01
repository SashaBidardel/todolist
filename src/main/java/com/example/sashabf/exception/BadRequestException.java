package com.example.sashabf.exception;

//Para conflictos como el email duplicado (400 o 409)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}