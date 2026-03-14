package com.example.PaySathi.services;


import com.example.PaySathi.dto.PaymentDTO;
import com.example.PaySathi.exception.SyncException;
import com.example.PaySathi.gateway.AccountingGateway;
import com.example.PaySathi.mapper.PaymentMapper;
import com.example.PaySathi.models.Invoice;
import com.example.PaySathi.models.Payment;
import com.example.PaySathi.models.SyncLog;
import com.example.PaySathi.repositories.InvoiceRepository;
import com.example.PaySathi.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AccountingGateway accountingGateway;
    private final PaymentRepository paymentRepository;
    private final SyncLogService syncLogService;
    private final InvoiceRepository invoiceRepository;

    @Transactional
    public void syncPayments() {
        log.info("Starting payment sync...");
        SyncLog syncLog = syncLogService.startLog("PAYMENT");

        try {
            List<PaymentDTO> dtos = accountingGateway.fetchAllPayments();
            int saved = 0;
            int skipped = 0;

            for (PaymentDTO dto : dtos) {

                Invoice invoice = invoiceRepository
                        .findByExternalId(dto.getInvoiceId())
                        .orElse(null);

                if (invoice == null) {
                    log.warn("Skipping payment {} — invoice {} not found locally.",
                            dto.getExternalId(), dto.getInvoiceId());
                    skipped++;
                    continue;
                }

                Payment payment = paymentRepository
                        .findByExternalId(dto.getExternalId())
                        .orElseGet(Payment::new);

                PaymentMapper.mapToEntity(dto, payment, invoice);
                paymentRepository.save(payment);
                saved++;
            }

            syncLogService.completeLog(syncLog, dtos.size(), saved);
            log.info("Payment sync done — {} saved, {} skipped", saved, skipped);

        } catch (Exception e) {
            syncLogService.failLog(syncLog, e.getMessage());
            log.error("Payment sync failed: {}", e.getMessage());
            throw new SyncException("Payment sync failed: " + e.getMessage(), e);
        }
    }
}
