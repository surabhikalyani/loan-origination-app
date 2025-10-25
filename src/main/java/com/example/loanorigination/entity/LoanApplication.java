package com.example.loanorigination.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Data  // Generates getters, setters, equals, hashCode, toString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String email;
    private String phone;
    private String ssn;
    private BigDecimal requestedAmount;

    private Integer creditLines;
    private String decision;
    private String reason;
    private BigDecimal interestRate;
    private Integer termMonths;
    private BigDecimal monthlyPayment;
}
