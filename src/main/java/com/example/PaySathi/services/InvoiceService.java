package com.example.PaySathi.services;

import com.example.PaySathi.dto.InvoiceDTO;
import com.example.PaySathi.dto.OverdueInvoiceResponse;
import com.example.PaySathi.exception.SyncException;
import com.example.PaySathi.gateway.AccountingGateway;
import com.example.PaySathi.mapper.InvoiceMapper;
import com.example.PaySathi.models.Customer;
import com.example.PaySathi.models.Invoice;
import com.example.PaySathi.models.InvoiceStatus;
import com.example.PaySathi.models.SyncLog;
import com.example.PaySathi.repositories.CustomerRepository;
import com.example.PaySathi.repositories.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final AccountingGateway accountingGateway;
    private final CustomerRepository customerRepository;
    private final SyncLogService syncLogService;
    private final InvoiceRepository invoiceRepository;


    @Transactional
    public void syncInvoices() {
        log.info("Starting invoice sync...");
        SyncLog syncLog = syncLogService.startLog("INVOICE");
        try {
            List<InvoiceDTO> dtos = accountingGateway.fetchAllInvoices();
            int saved = 0;
            int skipped = 0;
            for (InvoiceDTO dto : dtos) {
                Customer customer = customerRepository
                        .findByExternalId(dto.getCustomerId())
                        .orElse(null);

                if (customer == null) {
                    log.warn("Skipping invoice {} — customer {} not found locally.",
                            dto.getExternalId(), dto.getCustomerId());
                    skipped++;
                    continue;
                }

                Invoice invoice = invoiceRepository
                        .findByExternalId(dto.getExternalId())
                        .orElseGet(Invoice::new);

                InvoiceMapper.mapToEntity(dto, invoice, customer);
                invoiceRepository.save(invoice);
                saved++;
            }

            syncLogService.completeLog(syncLog, dtos.size(), saved);
            log.info("Invoice sync done — {} saved, {} skipped", saved, skipped);


        } catch (Exception e) {
            syncLogService.failLog(syncLog, e.getMessage());
            log.error("Invoice sync failed: {}", e.getMessage());
            throw new SyncException("Invoice sync failed: " + e.getMessage(), e);
        }

    }
}
