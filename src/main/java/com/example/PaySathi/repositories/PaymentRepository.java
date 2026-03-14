package com.example.PaySathi.repositories;

import com.example.PaySathi.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
}
