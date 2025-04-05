package com.website.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.website.Dao.CartRepository;
import com.website.Dao.OrderRepository;
import com.website.Dao.RoleRepository;
import com.website.Dao.UserRepository;
import com.website.dto.LoginRequest;
import com.website.dto.UserDTO;
import com.website.entities.Role;
import com.website.entities.User;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private User normalUser;
    
    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        adminRole = roleRepository.save(adminRole);

        Role userRole = new Role();
        userRole.setName("ROLE_USER");
        userRole = roleRepository.save(userRole);

        adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("adminpass"));
        adminUser.setRole(new HashSet<>(roleRepository.findAll()));
        userRepository.save(adminUser);

        normalUser = new User();
        normalUser.setName("Regular User");
        normalUser.setEmail("user@example.com");
        normalUser.setPassword(passwordEncoder.encode("userpass"));
        normalUser.setRole(new HashSet<>(roleRepository.findAll()));
        userRepository.save(normalUser);
    }

    @Test
    void testRegisterUser() throws Exception {
        UserDTO newUser = new UserDTO();
        newUser.setName("Test User");
        newUser.setEmail("testuser@example.com");
        newUser.setPassword("testpass");

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk());
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@example.com", "adminpass");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testLogin_Failure_InvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@example.com", "wrongpassword");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetAllUsers_AsAdmin() throws Exception {
        mockMvc.perform(get("/api/users/all")
                .with(httpBasic("admin@example.com", "adminpass")))
                .andExpect(status().isOk());
    }

}
