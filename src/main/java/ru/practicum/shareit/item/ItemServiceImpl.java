package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CommentMapper;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("Создание вещи пользователем id={}", userId);

        User owner = getUserOrThrow(userId);

        validateItem(itemDto);

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        Item savedItem = itemRepository.save(item);

        log.info("Вещь успешно создана. id={}", savedItem.getId());

        return buildItemDto(savedItem, userId);
    }

    @Override
    @Transactional
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

        Item updatedItem = itemRepository.save(item);

        log.info("Вещь успешно обновлена. id={}", itemId);

        return buildItemDto(updatedItem, userId);
    }

    @Override
    public ItemDto getById(Long userId, Long itemId) {
        log.info("Получение вещи id={} пользователем id={}", itemId, userId);

        getUserOrThrow(userId);

        Item item = getItemOrThrow(itemId);

        return buildItemDto(item, userId);
    }

    @Override
    public Collection<ItemDto> getUserItems(Long userId) {
        log.info("Получение списка вещей владельца id={}", userId);

        getUserOrThrow(userId);

        return itemRepository.findByOwner_IdOrderByIdAsc(userId)
                .stream()
                .map(item -> buildItemDto(item, userId))
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
                .map(item -> buildItemDto(item, null))
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        log.info("Добавление комментария к вещи id={} пользователем id={}",
                itemId, userId);

        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);

        validateComment(commentDto);

        boolean hasFinishedBooking = bookingRepository
                .existsByItem_IdAndBooker_IdAndEndBefore(
                        itemId,
                        userId,
                        LocalDateTime.now()
                );

        if (!hasFinishedBooking) {
            log.warn("Пользователь id={} пытается оставить отзыв без завершённого бронирования вещи id={}",
                    userId, itemId);

            throw new ValidationException(
                    "Оставить отзыв может только пользователь, завершивший бронирование"
            );
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        log.info("Комментарий успешно добавлен. id={}", savedComment.getId());

        return CommentMapper.toCommentDto(savedComment);
    }


    private ItemDto buildItemDto(Item item, Long userId) {
        List<CommentDto> comments = commentRepository
                .findByItem_IdOrderByCreatedAsc(item.getId())
                .stream()
                .map(CommentMapper::toCommentDto)
                .toList();

        BookingDto lastBooking = null;
        BookingDto nextBooking = null;

        if (userId != null && item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            lastBooking = bookingRepository
                    .findFirstByItem_IdAndStatusAndEndBeforeOrderByEndDesc(
                            item.getId(),
                            BookingStatus.APPROVED,
                            now
                    )
                    .map(BookingMapper::toBookingDto)
                    .orElse(null);

            nextBooking = bookingRepository
                    .findFirstByItem_IdAndStatusAndStartAfterOrderByStartAsc(
                            item.getId(),
                            BookingStatus.APPROVED,
                            now
                    )
                    .map(BookingMapper::toBookingDto)
                    .orElse(null);
        }

        return ItemMapper.toItemDto(item, lastBooking, nextBooking, comments);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден. id={}", userId);

                    return new NotFoundException(
                            "Пользователь с id=" + userId + " не найден"
                    );
                });
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь не найдена. id={}", itemId);

                    return new NotFoundException(
                            "Вещь с id=" + itemId + " не найдена"
                    );
                });
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

    private void validateComment(CommentDto commentDto) {
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            log.warn("Попытка добавить пустой комментарий");

            throw new ValidationException(
                    "Текст комментария не может быть пустым"
            );
        }
    }
}