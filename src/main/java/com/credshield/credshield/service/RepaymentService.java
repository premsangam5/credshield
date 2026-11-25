
package com.credshield.credshield.service;

import com.credshield.credshield.dto.RepaymentSummaryResponse;
import com.credshield.credshield.entity.Loan;
import com.credshield.credshield.entity.LoanStatus;
import com.credshield.credshield.entity.Repayment;
import com.credshield.credshield.entity.Installment;
import com.credshield.credshield.entity.InstallmentStatus;
import com.credshield.credshield.repository.LoanRepository;
import com.credshield.credshield.repository.RepaymentRepository;
import com.credshield.credshield.repository.InstallmentRepository;
import com.credshield.credshield.exception.ResourceNotFoundException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RepaymentService {

    private final LoanRepository loanRepository;
    private final RepaymentRepository repaymentRepository;
    private final InstallmentRepository installmentRepository;
    private final EmiCalculator emiCalculator;
    private final AuditLogService auditLogService;

    public RepaymentService(
            LoanRepository loanRepository,
            RepaymentRepository repaymentRepository,
            InstallmentRepository installmentRepository,
            EmiCalculator emiCalculator,
            AuditLogService auditLogService) {

        this.loanRepository = loanRepository;
        this.repaymentRepository = repaymentRepository;
        this.installmentRepository = installmentRepository;
        this.emiCalculator = emiCalculator;
        this.auditLogService = auditLogService;
    }

    // Make a loan repayment
    @Transactional
    public Repayment makeRepayment(
            Long loanId,
            BigDecimal amount,
            String applicantEmail) {

        Loan loan = getCustomerLoan(loanId, applicantEmail);

        if (loan.getStatus() == LoanStatus.CLOSED) {
            throw new IllegalArgumentException(
                    "This loan is already closed"
            );
        }

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalArgumentException(
                    "Only approved loans can receive repayments"
            );
        }

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Repayment amount must be greater than zero"
            );
        }

        BigDecimal paymentAmount =
                amount.setScale(2, RoundingMode.HALF_UP);

        List<Installment> installments =
                installmentRepository
                        .findByLoan_IdOrderByInstallmentNumberAsc(loanId);

        if (installments.isEmpty()) {
            throw new IllegalStateException(
                    "Installment schedule has not been generated"
            );
        }

        BigDecimal totalPayable = installments.stream()
                .map(Installment::getAmountDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        List<Repayment> previousRepayments =
                repaymentRepository
                        .findByLoan_IdOrderByPaidAtDesc(loanId);

        BigDecimal totalPaid = previousRepayments.stream()
                .map(Repayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal outstandingBalance =
                totalPayable.subtract(totalPaid);

        if (outstandingBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "Existing repayments exceed total loan payable"
            );
        }

        if (paymentAmount.compareTo(outstandingBalance) > 0) {
            throw new IllegalArgumentException(
                    "Payment exceeds outstanding balance: "
                            + outstandingBalance
            );
        }

        Repayment repayment = new Repayment();
        repayment.setLoan(loan);
        repayment.setAmount(paymentAmount);

        Repayment savedRepayment =
                repaymentRepository.save(repayment);

        List<Repayment> allRepayments =
                new ArrayList<>(previousRepayments);

        allRepayments.add(savedRepayment);

        allocateRepayments(installments, allRepayments);

        installmentRepository.saveAll(installments);

        // Automatically close loan when all installments are paid
        boolean allInstallmentsPaid = installments.stream()
                .allMatch(installment ->
                        installment.getStatus() == InstallmentStatus.PAID
                );

        if (allInstallmentsPaid) {

            loan.setStatus(LoanStatus.CLOSED);

            loanRepository.save(loan);

            auditLogService.logAction(
                    "LOAN_CLOSED",
                    applicantEmail,
                    "Loan ID: " + loanId
                            + " fully repaid and closed"
            );
        }

        auditLogService.logAction(
                "LOAN_REPAYMENT",
                applicantEmail,
                "Loan ID: " + loanId
                        + ", Amount: " + savedRepayment.getAmount()
        );

        return savedRepayment;
    }

    // Allocate repayments to oldest unpaid installments first
    private void allocateRepayments(
            List<Installment> installments,
            List<Repayment> repayments) {

        for (Installment installment : installments) {

            installment.setAmountPaid(BigDecimal.ZERO);
            installment.setPaidAt(null);

            if (installment.getDueDate().isBefore(LocalDate.now())) {
                installment.setStatus(InstallmentStatus.OVERDUE);
            } else {
                installment.setStatus(InstallmentStatus.PENDING);
            }
        }

        repayments.sort(
                Comparator.comparing(Repayment::getPaidAt)
                        .thenComparing(Repayment::getId)
        );

        for (Repayment repayment : repayments) {

            BigDecimal remainingPayment = repayment.getAmount();

            for (Installment installment : installments) {

                if (remainingPayment.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }

                BigDecimal remainingDue =
                        installment.getAmountDue()
                                .subtract(installment.getAmountPaid());

                if (remainingDue.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                BigDecimal allocation =
                        remainingPayment.min(remainingDue);

                BigDecimal updatedPaid =
                        installment.getAmountPaid().add(allocation);

                installment.setAmountPaid(updatedPaid);

                remainingPayment =
                        remainingPayment.subtract(allocation);

                if (updatedPaid.compareTo(
                        installment.getAmountDue()) >= 0) {

                    installment.setStatus(InstallmentStatus.PAID);
                    installment.setPaidAt(repayment.getPaidAt());

                } else if (installment.getDueDate()
                        .isBefore(LocalDate.now())) {

                    installment.setStatus(InstallmentStatus.OVERDUE);

                } else {

                    installment.setStatus(
                            InstallmentStatus.PARTIALLY_PAID
                    );
                }
            }

            if (remainingPayment.compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalStateException(
                        "Unable to allocate the complete repayment"
                );
            }
        }
    }

    // Get customer's repayment history
    @Transactional(readOnly = true)
    public List<Repayment> getMyRepayments(
            Long loanId,
            String applicantEmail) {

        Loan loan = getCustomerLoan(loanId, applicantEmail);

        return repaymentRepository
                .findByLoan_IdOrderByPaidAtDesc(loan.getId());
    }

    // Get customer's repayment summary
    @Transactional(readOnly = true)
    public RepaymentSummaryResponse getRepaymentSummary(
            Long loanId,
            String applicantEmail) {

        Loan loan = getCustomerLoan(loanId, applicantEmail);

        BigDecimal emi = emiCalculator.calculateEmi(
                loan.getAmount(),
                loan.getTenureMonths()
        );

        List<Installment> installments =
                installmentRepository
                        .findByLoan_IdOrderByInstallmentNumberAsc(loanId);

        BigDecimal totalPayable;

        if (installments.isEmpty()) {
            totalPayable = emi.multiply(
                    BigDecimal.valueOf(loan.getTenureMonths())
            ).setScale(2, RoundingMode.HALF_UP);
        } else {
            totalPayable = installments.stream()
                    .map(Installment::getAmountDue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        List<Repayment> repayments =
                repaymentRepository
                        .findByLoan_IdOrderByPaidAtDesc(loanId);

        BigDecimal totalPaid = repayments.stream()
                .map(Repayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal outstandingBalance = totalPayable
                .subtract(totalPaid)
                .setScale(2, RoundingMode.HALF_UP);

        return new RepaymentSummaryResponse(
                loan.getId(),
                loan.getAmount(),
                loan.getTenureMonths(),
                emi,
                totalPayable,
                totalPaid,
                outstandingBalance
        );
    }

    // Check loan ownership
    private Loan getCustomerLoan(
            Long loanId,
            String applicantEmail) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan not found with ID: " + loanId
                        )
                );

        if (!loan.getApplicantEmail().equals(applicantEmail)) {
            throw new AccessDeniedException(
                    "You cannot access another customer's loan"
            );
        }

        return loan;
    }
}
