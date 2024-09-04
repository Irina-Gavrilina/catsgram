package ru.yandex.practicum.catsgram.controller;

import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }
        if (isEmailAlreadyExists(user.getEmail())) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        user.setId(getNextIdForUser());
        user.setRegistrationDate(Instant.now());
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
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

    private long getNextIdForUser() {
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
}