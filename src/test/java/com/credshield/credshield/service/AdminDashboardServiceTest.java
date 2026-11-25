package com.credshield.credshield.service;

import com.credshield.credshield.dto.DashboardSummaryResponse;
import com.credshield.credshield.entity.LoanStatus;
import com.credshield.credshield.repository.LoanRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    void shouldReturnCorrectLoanStatistics() {

        when(loanRepository.count()).thenReturn(10L);

        when(loanRepository.countByStatus(LoanStatus.PENDING))
                .thenReturn(2L);

        when(loanRepository.countByStatus(LoanStatus.APPROVED))
                .thenReturn(4L);

        when(loanRepository.countByStatus(LoanStatus.REJECTED))
                .thenReturn(1L);

        when(loanRepository.countByStatus(LoanStatus.CLOSED))
                .thenReturn(3L);

        DashboardSummaryResponse response =
                adminDashboardService.getDashboardSummary();

        assertEquals(10L, response.getTotalLoans());
        assertEquals(2L, response.getPendingLoans());
        assertEquals(4L, response.getApprovedLoans());
        assertEquals(1L, response.getRejectedLoans());
        assertEquals(3L, response.getClosedLoans());
    }
}