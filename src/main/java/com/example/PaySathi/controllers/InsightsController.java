package com.example.PaySathi.controllers;


import com.example.PaySathi.dto.CustomerCreditResponse;
import com.example.PaySathi.dto.CustomerSummaryResponse;
import com.example.PaySathi.dto.OverdueInvoiceResponse;
import com.example.PaySathi.services.CustomerService;
import com.example.PaySathi.services.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightsController {

    private final CustomerService customerService;
    private final InvoiceService invoiceService;

    // Summary of a single customer — balances, invoice counts
    @GetMapping("/customers/{externalId}/summary")
    public ResponseEntity<CustomerSummaryResponse> getCustomerSummary(
            @PathVariable String externalId) {
        log.info("GET /api/insights/customers/{}/summary", externalId);
        return ResponseEntity.ok(customerService.getCustomerSummary(externalId));
    }

    // Credit risk profile — outstanding amount, overdue invoices, risk level
    @GetMapping("/customers/{externalId}/credit-profile")
    public ResponseEntity<CustomerCreditResponse> getCustomerCreditProfile(
            @PathVariable String externalId) {
        log.info("GET /api/insights/customers/{}/credit-profile", externalId);
        return ResponseEntity.ok(customerService.getCustomerCreditProfile(externalId));
    }

    // All customers with their summaries — good for a dashboard view
    @GetMapping("/customers")
    public ResponseEntity<List<CustomerSummaryResponse>> getAllCustomerSummaries() {
        log.info("GET /api/insights/customers");
        return ResponseEntity.ok(customerService.getAllCustomerSummaries());
    }

    // All overdue invoices across all customers
    @GetMapping("/overdue")
    public ResponseEntity<List<OverdueInvoiceResponse>> getAllOverdueInvoices() {
        log.info("GET /api/insights/overdue");
        return ResponseEntity.ok(invoiceService.getAllOverdueInvoices());
    }
}