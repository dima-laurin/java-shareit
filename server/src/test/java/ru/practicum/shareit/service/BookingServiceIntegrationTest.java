package ru.practicum.shareit.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.BookingAccessException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void create_shouldCreateBookingWithWaitingStatus() {
        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Drill", "Power drill", true);

        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto created = bookingService.create(booker.getId(), request);

        assertThat(created.getId(), notNullValue());
        assertThat(created.getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(created.getBooker().getId(), equalTo(booker.getId()));
        assertThat(created.getItem().getId(), equalTo(item.getId()));

        Booking saved = bookingRepository.findById(created.getId()).orElseThrow();
        assertThat(saved.getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(saved.getBooker().getId(), equalTo(booker.getId()));
        assertThat(saved.getItem().getId(), equalTo(item.getId()));
    }

    @Test
    void approve_shouldChangeBookingStatusInDatabase() {
        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Drill", "Power drill", true);
        Booking booking = saveBooking(
                booker,
                item,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );

        BookingDto approved = bookingService.approve(owner.getId(), booking.getId(), true);

        assertThat(approved.getStatus(), equalTo(BookingStatus.APPROVED));
        assertThat(
                bookingRepository.findById(booking.getId()).orElseThrow().getStatus(),
                equalTo(BookingStatus.APPROVED)
        );
    }

    @Test
    void getById_shouldReturnBookingForBookerFromDatabase() {
        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Drill", "Power drill", true);
        Booking booking = saveBooking(
                booker,
                item,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.APPROVED
        );

        BookingDto found = bookingService.getById(booker.getId(), booking.getId());

        assertThat(found.getId(), equalTo(booking.getId()));
        assertThat(found.getStatus(), equalTo(BookingStatus.APPROVED));
        assertThat(found.getBooker().getId(), equalTo(booker.getId()));
        assertThat(found.getItem().getId(), equalTo(item.getId()));
        assertThat(found.getItem().getName(), equalTo("Drill"));
    }

    @Test
    void getUserBookings_shouldFilterUserBookingsByStateUsingDatabase() {
        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Drill", "Power drill", true);

        Booking past = saveBooking(
                booker,
                item,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(2),
                BookingStatus.APPROVED
        );
        saveBooking(
                booker,
                item,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );

        Collection<BookingDto> result = bookingService.getUserBookings(booker.getId(), "PAST");
        List<Long> bookingIds = result.stream()
                .map(BookingDto::getId)
                .collect(Collectors.toList());

        assertThat(result.size(), equalTo(1));
        assertThat(bookingIds, contains(past.getId()));
    }

    @Test
    void getOwnerBookings_shouldFilterOwnerBookingsByStateUsingDatabase() {
        User owner = saveUser("Owner", "owner@mail.com");
        User anotherOwner = saveUser("Another owner", "another-owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");

        Item ownerItem = saveItem(owner, "Drill", "Power drill", true);
        Item anotherOwnerItem = saveItem(anotherOwner, "Saw", "Electric saw", true);

        Booking waiting = saveBooking(
                booker,
                ownerItem,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );
        saveBooking(
                booker,
                ownerItem,
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4),
                BookingStatus.REJECTED
        );
        saveBooking(
                booker,
                anotherOwnerItem,
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(6),
                BookingStatus.WAITING
        );

        Collection<BookingDto> result = bookingService.getOwnerBookings(owner.getId(), "WAITING");
        List<Long> bookingIds = result.stream()
                .map(BookingDto::getId)
                .collect(Collectors.toList());

        assertThat(result.size(), equalTo(1));
        assertThat(bookingIds, contains(waiting.getId()));
    }

    @Test
    void approve_shouldRejectBookingWhenApprovedIsFalse() {
        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");

        Item item = saveItem(owner, "Drill", "Power drill", true);

        Booking booking = saveBooking(
                booker,
                item,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );

        BookingDto result = bookingService.approve(owner.getId(), booking.getId(), false);

        assertThat(result.getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void getById_shouldReturnBookingForOwner() {
        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");

        Item item = saveItem(owner, "Drill", "Power drill", true);

        Booking booking = saveBooking(
                booker,
                item,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );

        BookingDto result = bookingService.getById(owner.getId(), booking.getId());

        assertThat(result.getId(), equalTo(booking.getId()));
        assertThat(result.getBooker().getId(), equalTo(booker.getId()));
        assertThat(result.getItem().getId(), equalTo(item.getId()));
    }

    @Test
    void getUserBookings_shouldReturnAllBookingsWhenStateIsNull() {
        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");

        Item item = saveItem(owner, "Drill", "Power drill", true);

        Booking booking = saveBooking(
                booker,
                item,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );

        Collection<BookingDto> result = bookingService.getUserBookings(booker.getId(), null);

        assertThat(result.size(), equalTo(1));
        assertThat(result.iterator().next().getId(), equalTo(booking.getId()));
    }

    @Test
    void getOwnerBookings_shouldReturnAllBookingsWhenStateIsNull() {
        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");

        Item item = saveItem(owner, "Drill", "Power drill", true);

        Booking booking = saveBooking(
                booker,
                item,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );

        Collection<BookingDto> result = bookingService.getOwnerBookings(owner.getId(), null);

        assertThat(result.size(), equalTo(1));
        assertThat(result.iterator().next().getId(), equalTo(booking.getId()));
    }

    @Test
    void create_shouldThrowValidationExceptionWhenItemIdIsNull() {
        User booker = saveUser("Booker", "booker@mail.com");

        BookingRequestDto request = new BookingRequestDto();
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.create(booker.getId(), request)
        );

        assertThat(exception.getMessage(), equalTo("Не указана вещь для бронирования"));
    }

    @Test
    void create_shouldThrowBookingAccessExceptionWhenOwnerBooksOwnItem() {
        User owner = saveUser("Owner", "owner@mail.com");
        Item item = saveItem(owner, "Drill", "Power drill", true);

        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        BookingAccessException exception = assertThrows(
                BookingAccessException.class,
                () -> bookingService.create(owner.getId(), request)
        );

        assertThat(exception.getMessage(), equalTo("Владелец не может бронировать свою вещь"));
    }

    @Test
    void approve_shouldThrowForbiddenExceptionWhenUserIsNotOwner() {
        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        User other = saveUser("Other", "other@mail.com");

        Item item = saveItem(owner, "Drill", "Power drill", true);

        Booking booking = saveBooking(
                booker,
                item,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> bookingService.approve(other.getId(), booking.getId(), true)
        );

        assertThat(exception.getMessage(), equalTo("Подтвердить бронирование может только владелец вещи"));
    }

    @Test
    void create_shouldThrowValidationExceptionWhenDatesAreNull() {
        User booker = saveUser("Booker", "booker@mail.com");

        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(1L);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.create(booker.getId(), request)
        );

        assertThat(exception.getMessage(), equalTo("Даты бронирования должны быть указаны"));
    }

    @Test
    void create_shouldThrowValidationExceptionWhenEndIsBeforeStart() {
        User booker = saveUser("Booker", "booker@mail.com");

        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusDays(2));
        request.setEnd(LocalDateTime.now().plusDays(1));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.create(booker.getId(), request)
        );

        assertThat(exception.getMessage(), equalTo("Дата окончания должна быть позже даты начала"));
    }

    @Test
    void create_shouldThrowValidationExceptionWhenStartIsInPast() {
        User booker = saveUser("Booker", "booker@mail.com");

        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().minusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(1));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.create(booker.getId(), request)
        );

        assertThat(exception.getMessage(), equalTo("Дата начала бронирования не может быть в прошлом"));
    }

    @Test
    void create_shouldThrowValidationExceptionWhenItemIsNotAvailable() {
        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Drill", "Power drill", false);

        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(item.getId());
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.create(booker.getId(), request)
        );

        assertThat(exception.getMessage(), equalTo("Вещь недоступна для бронирования"));
    }

    @Test
    void approve_shouldThrowValidationExceptionWhenApprovedIsNull() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.approve(1L, 1L, null)
        );

        assertThat(exception.getMessage(), equalTo("Параметр approved должен быть указан"));
    }

    @Test
    void approve_shouldThrowValidationExceptionWhenBookingAlreadyProcessed() {
        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");

        Item item = saveItem(owner, "Drill", "Power drill", true);

        Booking booking = saveBooking(
                booker,
                item,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.APPROVED
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.approve(owner.getId(), booking.getId(), true)
        );

        assertThat(exception.getMessage(), equalTo("Бронирование уже обработано"));
    }

    @Test
    void create_shouldThrowNotFoundExceptionWhenItemDoesNotExist() {
        User booker = saveUser("Booker", "booker@mail.com");

        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(999L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.create(booker.getId(), request)
        );

        assertThat(exception.getMessage(), equalTo("Вещь с id=999 не найдена"));
    }

    @Test
    void approve_shouldThrowNotFoundExceptionWhenBookingDoesNotExist() {
        User owner = saveUser("Owner", "owner@mail.com");

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.approve(owner.getId(), 999L, true)
        );

        assertThat(exception.getMessage(), equalTo("Бронирование с id=999 не найдено"));
    }

    @Test
    void getUserBookings_shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.getUserBookings(999L, "ALL")
        );

        assertThat(exception.getMessage(), equalTo("Пользователь с id=999 не найден"));
    }

    @Test
    void getOwnerBookings_shouldThrowValidationExceptionWhenStateIsUnknown() {
        User owner = saveUser("Owner", "owner@mail.com");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.getOwnerBookings(owner.getId(), "UNKNOWN")
        );

        assertThat(exception.getMessage(), equalTo("Неизвестный статус: UNKNOWN"));
    }

    @Test
    void getUserBookings_shouldThrowValidationExceptionWhenStateIsUnknown() {
        User booker = saveUser("Booker", "booker@mail.com");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.getUserBookings(booker.getId(), "UNKNOWN")
        );

        assertThat(exception.getMessage(), equalTo("Неизвестный статус: UNKNOWN"));
    }

    @Test
    void getById_shouldThrowBookingAccessExceptionWhenUserIsNotBookerOrOwner() {
        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        User other = saveUser("Other", "other@mail.com");

        Item item = saveItem(owner, "Drill", "Power drill", true);

        Booking booking = saveBooking(
                booker,
                item,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING
        );

        BookingAccessException exception = assertThrows(
                BookingAccessException.class,
                () -> bookingService.getById(other.getId(), booking.getId())
        );

        assertThat(exception.getMessage(), equalTo("Бронирование недоступно пользователю"));
    }

    private User saveUser(String name, String email) {
        return userRepository.save(new User(null, name, email));
    }

    private Item saveItem(User owner, String name, String description, Boolean available) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);

        return itemRepository.save(item);
    }

    private Booking saveBooking(User booker,
                                Item item,
                                LocalDateTime start,
                                LocalDateTime end,
                                BookingStatus status) {
        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);

        return bookingRepository.save(booking);
    }
}

