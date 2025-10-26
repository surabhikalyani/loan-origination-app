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
    public enum Status {
        EMPLOYED,
        UNEMPLOYED
    }
    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @Email
    private String email;

    @NotBlank
    private String phone;

    @Pattern(regexp = "^[0-9]{10}$", message = "SSN must contain exactly 10 digits with no spaces or dashes")
    private String ssn;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal requestedAmount;

    @NotNull
    private Status employmentStatus;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal monthlyIncome;
}
