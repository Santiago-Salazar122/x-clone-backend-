
package com.x_clone.backend.controller;

import com.x_clone.backend.model.Follow;
import com.x_clone.backend.service.FollowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{username}")
    public ResponseEntity<Map<String, String>> followUser(
            @PathVariable String username,
            HttpServletRequest request) {

        Long followerId = (Long) request.getAttribute("authenticatedUserId");

        if (followerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Debes iniciar sesión"));
        }

        try {
            Follow follow = followService.followUser(followerId, username);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "message", "Ahora sigues a " + username,
                            "createdAt", follow.getCreatedAt().toString()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", exception.getMessage()));
        }
    }

    @GetMapping("/{username}/followers/count")
    public ResponseEntity<Map<String, Long>> getFollowersCount(
            @PathVariable String username) {
        try {
            return ResponseEntity.ok(Map.of(
                    "count", followService.getFollowersCount(username)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of());
        }
    }

    @GetMapping("/{username}/following/count")
    public ResponseEntity<Map<String, Long>> getFollowingCount(
            @PathVariable String username) {
        try {
            return ResponseEntity.ok(Map.of(
                    "count", followService.getFollowingCount(username)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of());
        }
    }

    @GetMapping("/{username}/is-following")
    public ResponseEntity<Map<String, Boolean>> isFollowing(
            @PathVariable String username,
            HttpServletRequest request) {

        Long followerId = (Long) request.getAttribute("authenticatedUserId");

        if (followerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("isFollowing", false));
        }

        return ResponseEntity.ok(Map.of(
                "isFollowing",
                followService.isFollowing(followerId, username)));
    }
}
