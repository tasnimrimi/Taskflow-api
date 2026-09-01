package com.tasnim.taskflow_api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationError(
            MethodArgumentNotValidException exception
    ) {
        FieldError fieldError =
                exception.getBindingResult().getFieldError();

        String field = fieldError != null
                ? fieldError.getField()
                : "request";

        String message = fieldError != null
                ? fieldError.getDefaultMessage()
                : "Invalid request";

        Map<String, String> response = new LinkedHashMap<>();
        response.put("error", "Validation failed");
        response.put("field", field);
        response.put("message", message);

        return ResponseEntity.badRequest().body(response);
    }
}