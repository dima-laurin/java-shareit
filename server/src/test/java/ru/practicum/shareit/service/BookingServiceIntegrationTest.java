package ru.practicum.shareit.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
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

