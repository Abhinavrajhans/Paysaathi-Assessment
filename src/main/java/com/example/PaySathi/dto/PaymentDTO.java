package com.example.PaySathi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentDTO {

    @JsonProperty("id")
    private String externalId;

    private String invoiceId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String method;
    private String reference;
}
