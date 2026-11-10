
package com.x_clone.backend.repository;

import com.x_clone.backend.model.Follow;
import com.x_clone.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {

        boolean existsByFollowerAndFollowed(User follower, User followed);

        long countByFollower(User follower);

        long countByFollowed(User followed);

        List<Follow> findByFollower(User follower);

        List<Follow> findByFollowed(User followed);

        Page<Follow> findByFollowed(User followed, Pageable pageable);

        Page<Follow> findByFollower(User follower, Pageable pageable);

        @Query("SELECT f FROM Follow f WHERE f.followed = :user ORDER BY LOWER(f.follower.name) ASC")
        Page<Follow> findFollowersOrderedByName(
                        @Param("user") User user,
                        Pageable pageable);

        @Query("SELECT f FROM Follow f WHERE f.follower = :user ORDER BY LOWER(f.followed.name) ASC")
        Page<Follow> findFollowingOrderedByName(
                        @Param("user") User user,
                        Pageable pageable);

        void deleteByFollowerAndFollowed(User follower, User followed);
}
