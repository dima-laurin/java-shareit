package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.RequestItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestDto requestDto) {
        log.info("Создание запроса вещи пользователем id={}", userId);

        User requestor = getUserOrThrow(userId);

        validateRequest(requestDto);

        ru.practicum.shareit.request.ItemRequest request = new ru.practicum.shareit.request.ItemRequest();
        request.setDescription(requestDto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ru.practicum.shareit.request.ItemRequest savedRequest = itemRequestRepository.save(request);

        log.info("Запрос вещи успешно создан. id={}", savedRequest.getId());

        return buildItemRequestDto(savedRequest);
    }

    @Override
    public Collection<ItemRequestDto> getUserRequests(Long userId) {
        log.info("Получение запросов пользователя id={}", userId);

        getUserOrThrow(userId);

        return itemRequestRepository.findByRequestor_IdOrderByCreatedDesc(userId)
                .stream()
                .map(this::buildItemRequestDto)
                .toList();
    }

    @Override
    public Collection<ItemRequestDto> getAllRequests(Long userId) {
        log.info("Получение запросов других пользователей для userId={}", userId);

        getUserOrThrow(userId);

        return itemRequestRepository.findByRequestor_IdNotOrderByCreatedDesc(userId)
                .stream()
                .map(this::buildItemRequestDto)
                .toList();
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        log.info("Получение запроса id={} пользователем id={}", requestId, userId);

        getUserOrThrow(userId);

        ru.practicum.shareit.request.ItemRequest request = getRequestOrThrow(requestId);

        return buildItemRequestDto(request);
    }

    private ItemRequestDto buildItemRequestDto(ru.practicum.shareit.request.ItemRequest request) {
        List<RequestItemDto> items = itemRepository
                .findByRequest_IdOrderByIdAsc(request.getId())
                .stream()
                .map(ItemRequestMapper::toRequestItemDto)
                .toList();

        return ItemRequestMapper.toItemRequestDto(request, items);
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

    private ItemRequest getRequestOrThrow(Long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Запрос вещи не найден. id={}", requestId);

                    return new NotFoundException(
                            "Запрос вещи с id=" + requestId + " не найден"
                    );
                });
    }

    private void validateRequest(ItemRequestDto requestDto) {
        if (requestDto.getDescription() == null
                || requestDto.getDescription().isBlank()) {
            log.warn("Попытка создать запрос вещи без описания");

            throw new ValidationException(
                    "Описание запроса не может быть пустым"
            );
        }
    }
}
