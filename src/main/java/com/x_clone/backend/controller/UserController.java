
package com.x_clone.backend.controller;

import com.x_clone.backend.model.User;
import com.x_clone.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/test")
    public String test() {
        return "El controlador de usuarios funciona correctamente";
    }

    @GetMapping("/{username}")
    public ResponseEntity<Map<String, String>> getUserProfile(
            @PathVariable String username) {

        User user = userService.findByUsername(
                username.trim().toLowerCase());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Usuario no encontrado"));
        }

        return ResponseEntity.ok(Map.of(
                "id", user.getId().toString(),
                "username", user.getUsername(),
                "name", user.getName()));
    }

    @PutMapping("/{username}")
    public ResponseEntity<Map<String, String>> updateUserProfile(
            @PathVariable String username,
            @RequestBody Map<String, String> profileData,
            HttpSession session) {

        Long authenticatedUserId = (Long) session.getAttribute("authenticatedUserId");

        if (authenticatedUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Debes iniciar sesión para editar el perfil"));
        }

        User user = userService.findByUsername(
                username.trim().toLowerCase());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Usuario no encontrado"));
        }

        if (!authenticatedUserId.equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "No puedes editar el perfil de otro usuario"));
        }

        String name = profileData.get("name");

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "El nombre es obligatorio"));
        }

        user.setName(name.trim());
        userService.updateUser(user);

        return ResponseEntity.ok(Map.of(
                "id", user.getId().toString(),
                "username", user.getUsername(),
                "name", user.getName()));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(
            @RequestBody User user) {

        if (user.getUsername() == null || user.getUsername().isBlank()
                || user.getName() == null || user.getName().isBlank()
                || user.getEmail() == null || user.getEmail().isBlank()
                || user.getPassword() == null || user.getPassword().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Todos los campos son obligatorios"));
        }

        if (userService.usernameExists(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "El nombre de usuario ya existe"));
        }

        if (userService.emailExists(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "El correo electrónico ya está registrado"));
        }

        userService.saveUser(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Usuario registrado correctamente"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(
            @RequestBody Map<String, String> credentials,
            HttpSession session) {

        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Ingresa el correo y la contraseña"));
        }

        User user = userService.login(
                email.trim().toLowerCase(),
                password);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Correo o contraseña incorrectos"));
        }

        session.setAttribute("authenticatedUserId", user.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Inicio de sesión correcto",
                "id", user.getId().toString(),
                "name", user.getName(),
                "username", user.getUsername(),
                "email", user.getEmail()));
    }
}
