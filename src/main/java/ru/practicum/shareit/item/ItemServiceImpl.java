package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("Создание вещи пользователем id={}", userId);

        User owner = getUserOrThrow(userId);

        validateItem(itemDto);

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        Item savedItem = itemRepository.save(item);

        log.info("Вещь успешно создана. id={}", savedItem.getId());

        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Обновление вещи id={} пользователем id={}", itemId, userId);

        getUserOrThrow(userId);

        Item item = getItemOrThrow(itemId);

        if (!item.getOwner().getId().equals(userId)) {
            log.warn("Пользователь id={} пытается изменить чужую вещь id={}",
                    userId, itemId);

            throw new NotFoundException(
                    "Редактировать вещь может только владелец"
            );
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.update(item);

        log.info("Вещь успешно обновлена. id={}", itemId);

        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getById(Long itemId) {
        log.info("Получение вещи id={}", itemId);

        Item item = getItemOrThrow(itemId);

        return ItemMapper.toItemDto(item);
    }

    @Override
    public Collection<ItemDto> getUserItems(Long userId) {
        log.info("Получение списка вещей владельца id={}", userId);

        getUserOrThrow(userId);

        return itemRepository.getByOwnerId(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public Collection<ItemDto> search(String text) {
        log.info("Поиск вещей по запросу '{}'", text);

        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.search(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private User getUserOrThrow(Long userId) {
        User user = userRepository.getById(userId);

        if (user == null) {
            log.warn("Пользователь не найден. id={}", userId);

            throw new NotFoundException(
                    "Пользователь с id=" + userId + " не найден"
            );
        }

        return user;
    }

    private Item getItemOrThrow(Long itemId) {
        Item item = itemRepository.getById(itemId);

        if (item == null) {
            log.warn("Вещь не найдена. id={}", itemId);

            throw new NotFoundException(
                    "Вещь с id=" + itemId + " не найдена"
            );
        }

        return item;
    }

    private void validateItem(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            log.warn("Попытка создать вещь без названия");

            throw new ValidationException(
                    "Название вещи не может быть пустым"
            );
        }

        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            log.warn("Попытка создать вещь без описания");

            throw new ValidationException(
                    "Описание вещи не может быть пустым"
            );
        }

        if (itemDto.getAvailable() == null) {
            log.warn("Не указан статус доступности вещи");

            throw new ValidationException(
                    "Статус доступности вещи должен быть указан"
            );
        }
    }
}