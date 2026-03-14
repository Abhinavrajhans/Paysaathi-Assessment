package com.example.PaySathi.mapper;

import com.example.PaySathi.dto.InvoiceDTO;
import com.example.PaySathi.models.Customer;
import com.example.PaySathi.models.Invoice;
import com.example.PaySathi.models.InvoiceStatus;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
public class InvoiceMapper {

    public static Invoice toEntity(InvoiceDTO dto, Customer customer) {
        Invoice invoice = Invoice.builder()
                .externalId(dto.getExternalId())
                .customer(customer)
                .totalAmount(dto.getTotalAmount())
                .paidAmount(dto.getPaidAmount())
                .issueDate(dto.getIssueDate())
                .dueDate(dto.getDueDate())
                .syncedAt(LocalDateTime.now())
                .build();

        invoice.setStatus(deriveStatus(dto));
        invoice.recalculateOutstanding();
        return invoice;
    }

    public static void updateEntity(InvoiceDTO dto, Invoice existing, Customer customer) {
        existing.setCustomer(customer);
        existing.setTotalAmount(dto.getTotalAmount());
        existing.setPaidAmount(dto.getPaidAmount());
        existing.setIssueDate(dto.getIssueDate());
        existing.setDueDate(dto.getDueDate());
        existing.setSyncedAt(LocalDateTime.now());
        existing.setStatus(deriveStatus(dto));
        existing.recalculateOutstanding();
    }

    public static void mapToEntity(InvoiceDTO dto, Invoice invoice, Customer customer) {
        invoice.setExternalId(dto.getExternalId());
        updateEntity(dto, invoice, customer);
    }

    private static InvoiceStatus deriveStatus(InvoiceDTO dto) {
        if (dto.getDueDate().isBefore(LocalDate.now())
                && dto.getPaidAmount().compareTo(dto.getTotalAmount()) < 0) {
            return InvoiceStatus.OVERDUE;
        }
        try {
            return InvoiceStatus.valueOf(dto.getStatus());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown status '{}', defaulting to SENT", dto.getStatus());
            return InvoiceStatus.SENT;
        }
    }
}