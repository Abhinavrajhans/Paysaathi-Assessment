package com.example.PaySathi.mapper;

import com.example.PaySathi.dto.CustomerDTO;
import com.example.PaySathi.models.Customer;
import java.time.LocalDateTime;

public class CustomerMapper {

    // DTO → new Model (for inserts)
    public static Customer toEntity(CustomerDTO dto) {
        return Customer.builder()
                .externalId(dto.getExternalId())
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .syncedAt(LocalDateTime.now())
                .build();
    }

    // DTO → existing Model (for updates)
    public static void updateEntity(CustomerDTO dto, Customer existing) {
        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());
        existing.setAddress(dto.getAddress());
        existing.setSyncedAt(LocalDateTime.now());
    }

    public static void mapToEntity(CustomerDTO dto, Customer customer) {
        customer.setExternalId(dto.getExternalId());
        updateEntity(dto, customer);
    }

}