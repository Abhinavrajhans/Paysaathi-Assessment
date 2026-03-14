package com.example.PaySathi.services;

import com.example.PaySathi.dto.CustomerCreditResponse;
import com.example.PaySathi.dto.CustomerSummaryResponse;
import com.example.PaySathi.dto.OverdueInvoiceResponse;
import com.example.PaySathi.mapper.CustomerMapper;
import com.example.PaySathi.mapper.InvoiceMapper;
import com.example.PaySathi.models.Customer;
import com.example.PaySathi.models.Invoice;
import com.example.PaySathi.models.InvoiceStatus;
import com.example.PaySathi.repositories.CustomerRepository;
import com.example.PaySathi.repositories.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class InsightsService {


    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;

    // Single customer summary
    public CustomerSummaryResponse getCustomerSummary(String externalId) {
        Customer customer = customerRepository
                .findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + externalId));

        List<Invoice> invoices = invoiceRepository.findByCustomerId(customer.getId());

        BigDecimal totalBilled = invoices.stream()
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = invoices.stream()
                .map(Invoice::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOutstanding = invoices.stream()
                .map(Invoice::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long overdueCount = invoices.stream()
                .filter(i -> i.getStatus() == InvoiceStatus.OVERDUE)
                .count();

        return CustomerMapper.toCustomerSummaryResponse(
                customer,invoices.size(),(int)overdueCount,
                totalBilled,totalPaid,totalOutstanding);

    }


    // All customers with their outstanding balances
    public List<CustomerSummaryResponse> getAllCustomerSummaries() {
        return customerRepository.findAll()
                .stream()
                .map(customer -> getCustomerSummary(customer.getExternalId()))
                .toList();
    }

    // Credit risk profile for a single customer
    public CustomerCreditResponse getCustomerCreditProfile(String externalId) {
        Customer customer = customerRepository
                .findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + externalId));

        List<Invoice> overdueInvoices = invoiceRepository
                .findByCustomerIdAndStatus(customer.getId(), InvoiceStatus.OVERDUE);

        BigDecimal totalOutstanding = invoiceRepository
                .sumOutstandingByCustomerId(customer.getId());

        List<OverdueInvoiceResponse> overdueResponses = overdueInvoices.stream()
                .map(inv -> InvoiceMapper.toOverdueInvoiceResponse(inv,customer.getName(),customer.getExternalId()))
                .toList();

        String riskLevel=deriveRiskLevel(totalOutstanding, overdueInvoices.size());

        return CustomerMapper.toCustomerCreditResponse(customer,totalOutstanding,
                overdueInvoices.size(),riskLevel,overdueResponses);

    }

    // Risk level derivation — business logic lives here, not in controller
    private String deriveRiskLevel(BigDecimal totalOutstanding, int overdueCount) {
        if (overdueCount == 0) return "LOW";
        if (overdueCount <= 2 && totalOutstanding.compareTo(new BigDecimal("100000")) < 0)
            return "MEDIUM";
        return "HIGH";
    }

    public List<OverdueInvoiceResponse> getAllOverdueInvoices() {
        return invoiceRepository
                .findByStatusWithCustomer(InvoiceStatus.OVERDUE)
                .stream()
                .map(InvoiceMapper::toOverdueInvoiceResponse)
                .toList();
    }
}
