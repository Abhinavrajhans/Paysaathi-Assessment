package com.example.PaySathi.gateway;

import com.example.PaySathi.dto.CustomerDTO;
import com.example.PaySathi.dto.InvoiceDTO;
import com.example.PaySathi.dto.PaymentDTO;
import java.util.List;

public interface AccountingGateway {
    List<CustomerDTO> fetchAllCustomers();
    List<InvoiceDTO> fetchAllInvoices();
    List<PaymentDTO> fetchAllPayments();
}