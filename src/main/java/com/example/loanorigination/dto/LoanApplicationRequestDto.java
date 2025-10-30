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
public class LoanApplicationRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone number must contain exactly 10 digits"
    )
    private String phone;

    @NotBlank(message = "SSN is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "SSN must contain exactly 10 digits with no spaces or dashes"
    )
    private String ssn;

    @NotNull(message = "Requested loan amount is required")
    @DecimalMin(value = "1.0", message = "Requested amount must be greater than zero")
    private BigDecimal requestedAmount;
    
}
