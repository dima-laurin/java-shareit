package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Slf4j
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestBody ItemRequestDto requestDto) {
        log.info("Gateway: создание запроса вещи пользователем id={}", userId);

        validateRequest(requestDto);

        return itemRequestClient.create(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Gateway: получение запросов пользователя id={}", userId);

        return itemRequestClient.getUserRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Gateway: получение запросов других пользователей для userId={}", userId);

        return itemRequestClient.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable Long requestId) {
        log.info("Gateway: получение запроса id={}, userId={}", requestId, userId);

        return itemRequestClient.getById(userId, requestId);
    }

    private void validateRequest(ItemRequestDto requestDto) {
        if (requestDto == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        if (requestDto.getDescription() == null || requestDto.getDescription().isBlank()) {
            throw new ValidationException("Описание запроса не может быть пустым");
        }
    }
}