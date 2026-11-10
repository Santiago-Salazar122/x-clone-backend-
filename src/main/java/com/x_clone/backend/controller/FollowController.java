package com.x_clone.backend.controller;

import com.x_clone.backend.dto.UserFollowDTO;
import com.x_clone.backend.model.Follow;
import com.x_clone.backend.model.User;
import com.x_clone.backend.service.FollowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    @DeleteMapping("/{username}")
    public ResponseEntity<Map<String, String>> unfollowUser(
            @PathVariable String username,
            HttpServletRequest request) {

        Long followerId = (Long) request.getAttribute("authenticatedUserId");

        if (followerId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Debes iniciar sesión"));
        }

        try {
            followService.unfollowUser(followerId, username);

            return ResponseEntity.ok(
                    Map.of("message", "Has dejado de seguir a " + username));

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

    @GetMapping("/{username}/followers")
    public ResponseEntity<Page<UserFollowDTO>> getFollowers(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Page<Follow> follows = followService.getFollowers(
                    username,
                    PageRequest.of(page, size));

            Page<UserFollowDTO> users = follows.map(follow -> convertToDTO(follow.getFollower()));

            return ResponseEntity.ok(users);

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{username}/following")
    public ResponseEntity<Page<UserFollowDTO>> getFollowing(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Page<Follow> follows = followService.getFollowing(
                    username,
                    PageRequest.of(page, size));

            Page<UserFollowDTO> users = follows.map(follow -> convertToDTO(follow.getFollowed()));

            return ResponseEntity.ok(users);

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    private UserFollowDTO convertToDTO(User user) {
        return new UserFollowDTO(
                user.getId(),
                user.getUsername(),
                user.getName());
    }
}