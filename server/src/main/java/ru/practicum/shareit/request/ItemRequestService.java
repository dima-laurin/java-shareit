package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;

public interface ItemRequestService {

    ItemRequestDto create(Long userId, ItemRequestDto requestDto);

    Collection<ItemRequestDto> getUserRequests(Long userId);

    Collection<ItemRequestDto> getAllRequests(Long userId);

    ItemRequestDto getById(Long userId, Long requestId);
}
