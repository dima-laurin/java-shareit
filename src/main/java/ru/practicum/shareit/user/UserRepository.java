package ru.practicum.shareit.user;

import java.util.Collection;

public interface UserRepository {

    User save(User user);

    User update(User user);

    User getById(Long userId);

    Collection<User> getAll();

    void deleteById(Long userId);
}
