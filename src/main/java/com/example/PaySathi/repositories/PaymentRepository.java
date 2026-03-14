package com.example.PaySathi.repositories;

import com.example.PaySathi.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByExternalId(String externalId);
}
