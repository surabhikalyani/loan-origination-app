package com.example.loanorigination.controller;

import com.example.loanorigination.dto.LoanApplicationRequestDto;
import com.example.loanorigination.dto.LoanApplicationResponseDto;
import com.example.loanorigination.service.LoanDecisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/loan-applications/apply")
@CrossOrigin(origins = {"http://localhost:5173/", "http://localhost:3000"})
public class LoanApplicationController {

    private final LoanDecisionService service;

    /**
     * Handles borrower loan applications.
     * Validates the request, delegates to business logic, and returns the loan decision.
     */
    @PostMapping
    public ResponseEntity<LoanApplicationResponseDto> apply(
            @Valid @RequestBody LoanApplicationRequestDto request) {

        log.info("Received loan application for name='{}', requestedAmount={}",
                request.getName(), request.getRequestedAmount());

        LoanApplicationResponseDto response = service.processLoanApplication(request);

        log.info("Loan decision for '{}': decision={}",
                request.getName(), response.getDecision());

        return ResponseEntity.ok(response);
    }
}
