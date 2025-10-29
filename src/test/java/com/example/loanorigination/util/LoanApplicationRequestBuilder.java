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
    private LoanApplicationRequestDto.Status employmentStatus = LoanApplicationRequestDto.Status.EMPLOYED;
    private BigDecimal monthlyIncome = BigDecimal.valueOf(5000);
    private BigDecimal existingDebt = BigDecimal.ZERO;

    public LoanApplicationRequestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public LoanApplicationRequestBuilder withIncome(BigDecimal income) {
        this.monthlyIncome = income;
        return this;
    }

    public LoanApplicationRequestBuilder withExistingDebt(BigDecimal existingDebt) {
        this.existingDebt = existingDebt;
        return this;
    }

    public LoanApplicationRequestBuilder withRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
        return this;
    }

    public LoanApplicationRequestBuilder withEmploymentStatus(LoanApplicationRequestDto.Status employmentStatus) {
        this.employmentStatus = employmentStatus;
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
                .employmentStatus(employmentStatus)
                .monthlyIncome(monthlyIncome)
                .existingDebt(existingDebt)
                .build();
    }
}
