package com.personal.directview.controller;

import com.personal.directview.dto.AddressRequest;
import com.personal.directview.service.AddressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    // Endpoint to get address components as key-value pairs
    @GetMapping("/get-raw-addr")
    public Map<String, Object> getRawAddress(@RequestBody AddressRequest addressRequest) {
        String address = addressRequest.getAddress();
        return addressService.getRawAddressData(address);
    }

    @GetMapping("/get-lat-long")
    public Map<String, Object> getLatLong(@RequestBody AddressRequest addressRequest) {
        String address = addressRequest.getAddress();
        return addressService.getLatLong(address);
    }

    @GetMapping("/get-addr-comp")
    public Map<String, String> getAddressComponents(@RequestBody AddressRequest addressRequest) {
        String address = addressRequest.getAddress();
        return addressService.getAddressComponents(address);
    }

}
