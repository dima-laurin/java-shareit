package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.BookingAccessException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto create(Long userId, BookingRequestDto bookingDto) {
        log.info("Создание бронирования пользователем id={}", userId);

        User booker = getUserOrThrow(userId);

        validateBookingDates(bookingDto);

        Item item = getItemOrThrow(bookingDto.getItemId());

        if (item.getOwner().getId().equals(userId)) {
            log.warn("Владелец id={} пытается забронировать свою вещь id={}",
                    userId, item.getId());

            throw new BookingAccessException(
                    "Владелец не может бронировать свою вещь"
            );
        }

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            log.warn("Попытка забронировать недоступную вещь id={}", item.getId());

            throw new ValidationException(
                    "Вещь недоступна для бронирования"
            );
        }

        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);

        log.info("Бронирование успешно создано. id={}", savedBooking.getId());

        return BookingMapper.toBookingDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto approve(Long userId, Long bookingId, Boolean approved) {
        log.info("Подтверждение бронирования id={} пользователем id={}",
                bookingId, userId);

        if (approved == null) {
            log.warn("Не указан параметр approved для бронирования id={}", bookingId);

            throw new ValidationException(
                    "Параметр approved должен быть указан"
            );
        }

        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("Пользователь id={} пытается подтвердить чужое бронирование id={}",
                    userId, bookingId);

            throw new ForbiddenException(
                    "Подтвердить бронирование может только владелец вещи"
            );
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            log.warn("Попытка повторно обработать бронирование id={}", bookingId);

            throw new ValidationException(
                    "Бронирование уже обработано"
            );
        }

        booking.setStatus(approved
                ? BookingStatus.APPROVED
                : BookingStatus.REJECTED);

        Booking updatedBooking = bookingRepository.save(booking);

        log.info("Статус бронирования id={} изменён на {}",
                bookingId, updatedBooking.getStatus());

        return BookingMapper.toBookingDto(updatedBooking);
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        log.info("Получение бронирования id={} пользователем id={}",
                bookingId, userId);

        getUserOrThrow(userId);

        Booking booking = getBookingOrThrow(bookingId);

        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getOwner().getId();

        if (!bookerId.equals(userId) && !ownerId.equals(userId)) {
            log.warn("Пользователь id={} не имеет доступа к бронированию id={}",
                    userId, bookingId);

            throw new BookingAccessException(
                    "Бронирование недоступно пользователю"
            );
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public Collection<BookingDto> getUserBookings(Long userId, String stateValue) {
        log.info("Получение бронирований пользователя id={}, state={}",
                userId, stateValue);

        getUserOrThrow(userId);

        BookingState state = getBookingState(stateValue);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBooker_IdOrderByStartDesc(userId);
                break;
            case CURRENT:
                bookings = bookingRepository
                        .findByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(
                                userId,
                                now,
                                now
                        );
                break;
            case PAST:
                bookings = bookingRepository
                        .findByBooker_IdAndEndBeforeOrderByStartDesc(userId, now);
                break;
            case FUTURE:
                bookings = bookingRepository
                        .findByBooker_IdAndStartAfterOrderByStartDesc(userId, now);
                break;
            case WAITING:
                bookings = bookingRepository
                        .findByBooker_IdAndStatusOrderByStartDesc(
                                userId,
                                BookingStatus.WAITING
                        );
                break;
            case REJECTED:
                bookings = bookingRepository
                        .findByBooker_IdAndStatusOrderByStartDesc(
                                userId,
                                BookingStatus.REJECTED
                        );
                break;
            default:
                throw new ValidationException("Неизвестный статус: " + stateValue);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Override
    public Collection<BookingDto> getOwnerBookings(Long userId, String stateValue) {
        log.info("Получение бронирований вещей владельца id={}, state={}",
                userId, stateValue);

        getUserOrThrow(userId);

        BookingState state = getBookingState(stateValue);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByItem_Owner_IdOrderByStartDesc(userId);
                break;
            case CURRENT:
                bookings = bookingRepository
                        .findByItem_Owner_IdAndStartBeforeAndEndAfterOrderByStartDesc(
                                userId,
                                now,
                                now
                        );
                break;
            case PAST:
                bookings = bookingRepository
                        .findByItem_Owner_IdAndEndBeforeOrderByStartDesc(userId, now);
                break;
            case FUTURE:
                bookings = bookingRepository
                        .findByItem_Owner_IdAndStartAfterOrderByStartDesc(userId, now);
                break;
            case WAITING:
                bookings = bookingRepository
                        .findByItem_Owner_IdAndStatusOrderByStartDesc(
                                userId,
                                BookingStatus.WAITING
                        );
                break;
            case REJECTED:
                bookings = bookingRepository
                        .findByItem_Owner_IdAndStatusOrderByStartDesc(
                                userId,
                                BookingStatus.REJECTED
                        );
                break;
            default:
                throw new ValidationException("Неизвестный статус: " + stateValue);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    private BookingState getBookingState(String stateValue) {
        if (stateValue == null || stateValue.isBlank()) {
            return BookingState.ALL;
        }

        try {
            return BookingState.valueOf(stateValue.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Получен неизвестный state: {}", stateValue);

            throw new ValidationException(
                    "Неизвестный статус: " + stateValue
            );
        }
    }

    private void validateBookingDates(BookingRequestDto bookingDto) {
        if (bookingDto.getItemId() == null) {
            log.warn("Не указана вещь для бронирования");

            throw new ValidationException(
                    "Не указана вещь для бронирования"
            );
        }

        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            log.warn("Не указаны даты бронирования");

            throw new ValidationException(
                    "Даты бронирования должны быть указаны"
            );
        }

        if (!bookingDto.getEnd().isAfter(bookingDto.getStart())) {
            log.warn("Дата окончания бронирования раньше или равна дате начала");

            throw new ValidationException(
                    "Дата окончания должна быть позже даты начала"
            );
        }

        if (bookingDto.getStart().isBefore(LocalDateTime.now())) {
            log.warn("Дата начала бронирования находится в прошлом");

            throw new ValidationException(
                    "Дата начала бронирования не может быть в прошлом"
            );
        }
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден. id={}", userId);

                    return new NotFoundException(
                            "Пользователь с id=" + userId + " не найден"
                    );
                });
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь не найдена. id={}", itemId);

                    return new NotFoundException(
                            "Вещь с id=" + itemId + " не найдена"
                    );
                });
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Бронирование не найдено. id={}", bookingId);

                    return new NotFoundException(
                            "Бронирование с id=" + bookingId + " не найдено"
                    );
                });
    }
}
