package com.example.loanorigination.controller;

import com.example.loanorigination.dto.LoanApplicationRequest;
import com.example.loanorigination.dto.LoanApplicationResponse;
import com.example.loanorigination.service.LoanDecisionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/loan-applications")
@CrossOrigin(origins = {"http://localhost:5173/", "http://localhost:3000"})
public class LoanApplicationController {

    private static final Logger log = LoggerFactory.getLogger(LoanApplicationController.class);
    private final LoanDecisionService service;

    public LoanApplicationController(LoanDecisionService service) {
        this.service = service;
    }

    @PostMapping("/apply")
    public ResponseEntity<LoanApplicationResponse> apply(@RequestBody LoanApplicationRequest req) {

        log.info("POST /api/loans/apply received for applicant='{}'", req.getName());
        log.debug("Request payload: name={}, requestedAmount={}", req.getName(), req.getRequestedAmount());

        try {
            LoanApplicationResponse response = service.processLoanApplication(req);

            // 3️⃣ Exit log
            log.info("Loan decision completed for '{}': decision={}, creditLines={}",
                    req.getName(), response.getDecision(), response.getCreditLines());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 4️⃣ Error log
            log.error("Error processing loan application for '{}': {}", req.getName(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }    }
}
