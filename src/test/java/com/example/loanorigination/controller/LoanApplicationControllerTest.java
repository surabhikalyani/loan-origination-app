package com.example.loanorigination.controller;

import com.example.loanorigination.dto.LoanApplicationRequestDto;
import com.example.loanorigination.dto.LoanApplicationResponseDto;
import com.example.loanorigination.dto.LoanOfferDto;
import com.example.loanorigination.service.LoanDecisionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanApplicationController.class)
class LoanApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoanDecisionService service = Mockito.mock(LoanDecisionService.class);

    @Test
    void shouldReturnApprovedResponse() throws Exception {
        LoanOfferDto offer = new LoanOfferDto(
                BigDecimal.valueOf(20000),
                BigDecimal.valueOf(0.12),
                24,
                BigDecimal.valueOf(942.15)
        );

        LoanApplicationResponseDto mockResponse = new LoanApplicationResponseDto(30, "APPROVED", null, offer);
        Mockito.when(service.processLoanApplication(any())).thenReturn(mockResponse);

        LoanApplicationRequestDto request = LoanApplicationRequestDto.builder()
                .name("Jane Doe")
                .address("123 Main St")
                .email("jane@example.com")
                .phone("1234567890")
                .ssn("1234567890")
                .requestedAmount(BigDecimal.valueOf(20000))
                .monthlyIncome(BigDecimal.valueOf(5000))
                .employmentStatus(LoanApplicationRequestDto.Status.EMPLOYED)
                .build();

        mockMvc.perform(post("/api/loan-applications/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("APPROVED"))
                .andExpect(jsonPath("$.offer.totalAmount").value(20000));
    }

    @Test
    void shouldReturnDeniedResponse() throws Exception {
        LoanApplicationResponseDto mockResponse =
                new LoanApplicationResponseDto(15, "DENIED", "No income source", null);
        Mockito.when(service.processLoanApplication(any())).thenReturn(mockResponse);

        LoanApplicationRequestDto request = LoanApplicationRequestDto.builder()
                .name("John Doe")
                .address("456 Elm St")
                .email("john@example.com")
                .phone("9876543210")
                .ssn("9876543210")
                .requestedAmount(BigDecimal.valueOf(20000))
                .monthlyIncome(BigDecimal.valueOf(5000))
                .employmentStatus(LoanApplicationRequestDto.Status.UNEMPLOYED)
                .build();

        mockMvc.perform(post("/api/loan-applications/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("DENIED"))
                .andExpect(jsonPath("$.reason").value("No income source"));
    }

    @Test
    void shouldReturnInternalServerErrorWhenInvalidInput() throws Exception {
        LoanApplicationRequestDto invalid = LoanApplicationRequestDto.builder()
                .name("") // missing required
                .email("invalidemail") // no .com, invalid format
                .requestedAmount(BigDecimal.valueOf(20000))
                .build();

        mockMvc.perform(post("/api/loan-applications/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}
