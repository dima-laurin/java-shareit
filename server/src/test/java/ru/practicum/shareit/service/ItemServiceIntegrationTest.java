package ru.practicum.shareit.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Test
    void create_shouldSaveItemWithOwnerAndRequestToDatabase() {
        User owner = saveUser("Owner", "owner@mail.com");
        User requestor = saveUser("Requestor", "requestor@mail.com");
        ItemRequest request = saveRequest(requestor, "Need drill", LocalDateTime.now().minusDays(1));

        ItemDto itemDto = new ItemDto(
                null,
                "Drill",
                "Power drill",
                true,
                request.getId(),
                null,
                null,
                null
        );

        ItemDto created = itemService.create(owner.getId(), itemDto);

        assertThat(created.getId(), notNullValue());
        assertThat(created.getRequestId(), equalTo(request.getId()));

        Item saved = itemRepository.findById(created.getId()).orElseThrow();
        assertThat(saved.getName(), equalTo("Drill"));
        assertThat(saved.getDescription(), equalTo("Power drill"));
        assertThat(saved.getAvailable(), equalTo(true));
        assertThat(saved.getOwner().getId(), equalTo(owner.getId()));
        assertThat(saved.getRequest().getId(), equalTo(request.getId()));
    }

    @Test
    void update_shouldPatchItemFieldsInDatabase() {
        User owner = saveUser("Owner", "owner@mail.com");
        Item item = saveItem(owner, "Drill", "Old description", true);

        ItemDto updateDto = new ItemDto(
                null,
                null,
                "New description",
                false,
                null,
                null,
                null,
                null
        );

        ItemDto updated = itemService.update(owner.getId(), item.getId(), updateDto);

        assertThat(updated.getName(), equalTo("Drill"));
        assertThat(updated.getDescription(), equalTo("New description"));
        assertThat(updated.getAvailable(), equalTo(false));

        Item saved = itemRepository.findById(item.getId()).orElseThrow();
        assertThat(saved.getName(), equalTo("Drill"));
        assertThat(saved.getDescription(), equalTo("New description"));
        assertThat(saved.getAvailable(), equalTo(false));
    }

    @Test
    void getById_shouldReturnItemWithBookingsOnlyForOwnerAndCommentsFromDatabase() {
        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Drill", "Power drill", true);

        saveBooking(
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
                BookingStatus.APPROVED
        );
        saveComment(booker, item, "Great item", LocalDateTime.now().minusDays(1));

        ItemDto foundForOwner = itemService.getById(owner.getId(), item.getId());
        ItemDto foundForBooker = itemService.getById(booker.getId(), item.getId());

        List<String> commentTexts = foundForOwner.getComments().stream()
                .map(CommentDto::getText)
                .collect(Collectors.toList());

        assertThat(foundForOwner.getLastBooking(), notNullValue());
        assertThat(foundForOwner.getNextBooking(), notNullValue());
        assertThat(foundForOwner.getComments().size(), equalTo(1));
        assertThat(commentTexts, contains("Great item"));

        assertThat(foundForBooker.getLastBooking(), nullValue());
        assertThat(foundForBooker.getNextBooking(), nullValue());
        assertThat(foundForBooker.getComments().size(), equalTo(1));
    }

    @Test
    void getUserItems_shouldReturnOwnerItemsWithBookingInfoAndCommentsFromDatabase() {
        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item firstItem = saveItem(owner, "Drill", "Power drill", true);
        Item secondItem = saveItem(owner, "Saw", "Electric saw", true);

        saveBooking(
                booker,
                firstItem,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(2),
                BookingStatus.APPROVED
        );
        saveBooking(
                booker,
                firstItem,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                BookingStatus.APPROVED
        );
        saveComment(booker, firstItem, "Useful drill", LocalDateTime.now().minusDays(1));

        Collection<ItemDto> items = itemService.getUserItems(owner.getId());
        List<Long> itemIds = items.stream()
                .map(ItemDto::getId)
                .collect(Collectors.toList());

        assertThat(items.size(), equalTo(2));
        assertThat(itemIds, contains(firstItem.getId(), secondItem.getId()));

        ItemDto first = items.stream()
                .filter(itemDto -> itemDto.getId().equals(firstItem.getId()))
                .findFirst()
                .orElseThrow();
        List<String> commentTexts = first.getComments().stream()
                .map(CommentDto::getText)
                .collect(Collectors.toList());

        assertThat(first.getLastBooking(), notNullValue());
        assertThat(first.getNextBooking(), notNullValue());
        assertThat(first.getComments().size(), equalTo(1));
        assertThat(commentTexts, contains("Useful drill"));
    }

    @Test
    void search_shouldReturnOnlyAvailableItemsMatchingNameOrDescriptionFromDatabase() {
        User owner = saveUser("Owner", "owner@mail.com");
        Item availableByName = saveItem(owner, "Drill", "Tool", true);
        Item availableByDescription = saveItem(owner, "Toolbox", "Contains drill bits", true);
        saveItem(owner, "Broken drill", "Unavailable", false);
        saveItem(owner, "Saw", "Electric saw", true);

        Collection<ItemDto> result = itemService.search("drill");
        List<Long> itemIds = result.stream()
                .map(ItemDto::getId)
                .collect(Collectors.toList());

        assertThat(result.size(), equalTo(2));
        assertThat(itemIds, containsInAnyOrder(availableByName.getId(), availableByDescription.getId()));
    }

    @Test
    void addComment_shouldPersistCommentForUserWithFinishedBooking() {
        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Drill", "Power drill", true);

        saveBooking(
                booker,
                item,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(2),
                BookingStatus.APPROVED
        );

        CommentDto created = itemService.addComment(
                booker.getId(),
                item.getId(),
                new CommentDto(null, "Great item", null, null)
        );

        assertThat(created.getId(), notNullValue());
        assertThat(created.getText(), equalTo("Great item"));
        assertThat(created.getAuthorName(), equalTo("Booker"));
        assertThat(created.getCreated(), notNullValue());

        Comment saved = commentRepository.findById(created.getId()).orElseThrow();
        assertThat(saved.getText(), equalTo("Great item"));
        assertThat(saved.getAuthor().getId(), equalTo(booker.getId()));
        assertThat(saved.getItem().getId(), equalTo(item.getId()));
    }

    private User saveUser(String name, String email) {
        return userRepository.save(new User(null, name, email));
    }

    private Item saveItem(User owner, String name, String description, Boolean available) {
        return saveItem(owner, name, description, available, null);
    }

    private Item saveItem(User owner,
                          String name,
                          String description,
                          Boolean available,
                          ItemRequest request) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        item.setRequest(request);

        return itemRepository.save(item);
    }

    private ItemRequest saveRequest(User requestor, String description, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(created);

        return itemRequestRepository.save(request);
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

    private Comment saveComment(User author, Item item, String text, LocalDateTime created) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setCreated(created);

        return commentRepository.save(comment);
    }
}

