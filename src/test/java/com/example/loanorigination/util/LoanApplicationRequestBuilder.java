package com.example.loanorigination.util;

import com.example.loanorigination.dto.LoanApplicationRequestDto;

import java.math.BigDecimal;

public class LoanApplicationRequestBuilder {

    private String name = "John Doe";
    private String address = "123 Main St";
    private String email = "john.doe@example.com";
    private String phone = "9876543210";
    private String ssn = "1234567890";
    private BigDecimal requestedAmount = BigDecimal.valueOf(20000);

    public LoanApplicationRequestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public LoanApplicationRequestBuilder withRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
        return this;
    }

    public LoanApplicationRequestDto build() {
        return LoanApplicationRequestDto.builder()
                .name(name)
                .address(address)
                .email(email)
                .phone(phone)
                .ssn(ssn)
                .requestedAmount(requestedAmount)
                .build();
    }
}
