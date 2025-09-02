package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> body(HttpStatus status, String error, String message) {
        ErrorResponse resp = new ErrorResponse(error, message, LocalDateTime.now());
        return ResponseEntity.status(status).body(resp);
    }

    // 404 — не найдено (сущность)
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> notFound(NoSuchElementException ex) {
        log.warn("404 Not Found: {}", ex.getMessage());
        return body(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    // 404 — не найден эндпоинт
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> noHandler(NoHandlerFoundException ex) {
        String msg = "Endpoint not found: " + ex.getRequestURL();
        log.warn("404 No handler: {}", msg);
        return body(HttpStatus.NOT_FOUND, "NOT_FOUND", msg);
    }

    // 400 — неверные данные запроса
    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestHeaderException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ErrorResponse> badRequest(Exception ex) {
        String message = ex.getMessage();

        if (ex instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException manv = (MethodArgumentNotValidException) ex;
            // Собираем ошибки валидации полей тела запроса
            message = manv.getBindingResult().getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse(manv.getMessage());
        } else if (ex instanceof ConstraintViolationException) {
            ConstraintViolationException cve = (ConstraintViolationException) ex;
            // Собираем ошибки валидации параметров/заголовков (@Validated на контроллерах)
            message = cve.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse(cve.getMessage());
        }

        log.warn("400 Bad Request: {}", message);
        return body(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message);
    }

    // 400 — некорректное состояние
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> illegalState(IllegalStateException ex) {
        log.warn("400 Bad Request (illegal state): {}", ex.getMessage());
        return body(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
    }

    // 403 — запрет
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> forbidden(SecurityException ex) {
        log.warn("403 Forbidden: {}", ex.getMessage());
        return body(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage());
    }

    // 409 — наш доменный конфликт
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> conflict(ConflictException ex) {
        log.warn("409 Conflict: {}", ex.getMessage());
        return body(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage());
    }

    // 409 — конфликты на уровне БД/уникальные индексы
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> dataIntegrity(DataIntegrityViolationException ex) {
        String mostSpecific = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        log.warn("409 Conflict (data integrity): {}", mostSpecific);
        return body(HttpStatus.CONFLICT, "CONFLICT", mostSpecific);
    }

    // 500 — непредвиденная ошибка
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> internal(Exception ex) {
        log.error("500 Internal Server Error", ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected server error");
    }

}