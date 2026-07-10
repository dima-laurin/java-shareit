package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.ValidationException;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestBody BookingRequestDto bookingDto) {
        log.info("Gateway: создание бронирования пользователем id={}", userId);

        validateBookingRequest(bookingDto);

        return bookingClient.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable Long bookingId,
                                          @RequestParam Boolean approved) {
        log.info("Gateway: изменение статуса бронирования id={}, userId={}, approved={}",
                bookingId, userId, approved);

        if (approved == null) {
            throw new ValidationException("Параметр approved должен быть указан");
        }

        return bookingClient.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable Long bookingId) {
        log.info("Gateway: получение бронирования id={}, userId={}", bookingId, userId);

        return bookingClient.getById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        log.info("Gateway: получение бронирований пользователя id={}, state={}", userId, state);

        BookingState bookingState = getBookingState(state);

        return bookingClient.getUserBookings(userId, bookingState);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state) {
        log.info("Gateway: получение бронирований владельца id={}, state={}", userId, state);

        BookingState bookingState = getBookingState(state);

        return bookingClient.getOwnerBookings(userId, bookingState);
    }

    private BookingState getBookingState(String stateValue) {
        if (stateValue == null || stateValue.isBlank()) {
            return BookingState.ALL;
        }

        try {
            return BookingState.valueOf(stateValue.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Неизвестный статус: " + stateValue);
        }
    }

    private void validateBookingRequest(BookingRequestDto bookingDto) {
        if (bookingDto == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        if (bookingDto.getItemId() == null) {
            throw new ValidationException("Не указана вещь для бронирования");
        }

        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new ValidationException("Даты бронирования должны быть указаны");
        }

        if (!bookingDto.getEnd().isAfter(bookingDto.getStart())) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }

        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала бронирования не может быть в прошлом");
        }
    }
}