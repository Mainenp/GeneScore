package com.mainenp.genedb.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 * Catches all exceptions and returns unified error response format
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handle ResourceNotFoundException
     * @param ex the exception
     * @param request the web request
     * @return unified error response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        
        log.warn("Resource not found: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 404);
        response.put("msg", ex.getMessage());
        response.put("data", null);
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Handle ApiCallException
     * @param ex the exception
     * @param request the web request
     * @return unified error response
     */
    @ExceptionHandler(ApiCallException.class)
    public ResponseEntity<Map<String, Object>> handleApiCallException(
            ApiCallException ex, WebRequest request) {
        
        log.error("API call failed: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 502);
        response.put("msg", "External API call failed: " + ex.getMessage());
        response.put("data", null);
        
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }
    
    /**
     * Handle IllegalArgumentException
     * @param ex the exception
     * @param request the web request
     * @return unified error response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        
        log.warn("Invalid argument: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 400);
        response.put("msg", "Invalid argument: " + ex.getMessage());
        response.put("data", null);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handle all other exceptions
     * @param ex the exception
     * @param request the web request
     * @return unified error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("msg", "Internal server error: " + ex.getMessage());
        response.put("data", null);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
