package com.booknook.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler — catches all exceptions thrown anywhere in the app
 * and converts them into clean JSON error responses.
 *
 * Without this, Spring would return a confusing HTML error page.
 * With this, the frontend always gets a JSON like:
 *   { "status": 404, "error": "Book not found with id: 99", "timestamp": "..." }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── 404 Not Found ───────────────────────────────────────────────────────

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBookNotFound(BookNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ─── 409 Conflict ────────────────────────────────────────────────────────

    @ExceptionHandler(WishlistLimitException.class)
    public ResponseEntity<Map<String, Object>> handleWishlistLimit(WishlistLimitException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DuplicateWishlistException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateWishlistException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ─── 400 Bad Request (validation failures) ───────────────────────────────

    /**
     * Catches @Valid failures (blank title, negative price, etc.)
     * Collects all field-level error messages into a single readable string.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return buildError(HttpStatus.BAD_REQUEST, msg);
    }

    // ─── 500 Catch-all ───────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + ex.getMessage());
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    /** Builds a consistent JSON error body. */
    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status",    status.value());
        body.put("error",     message);
        body.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(status).body(body);
    }
}
