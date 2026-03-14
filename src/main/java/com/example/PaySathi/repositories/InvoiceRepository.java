package com.example.PaySathi.repositories;

import com.example.PaySathi.models.Invoice;
import com.example.PaySathi.models.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice,Long> {
    Optional<Invoice> findByExternalId(String externalId);
    // All invoices for a customer
    List<Invoice> findByCustomerId(Long customerId);

    // All overdue invoices across all customers
    List<Invoice> findByStatus(InvoiceStatus status);

    // All overdue invoices for a specific customer
    List<Invoice> findByCustomerIdAndStatus(Long customerId, InvoiceStatus status);

    // Total outstanding amount for a customer
    @Query("SELECT COALESCE(SUM(i.outstandingAmount), 0) " +
            "FROM Invoice i WHERE i.customer.id = :customerId " +
            "AND i.status != 'PAID'")
    BigDecimal sumOutstandingByCustomerId(@Param("customerId") Long customerId);

    // Count of overdue invoices for a customer
    long countByCustomerIdAndStatus(Long customerId, InvoiceStatus status);
}
