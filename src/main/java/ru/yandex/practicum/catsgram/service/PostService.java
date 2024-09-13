package ru.yandex.practicum.catsgram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.model.User;
import java.time.Instant;
import java.util.*;

@Service
public class PostService {

    private final Map<Long, Post> posts = new HashMap<>();
    private final UserService userService;

    @Autowired
    public PostService(UserService userService) {
        this.userService = userService;
    }

    public Collection<Post> findAll(String sort, int size, int from) {
        return posts.values()
                .stream()
                .sorted((post0, post1) -> {
                    SortOrder sortOrder = SortOrder.from(sort);
                    int compare = post0.getPostDate().compareTo(post1.getPostDate());
                    if (sortOrder != null && sortOrder.equals(SortOrder.ASCENDING)) {
                        return compare;
                    } else if (sortOrder != null && sortOrder.equals(SortOrder.DESCENDING)) {
                        return compare = -1 * compare;
                    }
                    throw new ConditionsNotMetException("Не верно введен параметр строки запроса для сортировки " +
                            "списка публикаций по дате создания");
                })
                .skip(Long.parseLong(String.valueOf(from)))
                .limit(Long.parseLong(String.valueOf(size)))
                .toList();
    }

    public Post create(Post post) {
            if (post.getDescription() == null || post.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            Optional<User> optionalUser = userService.findUserById(post.getAuthorId());
            if (optionalUser.isEmpty()) {
                throw new ConditionsNotMetException(String.format("Автор с id = %d не найден", post.getAuthorId()));
            }
            post.setId(getNextId());
            post.setPostDate(Instant.now());
            posts.put(post.getId(), post);
            return post;
    }

    public Post update(Post newPost) {
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());
            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }
        throw new NotFoundException(String.format("Пост с id = %s не найден", newPost.getId()));
    }

    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public Optional<Post> findPostById(long id) {
        return posts.values()
                .stream()
                .filter(post -> post.getId() == id)
                .findFirst();
    }

    public enum SortOrder {
        ASCENDING, DESCENDING;

        public static SortOrder from(String order) {
            return switch (order.toLowerCase()) {
                case "ascending", "asc" -> ASCENDING;
                case "descending", "desc" -> DESCENDING;
                default -> null;
            };
        }
    }
}