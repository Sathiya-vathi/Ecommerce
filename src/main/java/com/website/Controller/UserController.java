package com.website.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.website.dto.LoginRequest;
import com.website.dto.UserDTO;
import com.website.entities.User;
import com.website.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) {
        try {
        	logger.info("Attempting to register user with email: {}", userDTO.getEmail());
            User user = userService.registerUser(userDTO);
            logger.info("User registered successfully with ID: {}", user.getId());
            return ResponseEntity.ok("User registered successfully with ID: " + user.getId());
        } catch (RuntimeException e) {
        	logger.error("Error registering user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody LoginRequest loginRequest) {
    	logger.info("Login attempt for email: {}", loginRequest.getEmail());
        User user = userService.findByEmail(loginRequest.getEmail());
        
        // Check if user exists and verify the password using BCrypt
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
        	logger.warn("Invalid login attempt for email: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        logger.info("Login successful for user: {}", user.getEmail());
        return ResponseEntity.ok("Login successful!");
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
    	logger.info("Fetching all users");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

}
