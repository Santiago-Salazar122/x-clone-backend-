
package com.x_clone.backend.repository;

import com.x_clone.backend.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByOrderByCreatedAtDesc();

    List<Post> findByUserUsernameOrderByCreatedAtDesc(String username);
}
