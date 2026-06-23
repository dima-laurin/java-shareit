package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1L;

    @Override
    public User save(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getById(Long userId) {
        return users.get(userId);
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public void deleteById(Long userId) {
        users.remove(userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return users.values()
                .stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Long userId) {
        return users.values()
                .stream()
                .anyMatch(user -> email.equals(user.getEmail())
                        && !user.getId().equals(userId));
    }
}
