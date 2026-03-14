package com.example.PaySathi.repositories;

import com.example.PaySathi.models.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice,Long> {
    Optional<Invoice> findByExternalId(String externalId);
}
