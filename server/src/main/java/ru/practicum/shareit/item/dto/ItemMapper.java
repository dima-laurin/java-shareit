package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {

    public static ru.practicum.shareit.item.dto.ItemDto toItemDto(Item item) {
        return toItemDto(item, null, null, List.of());
    }

    public static ru.practicum.shareit.item.dto.ItemDto toItemDto(Item item,
                                                                  BookingDto lastBooking,
                                                                  BookingDto nextBooking,
                                                                  List<CommentDto> comments) {
        return new ru.practicum.shareit.item.dto.ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest() != null
                        ? item.getRequest().getId()
                        : null,
                lastBooking,
                nextBooking,
                comments
        );
    }

    public static Item toItem(ItemDto itemDto) {
        Item item = new Item();

        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());

        return item;
    }
}
