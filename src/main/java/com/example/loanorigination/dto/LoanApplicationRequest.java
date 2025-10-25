package com.example.loanorigination.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String address;
    @Email
    private String email;
    @NotBlank
    private String phone;
    @Pattern(regexp = "[0-9]{3}-?[0-9]{2}-?[0-9]{4}")
    private String ssn;
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal requestedAmount;
}
