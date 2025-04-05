package com.website.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.website.dto.AddressDTO;
import com.website.entities.Address;
import com.website.service.AddressService;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    
    @Autowired
    private AddressService addressService;
    
    @PostMapping("/save")
    public ResponseEntity<Address> saveAddress(@RequestBody AddressDTO addressDTO) {
        Address savedAddress = addressService.saveAddress(addressDTO);
        return ResponseEntity.ok(savedAddress);
    }
}
