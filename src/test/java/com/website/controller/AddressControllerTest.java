package com.website.controller;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.website.Controller.AddressController;
import com.website.dto.AddressDTO;
import com.website.entities.Address;
import com.website.service.AddressService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;



@WebMvcTest(AddressController.class)  
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddressService addressService;  

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testSaveAddressWithAuthenticatedUser() throws Exception {
        // Mock AddressDTO
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setStreet("South Street");
        addressDTO.setCity("Coimbatore");
        addressDTO.setState("TamilNadu");
        addressDTO.setZipCode("62704");
        addressDTO.setCountry("India");
        addressDTO.setUserId(1);

        // Mock saved Address response
        Address savedAddress = new Address();
        savedAddress.setStreet("South Street");
        savedAddress.setCity("Coimbatore");
        savedAddress.setState("TamilNadu");
        savedAddress.setZipCode("62704");
        savedAddress.setCountry("India");

        when(addressService.saveAddress(Mockito.any(AddressDTO.class))).thenReturn(savedAddress);

        mockMvc.perform(post("/api/addresses/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addressDTO))
                .with(csrf())) 
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.street").value("South Street"))  
                .andExpect(jsonPath("$.city").value("Coimbatore"));
    }


    @Test
    void testSaveAddressWithoutAuthentication() throws Exception {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setStreet("123 Main St");
        addressDTO.setCity("Springfield");
        addressDTO.setState("IL");
        addressDTO.setZipCode("62704");
        addressDTO.setCountry("USA");
        addressDTO.setUserId(1);

        mockMvc.perform(post("/api/addresses/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addressDTO))
        		.with(csrf()))
                .andExpect(status().isUnauthorized());  
    }
}

