
package com.x_clone.backend.repository;

import com.x_clone.backend.model.Follow;
import com.x_clone.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerAndFollowed(User follower, User followed);

    long countByFollower(User follower);

    long countByFollowed(User followed);

    List<Follow> findByFollower(User follower);

    List<Follow> findByFollowed(User followed);
}
