
package com.credshield.credshield.service;

import com.credshield.credshield.dto.LoanApplicationRequest;
import com.credshield.credshield.entity.Loan;
import com.credshield.credshield.entity.LoanStatus;
import com.credshield.credshield.exception.ResourceNotFoundException;
import com.credshield.credshield.repository.LoanRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private InstallmentService installmentService;

    @InjectMocks
    private LoanService loanService;

    // Test 1: Apply for a new loan
    @Test
    void shouldApplyForLoanSuccessfully() {

        LoanApplicationRequest request =
                new LoanApplicationRequest();

        request.setAmount(new BigDecimal("50000"));
        request.setTenureMonths(12);
        request.setPurpose("Education");

        String applicantEmail = "customer@test.com";

        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Loan result = loanService.applyForLoan(
                request,
                applicantEmail
        );

        assertEquals(applicantEmail, result.getApplicantEmail());
        assertEquals(new BigDecimal("50000"), result.getAmount());
        assertEquals(12, result.getTenureMonths());
        assertEquals("Education", result.getPurpose());
        assertEquals(LoanStatus.PENDING, result.getStatus());

        verify(loanRepository).save(any(Loan.class));

        verify(auditLogService).logAction(
                eq("LOAN_APPLIED"),
                eq(applicantEmail),
                contains("Amount: 50000")
        );
    }

    // Test 2: Approve a pending loan
    @Test
    void shouldApprovePendingLoanAndGenerateInstallments() {

        Long loanId = 1L;
        String adminEmail = "admin@test.com";

        Loan loan = createLoan(LoanStatus.PENDING);

        when(loanRepository.findById(loanId))
                .thenReturn(Optional.of(loan));

        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Loan result = loanService.updateLoanStatus(
                loanId,
                LoanStatus.APPROVED,
                adminEmail
        );

        assertEquals(LoanStatus.APPROVED, result.getStatus());

        verify(loanRepository).save(loan);
        verify(installmentService).generateInstallments(loanId);

        verify(auditLogService).logAction(
                eq("LOAN_APPROVED"),
                eq(adminEmail),
                contains("New Status: APPROVED")
        );
    }

    // Test 3: Reject a pending loan
    @Test
    void shouldRejectPendingLoanWithoutGeneratingInstallments() {

        Long loanId = 2L;
        String adminEmail = "admin@test.com";

        Loan loan = createLoan(LoanStatus.PENDING);

        when(loanRepository.findById(loanId))
                .thenReturn(Optional.of(loan));

        when(loanRepository.save(any(Loan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Loan result = loanService.updateLoanStatus(
                loanId,
                LoanStatus.REJECTED,
                adminEmail
        );

        assertEquals(LoanStatus.REJECTED, result.getStatus());

        verify(loanRepository).save(loan);

        verify(installmentService, never())
                .generateInstallments(anyLong());

        verify(auditLogService).logAction(
                eq("LOAN_REJECTED"),
                eq(adminEmail),
                contains("New Status: REJECTED")
        );
    }

    // Test 4: Loan does not exist
    @Test
    void shouldThrowExceptionWhenLoanDoesNotExist() {

        Long loanId = 100L;

        when(loanRepository.findById(loanId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> loanService.updateLoanStatus(
                        loanId,
                        LoanStatus.APPROVED,
                        "admin@test.com"
                )
        );

        verify(loanRepository, never()).save(any(Loan.class));

        verify(installmentService, never())
                .generateInstallments(anyLong());

        verifyNoInteractions(auditLogService);
    }

    // Test 5: Already approved loan cannot be updated
    @Test
    void shouldThrowExceptionWhenLoanIsAlreadyApproved() {

        Long loanId = 3L;

        Loan loan = createLoan(LoanStatus.APPROVED);

        when(loanRepository.findById(loanId))
                .thenReturn(Optional.of(loan));

        assertThrows(
                IllegalArgumentException.class,
                () -> loanService.updateLoanStatus(
                        loanId,
                        LoanStatus.REJECTED,
                        "admin@test.com"
                )
        );

        verify(loanRepository, never()).save(any(Loan.class));

        verify(installmentService, never())
                .generateInstallments(anyLong());

        verifyNoInteractions(auditLogService);
    }

    // Helper method: Create a loan
    private Loan createLoan(LoanStatus status) {

        Loan loan = new Loan();

        loan.setApplicantEmail("customer@test.com");
        loan.setAmount(new BigDecimal("50000"));
        loan.setTenureMonths(12);
        loan.setPurpose("Education");
        loan.setStatus(status);

        return loan;
    }
}
