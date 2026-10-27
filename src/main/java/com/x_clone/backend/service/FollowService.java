
package com.x_clone.backend.service;

import com.x_clone.backend.model.Follow;
import com.x_clone.backend.model.User;
import com.x_clone.backend.repository.FollowRepository;
import com.x_clone.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowService(
            FollowRepository followRepository,
            UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Follow followUser(Long followerId, String followedUsername) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario autenticado no encontrado"));

        User followed = userRepository.findByUsername(
                followedUsername.trim().replaceFirst("^@", "").toLowerCase());

        if (followed == null) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        if (follower.getId().equals(followed.getId())) {
            throw new IllegalArgumentException(
                    "No puedes seguirte a ti mismo");
        }

        if (followRepository.existsByFollowerAndFollowed(follower, followed)) {
            throw new IllegalArgumentException(
                    "Ya sigues a este usuario");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        follow.setCreatedAt(LocalDateTime.now());

        return followRepository.save(follow);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, String followedUsername) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario autenticado no encontrado"));

        User followed = userRepository.findByUsername(
                followedUsername.trim().replaceFirst("^@", "").toLowerCase());

        return followed != null
                && followRepository.existsByFollowerAndFollowed(follower, followed);
    }

    @Transactional(readOnly = true)
    public long getFollowersCount(String username) {
        User user = userRepository.findByUsername(
                username.trim().replaceFirst("^@", "").toLowerCase());

        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        return followRepository.countByFollowed(user);
    }

    @Transactional(readOnly = true)
    public long getFollowingCount(String username) {
        User user = userRepository.findByUsername(
                username.trim().replaceFirst("^@", "").toLowerCase());

        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        return followRepository.countByFollower(user);
    }

    @Transactional(readOnly = true)
    public List<Follow> getFollowers(String username) {
        User user = userRepository.findByUsername(
                username.trim().replaceFirst("^@", "").toLowerCase());

        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        return followRepository.findByFollowed(user);
    }

    @Transactional(readOnly = true)
    public List<Follow> getFollowing(String username) {
        User user = userRepository.findByUsername(
                username.trim().replaceFirst("^@", "").toLowerCase());

        if (user == null) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        return followRepository.findByFollower(user);
    }
}
