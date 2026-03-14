package com.example.PaySathi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvoiceDTO {

    @JsonProperty("id")
    private String externalId;

    private String customerId;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private String status;
    private LocalDate issueDate;
    private LocalDate dueDate;
}