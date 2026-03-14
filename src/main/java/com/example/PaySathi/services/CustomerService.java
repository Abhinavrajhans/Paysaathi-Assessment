package com.example.PaySathi.services;

import com.example.PaySathi.gateway.AccountingGateway;
import com.example.PaySathi.mapper.CustomerMapper;
import com.example.PaySathi.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.example.PaySathi.dto.CustomerDTO;
import com.example.PaySathi.models.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final AccountingGateway accountingGateway;
    private final CustomerRepository customerRepository;
    private final SyncLogService syncLogService;

    @Transactional
    public void syncCustomers() {
        log.info("Starting customer sync...");
        SyncLog syncLog = syncLogService.startLog("CUSTOMER");

        try {
            List<CustomerDTO> dtos = accountingGateway.fetchAllCustomers();
            int saved = 0;
            for (CustomerDTO dto : dtos) {
                Customer customer = customerRepository
                        .findByExternalId(dto.getExternalId())
                        .orElseGet(Customer::new);
                CustomerMapper.mapToEntity(dto, customer);
                customerRepository.save(customer);
                saved++;
            }
            syncLogService.completeLog(syncLog, dtos.size(), saved);
            log.info("Customer sync done — {} records processed", saved);

        } catch (Exception e) {
            syncLogService.failLog(syncLog, e.getMessage());
            log.error("Customer sync failed: {}", e.getMessage());
            throw e;
        }
    }
}
