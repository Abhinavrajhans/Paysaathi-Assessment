package com.example.PaySathi.repositories;

import com.example.PaySathi.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
    Optional<Customer> findByExternalId(String externalId);

    // Customers who have at least one overdue invoice
    // Used for the global overdue report
    @Query("SELECT DISTINCT i.customer FROM Invoice i WHERE i.status = 'OVERDUE'")
    List<Customer> findCustomersWithOverdueInvoices();
}
