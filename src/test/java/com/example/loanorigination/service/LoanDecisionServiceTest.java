package com.example.loanorigination.service;

import com.example.loanorigination.dto.LoanApplicationRequestDto;
import com.example.loanorigination.dto.LoanApplicationResponseDto;
import com.example.loanorigination.dto.LoanOfferDto;
import com.example.loanorigination.entity.Applicant;
import com.example.loanorigination.entity.LoanApplication;
import com.example.loanorigination.entity.LoanOffer;
import com.example.loanorigination.mapper.LoanMapper;
import com.example.loanorigination.repository.ApplicantRepository;
import com.example.loanorigination.repository.LoanApplicationRepository;
import com.example.loanorigination.repository.LoanOfferRepository;
import com.example.loanorigination.util.LoanApplicationRequestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoanDecisionServiceTest {

    @Mock private LoanApplicationRepository loanApplicationRepository;
    @Mock private ApplicantRepository applicantRepository;
    @Mock private LoanOfferRepository loanOfferRepository;
    @Mock private LoanMapper loanMapper;
    @Mock private Random rng;

    @InjectMocks private LoanDecisionService service;

    @BeforeEach
    void setUp() {
        // Applicant mock
        when(applicantRepository.findByEmail(any()))
                .thenReturn(Optional.of(new Applicant()));

        // MapStruct mocks
        when(loanMapper.toLoanApplication(any(), any())).thenAnswer(invocation -> {
            var req = invocation.getArgument(0, LoanApplicationRequestDto.class);
            var applicant = invocation.getArgument(1, Applicant.class);

            LoanApplication app = new LoanApplication();
            app.setApplicant(applicant);
            app.setRequestedAmount(req.getRequestedAmount());
            app.setCreditLines(15);
            return app;
        });

        when(loanMapper.toLoanOfferDto(any())).thenAnswer(invocation -> {
            var offer = invocation.getArgument(0, LoanOffer.class);
            LoanOfferDto dto = new LoanOfferDto();
            dto.setInterestRate(offer != null && offer.getInterestRate() != null
                    ? offer.getInterestRate()
                    : BigDecimal.valueOf(0.20));
            dto.setTermMonths(offer != null && offer.getTermMonths() != null
                    ? offer.getTermMonths()
                    : 24);
            dto.setMonthlyPayment(offer != null && offer.getMonthlyPayment() != null
                    ? offer.getMonthlyPayment()
                    : BigDecimal.valueOf(1017.92));
            return dto;
        });

        // Repository + RNG mocks
        when(loanApplicationRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(rng.nextInt(101)).thenReturn(15); // default, override per test if needed
    }


    @Test
    void shouldApproveLoanWhenCreditLinesLessThan50() {
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().build();
        when(rng.nextInt(101)).thenReturn(30);

        LoanApplicationResponseDto response = service.processLoanApplication(request);

        assertNotNull(response);
        assertEquals("APPROVED", response.getDecision());
        assertNotNull(response.getOffer());
        assertEquals(24, response.getOffer().getTermMonths());
        assertEquals(1017.92, response.getOffer().getMonthlyPayment().doubleValue());
        assertEquals(0.2, response.getOffer().getInterestRate().doubleValue());
    }

    @Test
    void shouldDenyLoanWhenCreditLinesGreaterThan50() {
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().build();
        when(rng.nextInt(101)).thenReturn(55);

        LoanApplicationResponseDto response = service.processLoanApplication(request);

        assertEquals("DENIED", response.getDecision());
        assertNotNull(response.getReason());
    }

    @Test
    void shouldDenyWhenLoanAmountLessThan10k() {
        LoanApplicationRequestDto request =
                new LoanApplicationRequestBuilder().withRequestedAmount(BigDecimal.valueOf(1000)).build();
        when(rng.nextInt(101)).thenReturn(29);

        LoanApplicationResponseDto res = service.processLoanApplication(request);

        assertEquals("DENIED", res.getDecision());
        assertEquals("Requested amount outside 10k–50k range", res.getReason());
    }

    @Test
    void shouldDenyWhenLoanAmountGreaterThan50k() {
        LoanApplicationRequestDto request =
                new LoanApplicationRequestBuilder().withRequestedAmount(BigDecimal.valueOf(100000)).build();
        when(rng.nextInt(101)).thenReturn(29);

        LoanApplicationResponseDto res = service.processLoanApplication(request);

        assertEquals("DENIED", res.getDecision());
    }

    @Test
    void shouldApproveWith24MonthTermAnd20PercentInterestWhenCreditLinesBetween10And50() {
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().build();
        when(rng.nextInt(101)).thenReturn(15);

        LoanApplicationResponseDto res = service.processLoanApplication(request);

        assertEquals("APPROVED", res.getDecision());
        assertEquals(24, res.getOffer().getTermMonths());
        assertEquals(BigDecimal.valueOf(0.20), res.getOffer().getInterestRate());
    }

    @Test
    void shouldApproveWith36MonthTermAnd10PercentInterestWhenCreditLinesBelow10() {
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().build();
        when(rng.nextInt(101)).thenReturn(5);

        LoanApplicationResponseDto res = service.processLoanApplication(request);

        assertEquals("APPROVED", res.getDecision());
        assertEquals(36, res.getOffer().getTermMonths());
        assertEquals(BigDecimal.valueOf(0.10), res.getOffer().getInterestRate());
    }
}
