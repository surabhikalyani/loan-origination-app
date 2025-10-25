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
public class LoanApplicationResponse {
    private int creditLines;
    private String decision;
    private String reason;
    private LoanOffer offer;
}