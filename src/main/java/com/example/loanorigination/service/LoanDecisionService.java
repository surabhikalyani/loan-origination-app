package com.example.loanorigination.service;

import com.example.loanorigination.dto.LoanApplicationRequestDto;
import com.example.loanorigination.dto.LoanApplicationResponseDto;
import com.example.loanorigination.dto.LoanOfferDto;
import com.example.loanorigination.entity.Applicant;
import com.example.loanorigination.entity.LoanApplication;
import com.example.loanorigination.entity.LoanOffer;
import com.example.loanorigination.repository.ApplicantRepository;
import com.example.loanorigination.repository.LoanApplicationRepository;
import com.example.loanorigination.repository.LoanOfferRepository;
import com.example.loanorigination.mapper.LoanMapper;
import com.example.loanorigination.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanDecisionService {

    private final ApplicantRepository applicantRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanOfferRepository loanOfferRepository;
    private final LoanMapper loanMapper;
    private final Random rng;
    private final CryptoUtil cryptoUtil;

    private static final BigDecimal MIN_AMOUNT = BigDecimal.valueOf(10000);
    private static final BigDecimal MAX_AMOUNT = BigDecimal.valueOf(50000);

    @Transactional
    public LoanApplicationResponseDto processLoanApplication(LoanApplicationRequestDto req) {
        log.info("Processing new loan application for {}", req.getName());

        //Check if applicant exists before saving to DB
        Applicant applicant = applicantRepository
                .findByEmail(req.getEmail())
                .orElseGet(() -> applicantRepository.save(loanMapper.toApplicant(cryptoUtil, req)));

        //Save application details to DB
        LoanApplication application = loanMapper.toLoanApplication(req, applicant);
        loanApplicationRepository.save(application);
        log.debug("Saved loan application: id={}, applicantId={}", application.getId(), applicant.getId());

        //Evaluate decision
        LoanOffer offer = evaluateDecision(application);
        offer.setApplication(application);

        //Save offer details
        loanOfferRepository.save(offer);

        log.info("Decision: applicant={}, decision={}", applicant.getName(), offer.getDecision());

        LoanOfferDto offerDto = "APPROVED".equals(offer.getDecision()) ? loanMapper.toLoanOfferDto(offer) : null;

        return LoanApplicationResponseDto.builder()
                .decision(offer.getDecision())
                .reason(offer.getReason())
                .offer(offerDto)
                .build();
    }

    private LoanOffer evaluateDecision(LoanApplication app) {
        int creditLines = rng.nextInt(101);
        app.setCreditLines(creditLines);

        BigDecimal amount = app.getRequestedAmount();
        String decision;
        String reason = null;
        BigDecimal interestRate = null;
        Integer termMonths = null;
        BigDecimal monthlyPayment = null;

        if (amount.compareTo(MIN_AMOUNT) < 0 || amount.compareTo(MAX_AMOUNT) > 0) {
            decision = "DENIED";
            reason = "Requested amount outside 10k–50k range";
        } else if (creditLines > 50) {
            decision = "DENIED";
            reason = "Credit lines > 50";
        } else {
            decision = "APPROVED";
            interestRate = creditLines < 10 ? BigDecimal.valueOf(0.10) : BigDecimal.valueOf(0.20);
            termMonths = creditLines < 10 ? 36 : 24;
            monthlyPayment = amortizedMonthly(amount, interestRate, termMonths);
        }

        log.debug("Computed offer: decision={}, rate={}, term={}", decision, interestRate, termMonths);

        return LoanOffer.builder()
                .application(app)
                .decision(decision)
                .reason(reason)
                .interestRate(interestRate)
                .termMonths(termMonths)
                .monthlyPayment(monthlyPayment)
                .requestedAmount(app.getRequestedAmount())
                .build();
    }

    private BigDecimal amortizedMonthly(BigDecimal principal, BigDecimal rate, int term) {
        BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
        double r = monthlyRate.doubleValue();
        double p = principal.doubleValue();
        double payment = p * r / (1 - Math.pow(1 + r, -term));
        return BigDecimal.valueOf(payment).setScale(2, RoundingMode.HALF_UP);
    }
}
