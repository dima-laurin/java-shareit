package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<ru.practicum.shareit.booking.Booking, Long> {

    List<ru.practicum.shareit.booking.Booking> findByBooker_IdOrderByStartDesc(Long bookerId);

    List<ru.practicum.shareit.booking.Booking> findByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<ru.practicum.shareit.booking.Booking> findByBooker_IdAndEndBeforeOrderByStartDesc(
            Long bookerId,
            LocalDateTime end
    );

    List<ru.practicum.shareit.booking.Booking> findByBooker_IdAndStartAfterOrderByStartDesc(
            Long bookerId,
            LocalDateTime start
    );

    List<ru.practicum.shareit.booking.Booking> findByBooker_IdAndStatusOrderByStartDesc(
            Long bookerId,
            ru.practicum.shareit.booking.BookingStatus status
    );

    List<ru.practicum.shareit.booking.Booking> findByItem_Owner_IdOrderByStartDesc(Long ownerId);

    List<ru.practicum.shareit.booking.Booking> findByItem_Owner_IdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<ru.practicum.shareit.booking.Booking> findByItem_Owner_IdAndEndBeforeOrderByStartDesc(
            Long ownerId,
            LocalDateTime end
    );

    List<ru.practicum.shareit.booking.Booking> findByItem_Owner_IdAndStartAfterOrderByStartDesc(
            Long ownerId,
            LocalDateTime start
    );

    List<ru.practicum.shareit.booking.Booking> findByItem_Owner_IdAndStatusOrderByStartDesc(
            Long ownerId,
            ru.practicum.shareit.booking.BookingStatus status
    );

    Optional<ru.practicum.shareit.booking.Booking> findFirstByItem_IdAndStatusAndEndBeforeOrderByEndDesc(
            Long itemId,
            ru.practicum.shareit.booking.BookingStatus status,
            LocalDateTime now
    );

    Optional<Booking> findFirstByItem_IdAndStatusAndStartAfterOrderByStartAsc(
            Long itemId,
            BookingStatus status,
            LocalDateTime now
    );

    boolean existsByItem_IdAndBooker_IdAndEndBefore(
            Long itemId,
            Long bookerId,
            LocalDateTime now
    );
}