
package com.credshield.credshield.service;

import com.credshield.credshield.entity.LoanStatus;
import com.credshield.credshield.repository.LoanRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final LoanRepository loanRepository;

    public DashboardService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public Map<String, Object> getDashboardStats() {

        long totalLoans = loanRepository.count();

        long pendingLoans =
                loanRepository.countByStatus(LoanStatus.PENDING);

        long approvedLoans =
                loanRepository.countByStatus(LoanStatus.APPROVED);

        long rejectedLoans =
                loanRepository.countByStatus(LoanStatus.REJECTED);

        BigDecimal totalLoanAmount = loanRepository.findAll()
                .stream()
                .map(loan -> loan.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalLoans", totalLoans);
        stats.put("pendingLoans", pendingLoans);
        stats.put("approvedLoans", approvedLoans);
        stats.put("rejectedLoans", rejectedLoans);
        stats.put("totalLoanAmount", totalLoanAmount);

        return stats;
    }
}
