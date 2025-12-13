package com.pragma.ms_traceability.infrastructure.exceptionhandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class ControllerAdvisor {

    private static final String ERRORS = "errors";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = new HashMap<>();
        List<String> errorMessages = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errorMessages.add(error.getDefaultMessage()));
        errors.put(ERRORS, errorMessages);
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, List<String>>> handleBadCredentialsException(BadCredentialsException ex) {
        Map<String, List<String>> errors = new HashMap<>();
        // Devolvemos un mensaje genérico por seguridad.
        errors.put(ERRORS, List.of("Invalid credentials. Please check your email and password."));

        // Un fallo de credenciales debe devolver 401 UNAUTHORIZED
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, List<String>>> handleGlobalException(Exception ex, WebRequest request) {
        log.error("An unexpected error occurred: {} Hash code: {}", ex.getMessage(), ex.hashCode());
        Map<String, List<String>> errors = new HashMap<>();
        errors.put(ERRORS, List.of("An unexpected error occurred: " + ex.getMessage(), ((ServletWebRequest) request).getRequest().getRequestURI()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errors);
    }
}
