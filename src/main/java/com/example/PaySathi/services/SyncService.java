package com.example.PaySathi.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final CustomerService customerService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    public void syncAll() {
        log.info("========== Starting Full Sync ==========");
        customerService.syncCustomers();
        invoiceService.syncInvoices();
        paymentService.syncPayments();
        log.info("========== Full Sync Completed ==========");
    }


}