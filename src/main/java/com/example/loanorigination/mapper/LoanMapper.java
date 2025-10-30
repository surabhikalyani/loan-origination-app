package com.example.loanorigination.mapper;

import com.example.loanorigination.dto.LoanApplicationRequestDto;
import com.example.loanorigination.dto.LoanOfferDto;
import com.example.loanorigination.entity.Applicant;
import com.example.loanorigination.entity.LoanApplication;
import com.example.loanorigination.entity.LoanOffer;
import com.example.loanorigination.util.CryptoUtil;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    // 🔹 DTO → Applicant
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())"),
            @Mapping(target = "applications", ignore = true),
            @Mapping(target = "ssn", expression = "java(cryptoUtil.encrypt(req.getSsn()))")
    })
    Applicant toApplicant(@Context CryptoUtil cryptoUtil, LoanApplicationRequestDto req);


    // 🔹 DTO → LoanApplication
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "applicant", source = "applicant"),
            @Mapping(target = "creditLines", ignore = true),
            @Mapping(target = "offer", ignore = true),
            @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    })
    LoanApplication toLoanApplication(LoanApplicationRequestDto req, Applicant applicant);

    // 🔹 Entity → DTO (optional for response)
    @Mappings({
            @Mapping(target = "totalLoanAmount", source = "requestedAmount"),
            @Mapping(target = "interestRate", source = "interestRate"),
            @Mapping(target = "termMonths", source = "termMonths"),
            @Mapping(target = "monthlyPayment", source = "monthlyPayment")
    })
    LoanOfferDto toLoanOfferDto(LoanOffer offer);
}
