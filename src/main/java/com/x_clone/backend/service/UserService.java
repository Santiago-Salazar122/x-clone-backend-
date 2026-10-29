
package com.x_clone.backend.service;

import com.x_clone.backend.model.User;
import com.x_clone.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User saveUser(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        return userRepository.save(user);
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            return null;
        }

        boolean passwordMatches = passwordEncoder.matches(
                password,
                user.getPassword());

        if (!passwordMatches) {
            return null;
        }

        return user;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> searchUsers(String query) {
        List<User> usersByUsername = userRepository.findByUsernameContainingIgnoreCase(query);

        List<User> usersByName = userRepository.findByNameContainingIgnoreCase(query);

        usersByName.removeIf(user -> usersByUsername.stream()
                .anyMatch(existingUser -> existingUser.getId().equals(user.getId())));

        usersByUsername.addAll(usersByName);

        return usersByUsername;
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
