package com.website.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.website.Dao.RoleRepository;
import com.website.Dao.UserRepository;
import com.website.dto.UserDTO;
import com.website.entities.Role;
import com.website.entities.User;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private User user;
    private UserDTO userDTO;
    private Role userRole;
    
    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
        userDTO.setName("Sharu");
        userDTO.setEmail("sharu@example.com");
        userDTO.setPassword("password");
        
        user = new User();
        user.setId(1);
        user.setName("Sharu");
        user.setEmail("sharu@example.com");
        user.setPassword("encodedPassword");
        
        userRole = new Role();
        userRole.setId(100);
        userRole.setName("ROLE_USER");
    }
    
    @Test
    void testRegisterUser_Success() {
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(userDTO.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        User registeredUser = userService.registerUser(userDTO);
        
        assertNotNull(registeredUser);
        assertEquals("Sharu", registeredUser.getName());
        assertEquals("sharu@example.com", registeredUser.getEmail());
        assertEquals(1, registeredUser.getId());
    }
    
    @Test
    void testRegisterUser_EmailAlreadyExists() {
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(user);
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(userDTO);
        });
        
        assertEquals("Email already exists!", exception.getMessage());
    }
    
    @Test
    void testFindUserByEmail() {
        when(userRepository.findByEmail("sharu@example.com")).thenReturn(user);
        
        User foundUser = userService.findByEmail("sharu@example.com");
        
        assertNotNull(foundUser);
        assertEquals("sharu@example.com", foundUser.getEmail());
    }
    
    @Test
    void testGetAllUsers() {
        User user2 = new User();
        user2.setId(2);
        user2.setName("Siva");
        user2.setEmail("Siva@gmail.com");
        user2.setPassword("encodedPassword2");
        
        List<User> userList = Arrays.asList(user, user2);
        when(userRepository.findAll()).thenReturn(userList);
        
        List<User> retrievedUsers = userService.getAllUsers();
        
        assertNotNull(retrievedUsers);
        assertEquals(2, retrievedUsers.size());
        assertEquals("Sharu", retrievedUsers.get(0).getName());
        assertEquals("Siva", retrievedUsers.get(1).getName());
    }
}