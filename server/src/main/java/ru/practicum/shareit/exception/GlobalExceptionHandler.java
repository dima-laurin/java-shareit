package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ru.practicum.shareit.exception.NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NotFoundException e) {
        log.error(e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(ru.practicum.shareit.exception.ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(ValidationException e) {
        log.error(e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(ru.practicum.shareit.exception.SameEmailException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleSame(SameEmailException e) {
        log.error(e.getMessage());
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleOther(RuntimeException e) {
        log.error(e.getMessage(), e);
        return Map.of("error", "Внутренняя ошибка сервера");
    }

    @ExceptionHandler(ru.practicum.shareit.exception.ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleForbiddenException(ForbiddenException e) {
        log.warn("Ошибка доступа: {}", e.getMessage());

        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(ru.practicum.shareit.exception.BookingAccessException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleBookingAccessException(BookingAccessException e) {
        log.warn("Ошибка доступа к бронированию: {}", e.getMessage());

        return Map.of("error", e.getMessage());
    }
}
