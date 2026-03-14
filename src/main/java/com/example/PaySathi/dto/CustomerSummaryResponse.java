package com.example.PaySathi.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CustomerSummaryResponse {
    private String externalId;
    private String name;
    private String email;
    private int totalInvoices;
    private int overdueInvoices;
    private BigDecimal totalBilled;
    private BigDecimal totalPaid;
    private BigDecimal totalOutstanding;
}