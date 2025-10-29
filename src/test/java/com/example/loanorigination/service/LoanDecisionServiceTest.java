package com.example.loanorigination.service;

import com.example.loanorigination.dto.LoanApplicationRequestDto;
import com.example.loanorigination.dto.LoanApplicationResponseDto;
import com.example.loanorigination.dto.LoanApplicationRequestDto.Status;
import com.example.loanorigination.repository.LoanApplicationRepository;

import com.example.loanorigination.util.LoanApplicationRequestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class LoanDecisionServiceTest {

    @Mock
    private LoanApplicationRepository repo;

    @InjectMocks
    private LoanDecisionService service;

    @Mock
    private Random rng;

    @BeforeEach
    void setUp() { openMocks(this); }

    @Test
    void shouldDenyLoanWhenUnemployed() {
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().withEmploymentStatus(Status.UNEMPLOYED).build();
        when(rng.nextInt(101)).thenReturn(25);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponseDto response = service.processLoanApplication(request);

        assertEquals("DENIED", response.getDecision());
        assertEquals("No income source", response.getReason());
    }

    @Test
    void shouldApproveLoanWhenEmployedAndWithinRange() {
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().build();

        when(rng.nextInt(101)).thenReturn(25);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponseDto response = service.processLoanApplication(request);

        assertNotNull(response);
        assertEquals("APPROVED", response.getDecision());
        assertNotNull(response.getOffer());
    }

    @Test
    void shouldDenyLoanWhenEmployedAndAmountOutOfRange() {
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().withRequestedAmount(BigDecimal.valueOf(1000)).build();

        when(rng.nextInt(101)).thenReturn(25);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponseDto response = service.processLoanApplication(request);

        assertNotNull(response);
        assertEquals("DENIED", response.getDecision());
        assertNotNull(response.getReason());
    }

    @Test
    void shouldApproveLoanWhenCreditLinesWithinRange() {
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().build();

        when(rng.nextInt(101)).thenReturn(30);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponseDto response = service.processLoanApplication(request);

        assertNotNull(response);
        assertEquals("APPROVED", response.getDecision());
        assertNotNull(response.getOffer());
        assertEquals(24, response.getOffer().getTermMonths());
        assertEquals(1017.92, response.getOffer().getMonthlyPayment().doubleValue());
        assertEquals(0.2, response.getOffer().getInterestRate().doubleValue());
    }

    @Test
    void shouldDenyLoanWhenCreditLinesOutOfRange() {
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().build();

        when(rng.nextInt(101)).thenReturn(55);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponseDto response = service.processLoanApplication(request);

        assertNotNull(response);
        assertEquals("DENIED", response.getDecision());
        assertNotNull(response.getReason());
    }

    @Test
    void shouldDenyWhenLoanAmountTooLow() {
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().withRequestedAmount(BigDecimal.valueOf(1000)).build();

        when(rng.nextInt(101)).thenReturn(29);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponseDto res = service.processLoanApplication(request);
        assertEquals("DENIED", res.getDecision());
        assertEquals("Requested amount outside 10k–50k", res.getReason());
    }

    @Test
    void shouldDenyWhenLoanAmountTooHigh() {
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().withRequestedAmount(BigDecimal.valueOf(100000)).build();

        when(rng.nextInt(101)).thenReturn(29);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponseDto res = service.processLoanApplication(request);
        assertEquals("DENIED", res.getDecision());
    }

    @Test
    void shouldApproveWhenCreditLinesBelow10() {
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().build();

        when(rng.nextInt(101)).thenReturn(6);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponseDto res = service.processLoanApplication(request);
        assertEquals("APPROVED", res.getDecision());
        assertEquals(36, res.getOffer().getTermMonths());
        assertEquals(BigDecimal.valueOf(0.10), res.getOffer().getInterestRate());
    }

    @Test
    void shouldApproveWhenCreditLinesBetween10And50() {
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().build();

        when(rng.nextInt(101)).thenReturn(15);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponseDto res = service.processLoanApplication(request);
        assertEquals("APPROVED", res.getDecision());
        assertEquals(24, res.getOffer().getTermMonths());
        assertEquals(BigDecimal.valueOf(0.20), res.getOffer().getInterestRate());
    }

    @Test
    void shouldDenyWhenCreditLinesAbove50() {
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().build();

        when(rng.nextInt(101)).thenReturn(75);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponseDto res = service.processLoanApplication(request);
        assertEquals("DENIED", res.getDecision());
        assertEquals("Credit lines > 50", res.getReason());
    }

    @Test
    void shouldDenyWhenMonthlyIncomeLessThan2k() {
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().withIncome(BigDecimal.valueOf(1000)).build();
        when(rng.nextInt(101)).thenReturn(5);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponseDto res = service.processLoanApplication(request);
        assertEquals("DENIED", res.getDecision());
        assertEquals("Insufficient income", res.getReason());
    }

    @Test
    void shouldDenyWhenDTIGreaterThanThreshold() {
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().withIncome(BigDecimal.valueOf(4000)).withExistingDebt(BigDecimal.valueOf(2000)).build();
        when(rng.nextInt(101)).thenReturn(5);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponseDto res = service.processLoanApplication(request);
        assertEquals("DENIED", res.getDecision());
        assertEquals("High debt-to-income ratio", res.getReason());
    }

    @Test
    void shouldApproveWhenDTILessThanThreshold() {
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().withIncome(BigDecimal.valueOf(6000)).withExistingDebt(BigDecimal.valueOf(2000)).build();
        when(rng.nextInt(101)).thenReturn(5);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponseDto res = service.processLoanApplication(request);
        assertEquals("APPROVED", res.getDecision());
        assertEquals(36, res.getOffer().getTermMonths());
        assertEquals(645.34, res.getOffer().getMonthlyPayment().doubleValue());
        assertEquals(0.1, res.getOffer().getInterestRate().doubleValue());
    }

    @Test
    void shouldApproveWhenDTIEqualToThreshold() {
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().withIncome(BigDecimal.valueOf(5000)).withExistingDebt(BigDecimal.valueOf(2000)).build();
        when(rng.nextInt(101)).thenReturn(5);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponseDto res = service.processLoanApplication(request);
        assertEquals("APPROVED", res.getDecision());
        assertEquals(36, res.getOffer().getTermMonths());
        assertEquals(645.34, res.getOffer().getMonthlyPayment().doubleValue());
        assertEquals(0.1, res.getOffer().getInterestRate().doubleValue());
    }
}
