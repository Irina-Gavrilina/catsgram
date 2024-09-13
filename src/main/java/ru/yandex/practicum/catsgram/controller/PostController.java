package ru.yandex.practicum.catsgram.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.service.PostService;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public Collection<Post> findAll(@RequestParam(defaultValue = "asc") String sort,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(defaultValue = "0") int from) {
        return postService.findAll(sort, size, from);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post create(@RequestBody Post post) {
        return postService.create(post);
    }

    @PutMapping
    public Post update(@RequestBody Post newPost) {
        return postService.update(newPost);
    }

    @GetMapping("/{postId}")
    public Post getPostById(@PathVariable("postId") long postId) {
        Optional<Post> optionalPost = postService.findPostById(postId);
        if (optionalPost.isPresent()) {
            return optionalPost.get();
        }
        throw new NotFoundException(String.format("Пост с id = %d не найден", postId));
    }
}