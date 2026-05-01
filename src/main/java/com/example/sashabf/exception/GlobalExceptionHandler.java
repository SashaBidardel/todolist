package com.example.sashabf.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

//@ControllerAdvice para escuchar toda la aplicación
//Si algún servicio lanza una de las excepciones BadRequestException o ResourceNotFound, esta clase la atrapa y la convierte en un JSON entendible.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class) //(404)
    public ResponseEntity<ErrorMessage> resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            request.getDescription(false));
        
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(ForbiddenException.class) //(403)
    public ResponseEntity<ErrorMessage> forbiddenException(ForbiddenException ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            request.getDescription(false));
        
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class) //(400)
    public ResponseEntity<ErrorMessage> badRequestException(BadRequestException ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(
            HttpStatus.FORBIDDEN.value(),
            ex.getMessage(),
            request.getDescription(false));
        
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    // Un "atrapalotodo" para errores inesperados (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> globalExceptionHandler(Exception ex, WebRequest request) {
        ErrorMessage message = new ErrorMessage(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Error interno en el servidor",
            ex.getMessage());
        
        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
