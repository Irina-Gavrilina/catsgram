package ru.yandex.practicum.catsgram.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Collection;
import java.util.Optional;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;
import ru.yandex.practicum.catsgram.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        return userService.update(newUser);
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable("userId") long userId) {
        Optional<User> optionalUser = userService.findUserById(userId);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }
        throw new NotFoundException(String.format("Пользователь с id = %d не найден", userId));
    }
}