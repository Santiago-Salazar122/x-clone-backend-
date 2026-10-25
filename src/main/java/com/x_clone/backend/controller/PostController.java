
package com.x_clone.backend.controller;

import com.x_clone.backend.model.Post;
import com.x_clone.backend.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<?> createPost(
            @RequestBody Map<String, String> requestBody,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("authenticatedUserId");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Debes iniciar sesión para publicar"));
        }

        try {
            String content = requestBody.get("content");
            Post post = postService.createPost(content, userId);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "id", post.getId(),
                            "content", post.getContent(),
                            "createdAt", post.getCreatedAt().toString(),
                            "user", Map.of(
                                    "id", post.getUser().getId(),
                                    "username", post.getUser().getUsername(),
                                    "name", post.getUser().getName())));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", exception.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllPosts() {
        List<Map<String, Object>> posts = postService.getAllPosts()
                .stream()
                .map(this::toPostResponse)
                .toList();

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<Map<String, Object>>> getPostsByUsername(
            @PathVariable String username) {

        List<Map<String, Object>> posts = postService
                .getPostsByUsername(username.trim().toLowerCase())
                .stream()
                .map(this::toPostResponse)
                .toList();

        return ResponseEntity.ok(posts);
    }

    private Map<String, Object> toPostResponse(Post post) {
        return Map.of(
                "id", post.getId(),
                "content", post.getContent(),
                "createdAt", post.getCreatedAt().toString(),
                "user", Map.of(
                        "id", post.getUser().getId(),
                        "username", post.getUser().getUsername(),
                        "name", post.getUser().getName()));
    }
}
