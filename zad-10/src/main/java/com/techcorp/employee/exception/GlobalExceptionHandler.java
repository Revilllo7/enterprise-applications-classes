package com.techcorp.employee.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.context.request.WebRequest;

import com.techcorp.employee.dto.ErrorResponse;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorage(FileStorageException ex, WebRequest req) {
        String path = req.getDescription(false);
        if (path != null && path.startsWith("uri=")) path = path.substring(4);
        ErrorResponse err = new ErrorResponse(ex.getMessage(), Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), path);
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(InvalidFileException ex, WebRequest req) {
        String path = req.getDescription(false);
        if (path != null && path.startsWith("uri=")) path = path.substring(4);
        ErrorResponse err = new ErrorResponse(ex.getMessage(), Instant.now(), HttpStatus.BAD_REQUEST.value(), path);
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFileNotFound(FileNotFoundException ex, WebRequest req) {
        String path = req.getDescription(false);
        if (path != null && path.startsWith("uri=")) path = path.substring(4);
        ErrorResponse err = new ErrorResponse(ex.getMessage(), Instant.now(), HttpStatus.NOT_FOUND.value(), path);
        return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EmployeeNotFoundException ex, WebRequest req) {
        String path = req.getDescription(false);
        if (path != null && path.startsWith("uri=")) path = path.substring(4);
        ErrorResponse err = new ErrorResponse(ex.getMessage(), Instant.now(), HttpStatus.NOT_FOUND.value(), path);
        return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateEmailException ex, WebRequest req) {
        String path = req.getDescription(false);
        if (path != null && path.startsWith("uri=")) path = path.substring(4);
        ErrorResponse err = new ErrorResponse(ex.getMessage(), Instant.now(), HttpStatus.CONFLICT.value(), path);
        return new ResponseEntity<>(err, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidData(InvalidDataException ex, WebRequest req) {
        String path = req.getDescription(false);
        if (path != null && path.startsWith("uri=")) path = path.substring(4);
        ErrorResponse err = new ErrorResponse(ex.getMessage(), Instant.now(), HttpStatus.BAD_REQUEST.value(), path);
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArg(IllegalArgumentException ex, WebRequest req) {
        String path = req.getDescription(false);
        if (path != null && path.startsWith("uri=")) path = path.substring(4);
        ErrorResponse err = new ErrorResponse(ex.getMessage(), Instant.now(), HttpStatus.BAD_REQUEST.value(), path);
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, WebRequest req) {
        String path = req.getDescription(false);
        if (path != null && path.startsWith("uri=")) path = path.substring(4);
        ErrorResponse err = new ErrorResponse(ex.getMessage(), Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), path);
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Validation errors: return field -> message map
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a, HashMap::new));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            String path = v.getPropertyPath() == null ? "" : v.getPropertyPath().toString();
            errors.put(path, v.getMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
