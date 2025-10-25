package com.example.loanorigination.repository;

import com.example.loanorigination.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {}
