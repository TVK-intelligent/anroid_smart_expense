package com.example.smart_expense.controller;

import com.example.smart_expense.model.User;
import com.example.smart_expense.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        Optional<User> existing = userRepository.findByEmail(user.getEmail());
        if (existing.isPresent()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Email đã tồn tại!");
            return ResponseEntity.badRequest().body(err);
        }

        // Just hash/store password directly
        user.setPasswordHash(user.getPasswordHash());
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    /**
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Email hoặc mật khẩu không chính xác!");
            return ResponseEntity.badRequest().body(err);
        }

        User user = userOpt.get();
        if (!password.equals(user.getPasswordHash())) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Email hoặc mật khẩu không chính xác!");
            return ResponseEntity.badRequest().body(err);
        }

        return ResponseEntity.ok(user);
    }
}
