package ru.practicum.shareit.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.SameEmailException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void create_shouldSaveUserToDatabase() {
        UserDto created = userService.create(new UserDto(null, "User", "user@mail.com"));

        assertThat(created.getId(), notNullValue());

        User saved = userRepository.findById(created.getId()).orElseThrow();
        assertThat(saved.getName(), equalTo("User"));
        assertThat(saved.getEmail(), equalTo("user@mail.com"));
    }

    @Test
    void update_shouldPatchOnlyProvidedFieldsInDatabase() {
        User user = saveUser("Old name", "old@mail.com");

        UserDto updated = userService.update(
                user.getId(),
                new UserDto(null, null, "new@mail.com")
        );

        assertThat(updated.getName(), equalTo("Old name"));
        assertThat(updated.getEmail(), equalTo("new@mail.com"));

        User saved = userRepository.findById(user.getId()).orElseThrow();
        assertThat(saved.getName(), equalTo("Old name"));
        assertThat(saved.getEmail(), equalTo("new@mail.com"));
    }

    @Test
    void getById_shouldReturnUserFromDatabase() {
        User user = saveUser("User", "user@mail.com");

        UserDto found = userService.getById(user.getId());

        assertThat(found.getId(), equalTo(user.getId()));
        assertThat(found.getName(), equalTo("User"));
        assertThat(found.getEmail(), equalTo("user@mail.com"));
    }

    @Test
    void getAll_shouldReturnAllUsersFromDatabase() {
        saveUser("First", "first@mail.com");
        saveUser("Second", "second@mail.com");

        Collection<UserDto> users = userService.getAll();
        List<String> emails = users.stream()
                .map(UserDto::getEmail)
                .collect(Collectors.toList());

        assertThat(users.size(), equalTo(2));
        assertThat(emails, containsInAnyOrder("first@mail.com", "second@mail.com"));
    }

    @Test
    void delete_shouldRemoveUserFromDatabase() {
        User user = saveUser("User", "user@mail.com");

        userService.delete(user.getId());

        assertThat(userRepository.findById(user.getId()).isEmpty(), equalTo(true));
        assertThrows(NotFoundException.class, () -> userService.getById(user.getId()));
    }

    @Test
    void create_shouldThrowValidationExceptionWhenEmailIsBlank() {
        UserDto userDto = new UserDto(null, "User", "");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.create(userDto)
        );

        assertThat(exception.getMessage(), equalTo("Email не может быть пустым"));
    }

    @Test
    void create_shouldThrowSameEmailExceptionWhenEmailAlreadyExists() {
        saveUser("First", "user@mail.com");

        UserDto userDto = new UserDto(null, "Second", "user@mail.com");

        SameEmailException exception = assertThrows(
                SameEmailException.class,
                () -> userService.create(userDto)
        );

        assertThat(exception.getMessage(), equalTo("Email уже используется"));
    }

    @Test
    void update_shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
        UserDto userDto = new UserDto(null, "New name", "new@mail.com");

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.update(999L, userDto)
        );

        assertThat(exception.getMessage(), equalTo("Пользователь с id=999 не найден"));
    }

    private User saveUser(String name, String email) {
        return userRepository.save(new User(null, name, email));
    }
}

