package ru.practicum.shareit.request.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRequestMapper {

    public static ru.practicum.shareit.request.dto.ItemRequestDto toItemRequestDto(ItemRequest request,
                                                                                   List<ru.practicum.shareit.request.dto.RequestItemDto> items) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                items
        );
    }

    public static ru.practicum.shareit.request.dto.RequestItemDto toRequestItemDto(Item item) {
        return new RequestItemDto(
                item.getId(),
                item.getName(),
                item.getOwner().getId()
        );
    }
}
