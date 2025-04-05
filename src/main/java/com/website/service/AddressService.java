package com.website.service;

import com.website.Dao.AddressRepository;
import com.website.Dao.UserRepository;
import com.website.dto.AddressDTO;
import com.website.entities.Address;
import com.website.entities.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

    private static final Logger logger = LoggerFactory.getLogger(AddressService.class);

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    public Address saveAddress(AddressDTO addressDTO) {
        try {
            logger.info("Attempting to save address for user ID: {}", addressDTO.getUserId());

            User user = userRepository.findById(addressDTO.getUserId())
                    .orElseThrow(() -> {
                        logger.warn("User not found with ID: {}", addressDTO.getUserId());
                        return new RuntimeException("User not found");
                    });

            Address address = new Address();
            address.setStreet(addressDTO.getStreet());
            address.setCity(addressDTO.getCity());
            address.setState(addressDTO.getState());
            address.setZipCode(addressDTO.getZipCode());
            address.setCountry(addressDTO.getCountry());
            address.setUser(user);

            Address savedAddress = addressRepository.save(address);
            logger.info("Address saved successfully for user ID: {}", user.getId());

            return savedAddress;

        } catch (Exception e) {
            logger.error("Error while saving address: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save address");
        }
    }
}
