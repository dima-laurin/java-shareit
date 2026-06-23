package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.util.Collection;

public interface BookingService {

    BookingDto create(Long userId, BookingRequestDto bookingDto);

    BookingDto approve(Long userId, Long bookingId, Boolean approved);

    BookingDto getById(Long userId, Long bookingId);

    Collection<BookingDto> getUserBookings(Long userId, String state);

    Collection<BookingDto> getOwnerBookings(Long userId, String state);
}
