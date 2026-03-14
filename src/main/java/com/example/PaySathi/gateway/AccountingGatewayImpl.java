package com.example.PaySathi.gateway;

import com.example.PaySathi.dto.CustomerDTO;
import com.example.PaySathi.dto.InvoiceDTO;
import com.example.PaySathi.dto.PaymentDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountingGatewayImpl implements AccountingGateway {

    private final RestClient restClient;

    @Value("${external.api.base-url}")
    private String baseUrl;

    @Override
    public List<CustomerDTO> fetchAllCustomers() {
        log.info("Gateway → fetching customers from external API");
        return restClient.get()
                .uri(baseUrl + "/api/customers")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CustomerDTO>>() {});
    }

    @Override
    public List<InvoiceDTO> fetchAllInvoices() {
        log.info("Gateway → fetching invoices from external API");
        return restClient.get()
                .uri(baseUrl + "/api/invoices")
                .retrieve()
                .body(new ParameterizedTypeReference<List<InvoiceDTO>>() {});
    }

    @Override
    public List<PaymentDTO> fetchAllPayments() {
        log.info("Gateway → fetching payments from external API");
        return restClient.get()
                .uri(baseUrl + "/api/payments")
                .retrieve()
                .body(new ParameterizedTypeReference<List<PaymentDTO>>() {});
    }
}