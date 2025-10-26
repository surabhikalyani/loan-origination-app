package com.example.loanorigination.service;

import com.example.loanorigination.dto.LoanApplicationRequest;
import com.example.loanorigination.dto.LoanApplicationResponse;
import com.example.loanorigination.dto.LoanApplicationRequest.Status;
import com.example.loanorigination.repository.LoanApplicationRepository;
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
        LoanApplicationRequest request = LoanApplicationRequest.builder()
                .name("Jane Doe")
                .address("123 Main St")
                .email("jane@example.com")
                .phone("1234567890")
                .ssn("1234567890")
                .requestedAmount(BigDecimal.valueOf(20000))
                .employmentStatus(Status.UNEMPLOYED)
                .build();

        when(rng.nextInt(101)).thenReturn(25);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponse response = service.processLoanApplication(request);

        assertEquals("DENIED", response.getDecision());
        assertEquals("No income source", response.getReason());
    }

    @Test
    void shouldApproveLoanWhenEmployedAndWithinRange() {
        LoanApplicationRequest request = generateTestRequest("John Doe", "123 Main St", "john.doe@example.com", "987-987-8765", "1234567890", BigDecimal.valueOf(20000), Status.EMPLOYED, BigDecimal.valueOf(8000));

        when(rng.nextInt(101)).thenReturn(25);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponse response = service.processLoanApplication(request);

        assertNotNull(response);
        assertEquals("APPROVED", response.getDecision());
        assertNotNull(response.getOffer());
    }

    @Test
    void shouldDenyLoanWhenEmployedAndAmountOutOfRange() {
        LoanApplicationRequest request = generateTestRequest("John Doe", "123 Main St", "john.doe@example.com", "987-987-8765", "1234567890", BigDecimal.valueOf(2000), Status.EMPLOYED, BigDecimal.valueOf(8000));

        when(rng.nextInt(101)).thenReturn(25);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponse response = service.processLoanApplication(request);

        assertNotNull(response);
        assertEquals("DENIED", response.getDecision());
        assertNotNull(response.getReason());
    }

    @Test
    void shouldApproveLoanWhenCreditLinesWithinRange() {
        LoanApplicationRequest request = generateTestRequest("John Doe", "123 Main St", "john.doe@example.com", "987-987-8765", "1234567890", BigDecimal.valueOf(20000), Status.EMPLOYED, BigDecimal.valueOf(8000));

        when(rng.nextInt(101)).thenReturn(30);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponse response = service.processLoanApplication(request);

        assertNotNull(response);
        assertEquals("APPROVED", response.getDecision());
        assertNotNull(response.getOffer());
        assertEquals(24, response.getOffer().getTermMonths());
        assertEquals(1017.92, response.getOffer().getMonthlyPayment().doubleValue());
        assertEquals(0.2, response.getOffer().getInterestRate().doubleValue());
    }

    @Test
    void shouldDenyLoanWhenCreditLinesOutOfRange() {
        LoanApplicationRequest request = generateTestRequest("John Doe", "123 Main St", "john.doe@example.com", "987-987-8765", "1234567890", BigDecimal.valueOf(35000), Status.EMPLOYED, BigDecimal.valueOf(8000));

        when(rng.nextInt(101)).thenReturn(55);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponse response = service.processLoanApplication(request);

        assertNotNull(response);
        assertEquals("DENIED", response.getDecision());
        assertNotNull(response.getReason());
    }

    @Test
    void shouldDenyWhenLoanAmountTooLow() {
        LoanApplicationRequest req = generateTestRequest("John Doe", "123 Main St", "john.doe@example.com", "987-987-8765", "1234567890", BigDecimal.valueOf(55000), Status.EMPLOYED, BigDecimal.valueOf(8000));

        when(rng.nextInt(101)).thenReturn(29);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponse res = service.processLoanApplication(req);
        assertEquals("DENIED", res.getDecision());
        assertEquals("Requested amount outside 10k–50k", res.getReason());
    }

    @Test
    void shouldDenyWhenLoanAmountTooHigh() {
        LoanApplicationRequest req = generateTestRequest("John Doe", "123 Main St", "john.doe@example.com", "987-987-8765", "1234567890", BigDecimal.valueOf(55000), Status.EMPLOYED, BigDecimal.valueOf(8000));

        when(rng.nextInt(101)).thenReturn(29);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponse res = service.processLoanApplication(req);
        assertEquals("DENIED", res.getDecision());
    }

    @Test
    void shouldApproveWhenCreditLinesBelow10() {
        LoanApplicationRequest req = generateTestRequest("John Doe", "123 Main St", "john.doe@example.com", "987-987-8765", "1234567890", BigDecimal.valueOf(45000), Status.EMPLOYED, BigDecimal.valueOf(8000));

        when(rng.nextInt(101)).thenReturn(6);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponse res = service.processLoanApplication(req);
        assertEquals("APPROVED", res.getDecision());
        assertEquals(36, res.getOffer().getTermMonths());
        assertEquals(BigDecimal.valueOf(0.10), res.getOffer().getInterestRate());
    }

    @Test
    void shouldApproveWhenCreditLinesBetween10And50() {
        LoanApplicationRequest req = generateTestRequest("John Doe", "123 Main St", "john.doe@example.com", "987-987-8765", "1234567890", BigDecimal.valueOf(45000), Status.EMPLOYED, BigDecimal.valueOf(8000));

        when(rng.nextInt(101)).thenReturn(15);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponse res = service.processLoanApplication(req);
        assertEquals("APPROVED", res.getDecision());
        assertEquals(24, res.getOffer().getTermMonths());
        assertEquals(BigDecimal.valueOf(0.20), res.getOffer().getInterestRate());
    }

    @Test
    void shouldDenyWhenCreditLinesAbove50() {
        LoanApplicationRequest req = generateTestRequest("John Doe", "123 Main St", "john.doe@example.com", "987-987-8765", "1234567890", BigDecimal.valueOf(45000), Status.EMPLOYED, BigDecimal.valueOf(8500));

        when(rng.nextInt(101)).thenReturn(75);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponse res = service.processLoanApplication(req);
        assertEquals("DENIED", res.getDecision());
        assertEquals("Credit lines > 50", res.getReason());
    }

    @Test
    void shouldDenyWhenMonthlyIncomeLessThan2k() {
        LoanApplicationRequest req = generateTestRequest("John Doe", "123 Main St", "john.doe@example.com", "987-987-8765", "1234567890", BigDecimal.valueOf(45000), Status.EMPLOYED, BigDecimal.valueOf(1000));
        when(rng.nextInt(101)).thenReturn(5);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponse res = service.processLoanApplication(req);
        assertEquals("DENIED", res.getDecision());
        assertEquals("Insufficient income", res.getReason());
    }

    private LoanApplicationRequest generateTestRequest(String name, String address, String email, String phone, String ssn, BigDecimal requestedAmount, Status status, BigDecimal monthlyIncome) {
        return LoanApplicationRequest.builder()
                .name(name)
                .address(address)
                .email(email)
                .phone(phone)
                .ssn(ssn)
                .requestedAmount(requestedAmount)
                .employmentStatus(status)
                .monthlyIncome(monthlyIncome)
                .build();
    }
}
