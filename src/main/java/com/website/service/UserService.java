package com.website.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.website.Dao.RoleRepository;
import com.website.Dao.UserRepository;
import com.website.dto.UserDTO;
import com.website.entities.Role;
import com.website.entities.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User findUser(String email) {
        logger.info("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    public User registerUser(UserDTO userDTO) {
        logger.info("Registering user with email: {}", userDTO.getEmail());

        if (userRepository.findByEmail(userDTO.getEmail()) != null) {
            logger.warn("Registration failed: Email {} already exists", userDTO.getEmail());
            throw new RuntimeException("Email already exists!");
        }

        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> {
                logger.error("Role not found: ROLE_USER");
                return new RuntimeException("Role not found");
            });

        user.setRole(Collections.singleton(userRole));
        User savedUser = userRepository.save(user);

        logger.info("User registered successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    public User findByEmail(String email) {
        logger.debug("Searching for user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        List<User> users = userRepository.findAll();
        logger.debug("Total users found: {}", users.size());
        return users;
    }
}
