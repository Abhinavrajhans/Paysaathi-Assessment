package com.example.PaySathi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CustomerDTO {

    @JsonProperty("id")
    private String externalId;

    private String name;
    private String email;
    private String phone;
    private String address;
}