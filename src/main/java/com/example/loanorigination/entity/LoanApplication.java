package com.example.loanorigination.entity;

import com.example.loanorigination.dto.LoanApplicationRequest;
import static com.example.loanorigination.dto.LoanApplicationRequest.Status;
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
    private BigDecimal monthlyIncome;

    @Enumerated(EnumType.STRING)
    private Status employmentStatus;

    private Integer creditLines;
    private String decision;
    private String reason;
    private BigDecimal interestRate;
    private Integer termMonths;
    private BigDecimal monthlyPayment;
}
