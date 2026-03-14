package com.example.PaySathi.mapper;

import com.example.PaySathi.dto.PaymentDTO;
import com.example.PaySathi.models.Invoice;
import com.example.PaySathi.models.Payment;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class PaymentMapper {

    public static Payment toEntity(PaymentDTO dto, Invoice invoice) {
        return Payment.builder()
                .externalId(dto.getExternalId())
                .invoice(invoice)
                .amount(dto.getAmount())
                .paymentDate(dto.getPaymentDate())
                .method(dto.getMethod())
                .reference(dto.getReference())
                .syncedAt(LocalDateTime.now())
                .build();
    }

    public static void updateEntity(PaymentDTO dto, Payment existing, Invoice invoice) {
        existing.setInvoice(invoice);
        existing.setAmount(dto.getAmount());
        existing.setPaymentDate(dto.getPaymentDate());
        existing.setMethod(dto.getMethod());
        existing.setReference(dto.getReference());
        existing.setSyncedAt(LocalDateTime.now());
    }

    public static void mapToEntity(PaymentDTO dto, Payment payment, Invoice invoice) {
        payment.setExternalId(dto.getExternalId());
        updateEntity(dto, payment, invoice);
    }
}
