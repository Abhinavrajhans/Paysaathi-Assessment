package com.example.PaySathi.repositories;

import com.example.PaySathi.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
    Optional<Customer> findByExternalId(String externalId);
}
