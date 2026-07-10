package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody UserDto userDto) {
        log.info("Gateway: создание пользователя");

        validateUserForCreate(userDto);

        return userClient.create(userDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(@PathVariable Long userId,
                                         @RequestBody UserDto userDto) {
        log.info("Gateway: обновление пользователя id={}", userId);

        validateUserForUpdate(userDto);

        return userClient.update(userId, userDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getById(@PathVariable Long userId) {
        log.info("Gateway: получение пользователя id={}", userId);

        return userClient.getById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("Gateway: получение всех пользователей");

        return userClient.getAll();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> delete(@PathVariable Long userId) {
        log.info("Gateway: удаление пользователя id={}", userId);

        return userClient.delete(userId);
    }

    private void validateUserForCreate(UserDto userDto) {
        if (userDto == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new ValidationException("Имя пользователя не может быть пустым");
        }

        validateEmail(userDto.getEmail());
    }

    private void validateUserForUpdate(UserDto userDto) {
        if (userDto == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        if (userDto.getName() != null && userDto.getName().isBlank()) {
            throw new ValidationException("Имя пользователя не может быть пустым");
        }

        if (userDto.getEmail() != null) {
            validateEmail(userDto.getEmail());
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }

        if (!email.contains("@")) {
            throw new ValidationException("Некорректный формат email");
        }
    }
}
