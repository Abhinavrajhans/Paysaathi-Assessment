package com.example.PaySathi.mapper;

import com.example.PaySathi.dto.CustomerCreditResponse;
import com.example.PaySathi.dto.CustomerDTO;
import com.example.PaySathi.dto.CustomerSummaryResponse;
import com.example.PaySathi.dto.OverdueInvoiceResponse;
import com.example.PaySathi.models.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    public static CustomerCreditResponse toCustomerCreditResponse(
            Customer customer, BigDecimal totalOutstanding,
            int overdueInvoicesSize,String riskLevel,
            List<OverdueInvoiceResponse> overdueResponses
    ) {
        return CustomerCreditResponse.builder()
                .externalId(customer.getExternalId())
                .name(customer.getName())
                .totalOutstanding(totalOutstanding)
                .overdueCount(overdueInvoicesSize)
                .riskLevel(riskLevel)
                .overdueInvoices(overdueResponses)
                .build();
    }


    public static CustomerSummaryResponse toCustomerSummaryResponse (
            Customer customer,int invoicesSize,
            int overdueCount,BigDecimal totalBilled,
            BigDecimal totalPaid,BigDecimal totalOutstanding) {
        return CustomerSummaryResponse.builder()
                .externalId(customer.getExternalId())
                .name(customer.getName())
                .email(customer.getEmail())
                .totalInvoices(invoicesSize)
                .overdueInvoices((int) overdueCount)
                .totalBilled(totalBilled)
                .totalPaid(totalPaid)
                .totalOutstanding(totalOutstanding)
                .build();
    }

}