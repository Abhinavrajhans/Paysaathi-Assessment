package com.example.PaySathi.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CustomerCreditResponse {
    private String externalId;
    private String name;
    private BigDecimal totalOutstanding;
    private int overdueCount;
    private String riskLevel;       // LOW, MEDIUM, HIGH
    private List<OverdueInvoiceResponse> overdueInvoices;
}