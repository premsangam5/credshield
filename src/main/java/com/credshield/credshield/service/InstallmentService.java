
package com.credshield.credshield.service;

import com.credshield.credshield.entity.Installment;
import com.credshield.credshield.entity.InstallmentStatus;
import com.credshield.credshield.entity.Loan;
import com.credshield.credshield.entity.LoanStatus;

import com.credshield.credshield.repository.InstallmentRepository;
import com.credshield.credshield.repository.LoanRepository;

import com.credshield.credshield.exception.ResourceNotFoundException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class InstallmentService {

    private final InstallmentRepository installmentRepository;
    private final LoanRepository loanRepository;
    private final EmiCalculator emiCalculator;

    public InstallmentService(
            InstallmentRepository installmentRepository,
            LoanRepository loanRepository,
            EmiCalculator emiCalculator) {

        this.installmentRepository = installmentRepository;
        this.loanRepository = loanRepository;
        this.emiCalculator = emiCalculator;
    }

    // Generate monthly installments for an approved loan
    @Transactional
    public List<Installment> generateInstallments(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan not found with ID: " + loanId
                        )
                );

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalArgumentException(
                    "Installments can only be generated for approved loans"
            );
        }

        if (installmentRepository.existsByLoan_Id(loanId)) {
            throw new IllegalArgumentException(
                    "Installment schedule already exists for this loan"
            );
        }

        BigDecimal emi = emiCalculator.calculateEmi(
                loan.getAmount(),
                loan.getTenureMonths()
        );

        BigDecimal totalPayable = emi.multiply(
                BigDecimal.valueOf(loan.getTenureMonths())
        ).setScale(2, RoundingMode.HALF_UP);

        LocalDate firstDueDate = LocalDate.now().plusMonths(1);

        List<Installment> installments = new ArrayList<>();

        for (int i = 1; i <= loan.getTenureMonths(); i++) {

            Installment installment = new Installment();

            installment.setLoan(loan);
            installment.setInstallmentNumber(i);

            installment.setDueDate(
                    firstDueDate.plusMonths(i - 1)
            );

            BigDecimal amountDue = emi;

            if (i == loan.getTenureMonths()) {
                amountDue = totalPayable.subtract(
                        emi.multiply(
                                BigDecimal.valueOf(
                                        loan.getTenureMonths() - 1
                                )
                        )
                ).setScale(2, RoundingMode.HALF_UP);
            }

            installment.setAmountDue(amountDue);
            installment.setAmountPaid(BigDecimal.ZERO);
            installment.setStatus(InstallmentStatus.PENDING);

            installments.add(installment);
        }

        return installmentRepository.saveAll(installments);
    }

    // Get installments belonging to the logged-in customer
    @Transactional
    public List<Installment> getMyInstallments(
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
                    "You cannot access another customer's installments"
            );
        }

        // Allow viewing installments for approved and closed loans
        if (loan.getStatus() != LoanStatus.APPROVED
                && loan.getStatus() != LoanStatus.CLOSED) {

            throw new IllegalArgumentException(
                    "Installments are only available for approved or closed loans"
            );
        }

        List<Installment> installments =
                installmentRepository
                        .findByLoan_IdOrderByInstallmentNumberAsc(loanId);

        LocalDate today = LocalDate.now();

        for (Installment installment : installments) {

            if (installment.getAmountPaid()
                    .compareTo(installment.getAmountDue()) < 0
                    && installment.getDueDate().isBefore(today)) {

                installment.setStatus(InstallmentStatus.OVERDUE);
            }
        }

        return installmentRepository.saveAll(installments);
    }
}
