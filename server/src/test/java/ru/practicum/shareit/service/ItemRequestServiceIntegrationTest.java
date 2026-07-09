package ru.practicum.shareit.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestItemDto;
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
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void create_shouldSaveRequestWithRequestorAndCreatedDateToDatabase() {
        User requestor = saveUser("Requestor", "requestor@mail.com");
        ItemRequestDto dto = new ItemRequestDto(null, "Need drill", null, null);

        ItemRequestDto created = itemRequestService.create(requestor.getId(), dto);

        assertThat(created.getId(), notNullValue());
        assertThat(created.getDescription(), equalTo("Need drill"));
        assertThat(created.getCreated(), notNullValue());
        assertThat(created.getItems().size(), equalTo(0));

        ItemRequest saved = itemRequestRepository.findById(created.getId()).orElseThrow();
        assertThat(saved.getDescription(), equalTo("Need drill"));
        assertThat(saved.getRequestor().getId(), equalTo(requestor.getId()));
        assertThat(saved.getCreated(), notNullValue());
    }

    @Test
    void getUserRequests_shouldReturnOwnRequestsWithLinkedItemsFromDatabase() {
        User requestor = saveUser("Requestor", "requestor@mail.com");
        User owner = saveUser("Owner", "owner@mail.com");
        ItemRequest request = saveRequest(requestor, "Need drill", LocalDateTime.now().minusDays(1));
        Item item = saveItem(owner, "Drill", "Power drill", true, request);

        Collection<ItemRequestDto> result = itemRequestService.getUserRequests(requestor.getId());

        assertThat(result.size(), equalTo(1));

        ItemRequestDto found = result.iterator().next();
        List<Long> itemIds = found.getItems().stream()
                .map(RequestItemDto::getId)
                .collect(Collectors.toList());

        assertThat(found.getId(), equalTo(request.getId()));
        assertThat(found.getDescription(), equalTo("Need drill"));
        assertThat(found.getItems().size(), equalTo(1));
        assertThat(itemIds, contains(item.getId()));
    }

    @Test
    void getAllRequests_shouldReturnOnlyOtherUsersRequestsFromDatabase() {
        User currentUser = saveUser("Current", "current@mail.com");
        User anotherUser = saveUser("Another", "another@mail.com");

        saveRequest(currentUser, "Own request", LocalDateTime.now().minusHours(1));
        ItemRequest anotherRequest = saveRequest(anotherUser, "Another request", LocalDateTime.now().minusHours(2));

        Collection<ItemRequestDto> result = itemRequestService.getAllRequests(currentUser.getId());
        List<Long> requestIds = result.stream()
                .map(ItemRequestDto::getId)
                .collect(Collectors.toList());

        assertThat(result.size(), equalTo(1));
        assertThat(requestIds, contains(anotherRequest.getId()));
    }

    @Test
    void getById_shouldReturnRequestWithLinkedItemsFromDatabase() {
        User requestor = saveUser("Requestor", "requestor@mail.com");
        User viewer = saveUser("Viewer", "viewer@mail.com");
        User owner = saveUser("Owner", "owner@mail.com");
        ItemRequest request = saveRequest(requestor, "Need a drill", LocalDateTime.now().minusDays(1));
        Item item = saveItem(owner, "Drill", "Power drill", true, request);

        ItemRequestDto found = itemRequestService.getById(viewer.getId(), request.getId());

        RequestItemDto linkedItem = found.getItems().get(0);

        assertThat(found.getId(), equalTo(request.getId()));
        assertThat(found.getDescription(), equalTo("Need a drill"));
        assertThat(found.getItems().size(), equalTo(1));
        assertThat(linkedItem.getId(), equalTo(item.getId()));
        assertThat(linkedItem.getName(), equalTo("Drill"));
        assertThat(linkedItem.getOwnerId(), equalTo(owner.getId()));
    }

    private User saveUser(String name, String email) {
        return userRepository.save(new User(null, name, email));
    }

    private ItemRequest saveRequest(User requestor, String description, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(created);

        return itemRequestRepository.save(request);
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
}

