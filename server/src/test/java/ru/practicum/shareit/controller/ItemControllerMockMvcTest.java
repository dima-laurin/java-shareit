package ru.practicum.shareit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingUserDto;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Test
    void shouldCreateItem() throws Exception {
        ItemDto response = new ItemDto(
                1L,
                "Item",
                "Description",
                true,
                null,
                null,
                null,
                List.of()
        );

        when(itemService.create(eq(1L), any(ItemDto.class))).thenReturn(response);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Item\",\"description\":\"Description\",\"available\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Item")))
                .andExpect(jsonPath("$.description", is("Description")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    void shouldUpdateItem() throws Exception {
        ItemDto response = new ItemDto(
                1L,
                "Updated item",
                "Updated description",
                false,
                null,
                null,
                null,
                List.of()
        );

        when(itemService.update(eq(1L), eq(1L), any(ItemDto.class))).thenReturn(response);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated item\",\"description\":\"Updated description\",\"available\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated item")))
                .andExpect(jsonPath("$.description", is("Updated description")))
                .andExpect(jsonPath("$.available", is(false)));
    }

    @Test
    void shouldGetItemById() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        BookingDto lastBooking = new BookingDto(
                1L,
                now.minusDays(3),
                now.minusDays(2),
                BookingStatus.APPROVED,
                new BookingUserDto(2L),
                new BookingItemDto(1L, "Item")
        );

        BookingDto nextBooking = new BookingDto(
                2L,
                now.plusDays(1),
                now.plusDays(2),
                BookingStatus.APPROVED,
                new BookingUserDto(3L),
                new BookingItemDto(1L, "Item")
        );

        CommentDto comment = new CommentDto(
                1L,
                "Good item",
                "Booker",
                now.minusDays(1)
        );

        ItemDto response = new ItemDto(
                1L,
                "Item",
                "Description",
                true,
                null,
                lastBooking,
                nextBooking,
                List.of(comment)
        );

        when(itemService.getById(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Item")))
                .andExpect(jsonPath("$.lastBooking.id", is(1)))
                .andExpect(jsonPath("$.nextBooking.id", is(2)))
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].text", is("Good item")));
    }

    @Test
    void shouldGetUserItems() throws Exception {
        ItemDto first = new ItemDto(1L, "First", "First description", true, null, null, null, List.of());
        ItemDto second = new ItemDto(2L, "Second", "Second description", false, null, null, null, List.of());

        when(itemService.getUserItems(1L)).thenReturn(List.of(first, second));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("First")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Second")));
    }

    @Test
    void shouldSearchItems() throws Exception {
        ItemDto response = new ItemDto(1L, "Drill", "Powerful drill", true, null, null, null, List.of());

        when(itemService.search("drill")).thenReturn(List.of(response));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Drill")));
    }

    @Test
    void shouldAddComment() throws Exception {
        CommentDto response = new CommentDto(
                1L,
                "Good item",
                "Booker",
                LocalDateTime.now()
        );

        when(itemService.addComment(eq(2L), eq(1L), any(CommentDto.class))).thenReturn(response);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Good item\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Good item")))
                .andExpect(jsonPath("$.authorName", is("Booker")));
    }
}

