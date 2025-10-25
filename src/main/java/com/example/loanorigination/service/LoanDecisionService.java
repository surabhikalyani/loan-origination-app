package com.example.loanorigination.service;

import com.example.loanorigination.dto.LoanApplicationRequest;
import com.example.loanorigination.dto.LoanApplicationResponse;
import com.example.loanorigination.dto.LoanOffer;
import com.example.loanorigination.entity.LoanApplication;
import com.example.loanorigination.repository.LoanApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.loanorigination.util.DataMaskingUtil.maskEmail;
import static com.example.loanorigination.util.DataMaskingUtil.maskSsn;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Service
public class LoanDecisionService {

    private static final Logger log = LoggerFactory.getLogger(LoanDecisionService.class);

    private final LoanApplicationRepository loanApplicationRepository;
    private final Random rng = new Random();

    private static final BigDecimal MIN_AMOUNT = BigDecimal.valueOf(10_000);
    private static final BigDecimal MAX_AMOUNT = BigDecimal.valueOf(50_000);

    public LoanDecisionService(LoanApplicationRepository loanApplicationRepository) {
        this.loanApplicationRepository = loanApplicationRepository;
    }

    @Transactional
    public LoanApplicationResponse processLoanApplication(LoanApplicationRequest req) {

        log.info("Received new loan application: name='{}', requestedAmount={}",
                req.getName(), req.getRequestedAmount());
        // 1️⃣ Persist basic application data
        LoanApplication loanApplication = saveApplication(req);
        log.debug("Loan application saved with temporary ID={}", loanApplication.getId());

        // 2️⃣ Run business logic to decide on the loan
        evaluateDecision(loanApplication);
        log.info("Decision evaluated: creditLines={}, decision={}", loanApplication.getCreditLines(), loanApplication.getDecision());

        // 3️⃣ Persist final state after decision
        loanApplicationRepository.save(loanApplication);
        log.debug("Loan decision persisted: ID={}, decision={}", loanApplication.getId(), loanApplication.getDecision());

        // 4️⃣ Build API-friendly response
        LoanOffer offer = loanApplication.getDecision().equals("APPROVED")
                ? new LoanOffer(loanApplication.getRequestedAmount(), loanApplication.getInterestRate(),
                loanApplication.getTermMonths(), loanApplication.getMonthlyPayment())
                : null;

        if ("APPROVED".equals(loanApplication.getDecision())) {
            log.info("Loan approved for '{}': term={} months, rate={}%, monthlyPayment={}",
                    req.getName(),
                    loanApplication.getTermMonths(),
                    loanApplication.getInterestRate().multiply(BigDecimal.valueOf(100)).setScale(1),
                    loanApplication.getMonthlyPayment());
        } else {
            log.warn("Loan denied for '{}': reason='{}'", req.getName(), loanApplication.getReason());
        }

        return new LoanApplicationResponse(loanApplication.getCreditLines(), loanApplication.getDecision(), loanApplication.getReason(), offer);
    }

    /** ----------------------------------------------
     *  STEP 1 — Save initial borrower application
     *  ---------------------------------------------- */
    private LoanApplication saveApplication(LoanApplicationRequest req) {
        LoanApplication app = new LoanApplication();
        app.setName(req.getName());
        app.setAddress(req.getAddress());
        app.setEmail(req.getEmail());
        app.setPhone(req.getPhone());
        app.setSsn(req.getSsn());
        app.setRequestedAmount(req.getRequestedAmount());

        log.debug("Saving application for '{}'", req.getName());
        return loanApplicationRepository.save(app);
    }

    /** ----------------------------------------------
     *  STEP 2 — Evaluate and update loan decision
     *  ---------------------------------------------- */
    private void evaluateDecision(LoanApplication loanApplication) {
        int creditLines = rng.nextInt(101);
        loanApplication.setCreditLines(creditLines);
        log.debug("Generated random credit lines={} for '{}'", creditLines, loanApplication.getName());

        BigDecimal amount = loanApplication.getRequestedAmount();
        String decision;
        String reason = null;
        BigDecimal interestRate = null;
        Integer termMonths = null;
        BigDecimal monthlyPayment = null;

        if (amount.compareTo(MIN_AMOUNT) < 0 || amount.compareTo(MAX_AMOUNT) > 0) {
            decision = "DENIED";
            reason = "Requested amount outside 10k–50k";
        } else if (creditLines > 50) {
            decision = "DENIED";
            reason = "Credit lines > 50";
        } else {
            decision = "APPROVED";
            interestRate = creditLines < 10 ? BigDecimal.valueOf(0.10) : BigDecimal.valueOf(0.20);
            termMonths = creditLines < 10 ? 36 : 24;
            monthlyPayment = amortizedMonthly(amount, interestRate, termMonths);
        }

        log.debug("Decision computed: decision={}, reason={}, rate={}, term={}, monthly={}",
                decision, reason, interestRate, termMonths, monthlyPayment);

        loanApplication.setDecision(decision);
        loanApplication.setReason(reason);
        loanApplication.setInterestRate(interestRate);
        loanApplication.setTermMonths(termMonths);
        loanApplication.setMonthlyPayment(monthlyPayment);
    }

    /** ----------------------------------------------
     *  Helper — Monthly Payment Calculation
     *  ---------------------------------------------- */
    private BigDecimal amortizedMonthly(BigDecimal principal, BigDecimal interestRate, int term) {
        BigDecimal monthlyRate = interestRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        double r = monthlyRate.doubleValue();
        double p = principal.doubleValue();
        double payment = p * r / (1 - Math.pow(1 + r, -term));
        return BigDecimal.valueOf(payment).setScale(2, RoundingMode.HALF_UP);
    }
}
