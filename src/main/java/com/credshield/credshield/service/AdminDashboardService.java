
package com.credshield.credshield.service;

import com.credshield.credshield.dto.DashboardSummaryResponse;
import com.credshield.credshield.entity.LoanStatus;
import com.credshield.credshield.repository.LoanRepository;

import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {

    private final LoanRepository loanRepository;

    public AdminDashboardService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public DashboardSummaryResponse getDashboardSummary() {

        long totalLoans = loanRepository.count();

        long pendingLoans =
                loanRepository.countByStatus(LoanStatus.PENDING);

        long approvedLoans =
                loanRepository.countByStatus(LoanStatus.APPROVED);

        long rejectedLoans =
                loanRepository.countByStatus(LoanStatus.REJECTED);

        long closedLoans =
                loanRepository.countByStatus(LoanStatus.CLOSED);

        return new DashboardSummaryResponse(
                totalLoans,
                pendingLoans,
                approvedLoans,
                rejectedLoans,
                closedLoans
        );
    }
}
