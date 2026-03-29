package com.fanduel.depthchart.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> conflict(RuntimeException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<Map<String, Object>> jpaOptimistic(OptimisticLockException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler({ IllegalArgumentException.class, MethodArgumentNotValidException.class,
            ConstraintViolationException.class })
    public ResponseEntity<Map<String, Object>> badRequest(Exception ex, WebRequest request) {
        String msg;
        if (ex instanceof MethodArgumentNotValidException m) {
            var errors = m.getBindingResult().getAllErrors();
            msg = errors.isEmpty() ? "Validation failed" : errors.get(0).getDefaultMessage();
        } else {
            msg = ex.getMessage();
        }
        return build(HttpStatus.BAD_REQUEST, msg, request);
    }

    private static ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, WebRequest request) {
        String path = "";
        if (request instanceof ServletWebRequest sw) {
            path = sw.getRequest().getRequestURI();
        }
        return ResponseEntity.status(status)
                .body(Map.of("timestamp", Instant.now().toString(), "status", status.value(), "error",
                        status.getReasonPhrase(), "message", message != null ? message : "", "path", path));
    }
}
