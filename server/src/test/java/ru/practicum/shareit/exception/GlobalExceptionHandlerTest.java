package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler h = new GlobalExceptionHandler();

    @Test void notFound_404() {
        ResponseEntity<ErrorResponse> r = h.notFound(new NoSuchElementException("x"));
        assertEquals(404, r.getStatusCode().value());
    }

    @Test void conflict_409() {
        ResponseEntity<ErrorResponse> r = h.dataIntegrity(new DataIntegrityViolationException("dup"));
        assertEquals(409, r.getStatusCode().value());
    }

    @Test void internal_500() {
        ResponseEntity<ErrorResponse> r = h.internal(new RuntimeException("boom"));
        assertEquals(500, r.getStatusCode().value());
    }
}