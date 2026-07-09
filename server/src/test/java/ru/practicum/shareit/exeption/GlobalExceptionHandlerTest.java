package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_shouldReturnErrorMessage() {
        Map<String, String> result = handler.handleNotFound(
                new NotFoundException("not found")
        );

        assertThat(result.get("error"), equalTo("not found"));
    }

    @Test
    void handleValidation_shouldReturnErrorMessage() {
        Map<String, String> result = handler.handleValidation(
                new ValidationException("bad request")
        );

        assertThat(result.get("error"), equalTo("bad request"));
    }

    @Test
    void handleSame_shouldReturnErrorMessage() {
        Map<String, String> result = handler.handleSame(
                new SameEmailException("same email")
        );

        assertThat(result.get("error"), equalTo("same email"));
    }

    @Test
    void handleForbiddenException_shouldReturnErrorMessage() {
        Map<String, String> result = handler.handleForbiddenException(
                new ForbiddenException("forbidden")
        );

        assertThat(result.get("error"), equalTo("forbidden"));
    }

    @Test
    void handleBookingAccessException_shouldReturnErrorMessage() {
        Map<String, String> result = handler.handleBookingAccessException(
                new BookingAccessException("booking access")
        );

        assertThat(result.get("error"), equalTo("booking access"));
    }

    @Test
    void handleOther_shouldReturnInternalServerErrorMessage() {
        Map<String, String> result = handler.handleOther(
                new RuntimeException("unknown")
        );

        assertThat(result.get("error"), equalTo("Внутренняя ошибка сервера"));
    }
}