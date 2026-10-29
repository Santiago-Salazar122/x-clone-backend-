
package com.x_clone.backend.repository;

import com.x_clone.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User findByEmail(String email);

    User findByUsername(String username);
    
    java.util.List<User> findByUsernameContainingIgnoreCase(String username);

    java.util.List<User> findByNameContainingIgnoreCase(String name);

}
