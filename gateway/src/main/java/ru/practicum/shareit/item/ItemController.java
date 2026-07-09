package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestBody ItemDto itemDto) {
        log.info("Gateway: создание вещи пользователем id={}", userId);

        validateItemForCreate(itemDto);

        return itemClient.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long itemId,
                                         @RequestBody ItemDto itemDto) {
        log.info("Gateway: обновление вещи id={}, userId={}", itemId, userId);

        validateItemForUpdate(itemDto);

        return itemClient.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PathVariable Long itemId) {
        log.info("Gateway: получение вещи id={}, userId={}", itemId, userId);

        return itemClient.getById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserItems(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Gateway: получение вещей пользователя id={}", userId);

        return itemClient.getUserItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text) {
        log.info("Gateway: поиск вещей по тексту '{}'", text);

        return itemClient.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long itemId,
                                             @RequestBody CommentDto commentDto) {
        log.info("Gateway: добавление комментария к вещи id={}, userId={}", itemId, userId);

        validateComment(commentDto);

        return itemClient.addComment(userId, itemId, commentDto);
    }

    private void validateItemForCreate(ItemDto itemDto) {
        if (itemDto == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название вещи не может быть пустым");
        }

        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }

        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус доступности вещи должен быть указан");
        }
    }

    private void validateItemForUpdate(ItemDto itemDto) {
        if (itemDto == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        if (itemDto.getName() != null && itemDto.getName().isBlank()) {
            throw new ValidationException("Название вещи не может быть пустым");
        }

        if (itemDto.getDescription() != null && itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }
    }

    private void validateComment(CommentDto commentDto) {
        if (commentDto == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }
    }
}