package ru.practicum.shareit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void shouldCreateRequest() throws Exception {
        ItemRequestDto response = new ItemRequestDto(
                1L,
                "Need item",
                LocalDateTime.now(),
                List.of()
        );

        when(itemRequestService.create(eq(1L), any(ItemRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Need item\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Need item")))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void shouldGetUserRequests() throws Exception {
        ItemRequestDto first = new ItemRequestDto(
                1L,
                "First request",
                LocalDateTime.now(),
                List.of(new RequestItemDto(1L, "Item", 2L))
        );
        ItemRequestDto second = new ItemRequestDto(
                2L,
                "Second request",
                LocalDateTime.now(),
                List.of()
        );

        when(itemRequestService.getUserRequests(1L)).thenReturn(List.of(first, second));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("First request")))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].description", is("Second request")));
    }

    @Test
    void shouldGetAllRequests() throws Exception {
        ItemRequestDto response = new ItemRequestDto(
                1L,
                "Other user request",
                LocalDateTime.now(),
                List.of(new RequestItemDto(1L, "Item", 2L))
        );

        when(itemRequestService.getAllRequests(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].description", is("Other user request")))
                .andExpect(jsonPath("$[0].items[0].name", is("Item")));
    }

    @Test
    void shouldGetRequestById() throws Exception {
        ItemRequestDto response = new ItemRequestDto(
                1L,
                "Need item",
                LocalDateTime.now(),
                List.of(new RequestItemDto(1L, "Item", 2L))
        );

        when(itemRequestService.getById(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Need item")))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id", is(1)))
                .andExpect(jsonPath("$.items[0].name", is("Item")))
                .andExpect(jsonPath("$.items[0].ownerId", is(2)));
    }
}

