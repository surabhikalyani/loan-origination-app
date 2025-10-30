package com.example.loanorigination.controller;

import com.example.loanorigination.dto.LoanApplicationRequestDto;
import com.example.loanorigination.dto.LoanApplicationResponseDto;
import com.example.loanorigination.dto.LoanOfferDto;
import com.example.loanorigination.service.LoanDecisionService;
import com.example.loanorigination.util.LoanApplicationRequestBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanApplicationController.class)
class LoanApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoanDecisionService service;

    @Test
    void shouldReturnApprovedResponse() throws Exception {
        // mock response
        LoanOfferDto offer = new LoanOfferDto(
                BigDecimal.valueOf(20000),
                BigDecimal.valueOf(0.12),
                24,
                BigDecimal.valueOf(942.15)
        );

        LoanApplicationResponseDto mockResponse =
                new LoanApplicationResponseDto("APPROVED", null, offer);

        Mockito.when(service.processLoanApplication(Mockito.any()))
                .thenReturn(mockResponse);

        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().build();

        mockMvc.perform(post("/api/loan-applications/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("APPROVED"))
                .andExpect(jsonPath("$.offer.totalLoanAmount").value(20000));
    }

    @Test
    void shouldReturnDeniedResponse() throws Exception {
        // given: a mock denied response from the service
        LoanApplicationResponseDto mockResponse = new LoanApplicationResponseDto(
                "DENIED",                 // decision
                "Credit lines > 50",      // reason
                null                      // no offer DTO
        );

        Mockito.when(service.processLoanApplication(Mockito.any()))
                .thenReturn(mockResponse);

        // and: a valid loan request
        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().build();

        // when + then: perform the request and assert the denied decision
        mockMvc.perform(post("/api/loan-applications/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("DENIED"))
                .andExpect(jsonPath("$.reason").value("Credit lines > 50"));
    }

    @Test
    void shouldReturnBadRequestWhenInvalidInput() throws Exception {
        // invalid request: missing name and invalid email
        LoanApplicationRequestDto invalidRequest = LoanApplicationRequestDto.builder()
                .name("") // @NotBlank violation
                .email("invalid-email") // @Email violation
                .requestedAmount(BigDecimal.valueOf(20000))
                .build();

        mockMvc.perform(post("/api/loan-applications/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").doesNotExist()); // optional if you use default validation response
    }

    @Test
    void shouldReturnInternalServerErrorWhenServiceThrowsException() throws Exception {
        // simulate service throwing a runtime exception
        Mockito.when(service.processLoanApplication(Mockito.any()))
                .thenThrow(new RuntimeException("Unexpected DB error"));

        LoanApplicationRequestDto request = new LoanApplicationRequestBuilder().build();

        mockMvc.perform(post("/api/loan-applications/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

}
