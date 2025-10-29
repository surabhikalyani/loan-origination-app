package com.example.loanorigination.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanOfferDto {
    private BigDecimal totalLoanAmount;
    private BigDecimal interestRate;
    private int termMonths;
    private BigDecimal monthlyPayment;
}
