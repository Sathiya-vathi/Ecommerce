package com.website.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.website.Dao.AddressRepository;
import com.website.Dao.UserRepository;
import com.website.dto.AddressDTO;
import com.website.entities.Address;
import com.website.entities.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressService addressService;

    private AddressDTO addressDTO;
    private User user;
    private Address address;

    @BeforeEach
    void setUp() {
        addressDTO = new AddressDTO();
        addressDTO.setUserId((int) 1L);
        addressDTO.setStreet("123 Main St");
        addressDTO.setCity("Springfield");
        addressDTO.setState("IL");
        addressDTO.setZipCode("62704");
        addressDTO.setCountry("USA");

        user = new User();
        user.setId((int) 1L);

        address = new Address();
        address.setId(100L);
        address.setStreet(addressDTO.getStreet());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setZipCode(addressDTO.getZipCode());
        address.setCountry(addressDTO.getCountry());
        address.setUser(user);
    }

    @Test
    void testSaveAddress_Success() {
        when(userRepository.findById((int) 1L)).thenReturn(Optional.of(user));
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        Address saved = addressService.saveAddress(addressDTO);

        assertNotNull(saved);
        assertEquals("123 Main St", saved.getStreet());
        assertEquals("USA", saved.getCountry());
        assertEquals(1L, saved.getUser().getId());
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void testSaveAddress_UserNotFound() {
        when(userRepository.findById((int) 1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            addressService.saveAddress(addressDTO);
        });

        assertEquals("Failed to save address", exception.getMessage());
        verify(addressRepository, never()).save(any(Address.class));
    }
}
