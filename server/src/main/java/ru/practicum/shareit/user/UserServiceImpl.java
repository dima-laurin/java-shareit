package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.SameEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        log.info("Создание пользователя: {}", userDto);

        validateEmail(userDto.getEmail());

        if (userRepository.existsByEmail(userDto.getEmail())) {
            log.warn("Попытка создать пользователя с существующим email: {}",
                    userDto.getEmail());

            throw new SameEmailException("Email уже используется");
        }

        ru.practicum.shareit.user.User user = UserMapper.toUser(userDto);
        ru.practicum.shareit.user.User savedUser = userRepository.save(user);

        log.info("Пользователь успешно создан. id={}", savedUser.getId());

        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        log.info("Обновление пользователя. id={}, данные={}", userId, userDto);

        ru.practicum.shareit.user.User user = getUserOrThrow(userId);

        if (userDto.getEmail() != null) {
            validateEmail(userDto.getEmail());

            if (userRepository.existsByEmailAndIdNot(userDto.getEmail(), userId)) {
                log.warn("Попытка установить занятый email {} для пользователя {}", userDto.getEmail(), userId);

                throw new SameEmailException("Email уже используется");
            }

            user.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }

        ru.practicum.shareit.user.User updatedUser = userRepository.save(user);

        log.info("Пользователь успешно обновлён. id={}", userId);

        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto getById(Long userId) {
        log.info("Получение пользователя по id={}", userId);

        ru.practicum.shareit.user.User user = getUserOrThrow(userId);

        return UserMapper.toUserDto(user);
    }

    @Override
    public Collection<UserDto> getAll() {
        log.info("Получение списка всех пользователей");

        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long userId) {
        log.info("Удаление пользователя id={}", userId);

        getUserOrThrow(userId);

        userRepository.deleteById(userId);

        log.info("Пользователь удалён. id={}", userId);
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

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            log.warn("Получен пустой email");

            throw new ValidationException(
                    "Email не может быть пустым"
            );
        }

        if (!email.contains("@")) {
            log.warn("Получен некорректный email: {}", email);

            throw new ValidationException("Некорректный формат email");
        }
    }

}