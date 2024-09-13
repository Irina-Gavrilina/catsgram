package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

@Service
public class UserService {

    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAll() {
        return users.values();
    }

    public User create(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }
        if (isEmailAlreadyExists(user.getEmail())) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());
        users.put(user.getId(), user);
        return user;
    }

    public User update(User newUser) {
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (newUser.getEmail() != null && isEmailAlreadyExists(newUser.getEmail())) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (newUser.getEmail() == null) {
                newUser.setEmail(oldUser.getEmail());
            } else if (newUser.getUsername() == null) {
                newUser.setUsername(oldUser.getUsername());
            } else if (newUser.getPassword() == null) {
                newUser.setPassword(oldUser.getPassword());
            }
            users.put(newUser.getId(), newUser);
            return newUser;
        }
        throw new NotFoundException(String.format("Пользователь с id = %s не найден", newUser.getId()));
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private boolean isEmailAlreadyExists(String currentEmail) {
        return users.values()
                .stream()
                .map(User::getEmail)
                .anyMatch(email -> email.equals(currentEmail));
    }

    public Optional<User> findUserById(long id) {
        return users.values()
                .stream()
                .filter(user -> user.getId() == id)
                .findFirst();
    }
}