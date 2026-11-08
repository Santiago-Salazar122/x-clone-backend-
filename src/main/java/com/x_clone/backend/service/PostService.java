
package com.x_clone.backend.service;

import com.x_clone.backend.model.Post;
import com.x_clone.backend.model.User;
import com.x_clone.backend.repository.PostRepository;
import com.x_clone.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(
            PostRepository postRepository,
            UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public Post createPost(String content, Long userId) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "El contenido de la publicación es obligatorio");
        }

        if (content.trim().length() > 280) {
            throw new IllegalArgumentException(
                    "La publicación no puede superar los 280 caracteres");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario no encontrado"));

        Post post = new Post();
        post.setContent(content.trim());
        post.setUser(user);

        return postRepository.save(post);
    }

    public Page<Post> getAllPosts(int page, int size) {
        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")));

        return postRepository.findAll(pageable);
    }

    public List<Post> getPostsByUsername(String username) {
        return postRepository
                .findByUserUsernameOrderByCreatedAtDesc(username);
    }
}
