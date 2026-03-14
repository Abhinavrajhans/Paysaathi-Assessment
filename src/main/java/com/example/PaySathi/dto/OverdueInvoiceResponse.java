package com.example.PaySathi.dto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class OverdueInvoiceResponse {
    private String invoiceExternalId;
    private String customerName;
    private String customerExternalId;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private LocalDate dueDate;
    private long daysOverdue;       // how many days past due date
}