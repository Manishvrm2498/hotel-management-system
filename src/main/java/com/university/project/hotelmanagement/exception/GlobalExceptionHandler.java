package com.university.project.hotelmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    private Map<String, Object> createBody(HttpStatus status, String error, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        return response;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonErrors(HttpMessageNotReadableException ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Malformed JSON or Invalid Type");

        String message = "The request data is invalid.";
        if (ex.getMessage() != null && ex.getMessage().contains("RoomType")) {
            message = "Invalid Room Type. Accepted values are: [SINGLE, DOUBLE, DELUXE, SUITE, PRESIDENTIAL]";
        }

        response.put("message", message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("message", "Input data is invalid");
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return new ResponseEntity<>(createBody(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(createBody(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return new ResponseEntity<>(createBody(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccountNotVerifiedException.class)
    public ResponseEntity<Map<String, Object>> handleAccountNotVerified(AccountNotVerifiedException ex) {
        return new ResponseEntity<>(createBody(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedAccessException ex) {
        return new ResponseEntity<>(createBody(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateResourceException ex) {
        return new ResponseEntity<>(createBody(HttpStatus.CONFLICT, "Conflict", ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(OtpException.class)
    public ResponseEntity<Map<String, Object>> handleOtpException(OtpException ex) {

        return new ResponseEntity<>(createBody(HttpStatus.BAD_REQUEST, "OTP_ERROR", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return new ResponseEntity<>(
                createBody(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", ex.getMessage()),
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobal(Exception ex) {
        ex.printStackTrace();
        String message = ex.getCause() != null && ex.getCause().getMessage() != null
                ? ex.getCause().getMessage()
                : ex.getMessage();
        return new ResponseEntity<>(createBody(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", message), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
