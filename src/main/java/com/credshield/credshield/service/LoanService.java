
package com.credshield.credshield.service;

import com.credshield.credshield.dto.LoanApplicationRequest;
import com.credshield.credshield.entity.Loan;
import com.credshield.credshield.entity.LoanStatus;
import com.credshield.credshield.repository.LoanRepository;
import com.credshield.credshield.exception.ResourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LoanService {

    private static final Logger logger =
            LoggerFactory.getLogger(LoanService.class);

    private final LoanRepository loanRepository;
    private final AuditLogService auditLogService;
    private final InstallmentService installmentService;

    public LoanService(
            LoanRepository loanRepository,
            AuditLogService auditLogService,
            InstallmentService installmentService) {

        this.loanRepository = loanRepository;
        this.auditLogService = auditLogService;
        this.installmentService = installmentService;
    }

    public Loan applyForLoan(
            LoanApplicationRequest request,
            String applicantEmail) {

        logger.info("Loan application received");

        Loan loan = new Loan();

        loan.setApplicantEmail(applicantEmail);
        loan.setAmount(request.getAmount());
        loan.setTenureMonths(request.getTenureMonths());
        loan.setPurpose(request.getPurpose());
        loan.setStatus(LoanStatus.PENDING);

        Loan savedLoan = loanRepository.save(loan);

        auditLogService.logAction(
                "LOAN_APPLIED",
                applicantEmail,
                "Loan ID: " + savedLoan.getId()
                        + ", Amount: " + savedLoan.getAmount()
        );

        logger.info("Loan application saved successfully. Loan ID: {}",
                savedLoan.getId());

        return savedLoan;
    }

    public List<Loan> getMyLoans(String applicantEmail) {

        logger.info("Customer loan history requested");

        return loanRepository.findByApplicantEmail(applicantEmail);
    }

    public List<Loan> getAllLoans() {

        logger.info("Admin requested all loan applications");

        return loanRepository.findAll();
    }

    public Page<Loan> searchLoans(
            String email,
            LoanStatus status,
            int page,
            int size,
            String sortBy,
            String direction) {

        logger.info("Admin loan search requested");

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Invalid page or size"
            );
        }

        if (!List.of("id", "amount").contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sort field. Use id or amount"
            );
        }

        Sort.Direction sortDirection;

        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid sort direction. Use ASC or DESC"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        boolean hasEmail = email != null && !email.isBlank();
        boolean hasStatus = status != null;

        if (hasEmail && hasStatus) {
            return loanRepository
                    .findByApplicantEmailContainingIgnoreCaseAndStatus(
                            email.trim(),
                            status,
                            pageable
                    );
        }

        if (hasEmail) {
            return loanRepository
                    .findByApplicantEmailContainingIgnoreCase(
                            email.trim(),
                            pageable
                    );
        }

        if (hasStatus) {
            return loanRepository.findByStatus(status, pageable);
        }

        return loanRepository.findAll(pageable);
    }

    @Transactional
    public Loan updateLoanStatus(
            Long loanId,
            LoanStatus newStatus,
            String adminEmail) {

        logger.info("Admin loan status update requested. Loan ID: {}",
                loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    logger.warn("Loan not found. Loan ID: {}", loanId);

                    return new ResourceNotFoundException(
                            "Loan not found with ID: " + loanId
                    );
                });

        if (newStatus == null || newStatus == LoanStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Admin can only approve or reject a loan"
            );
        }

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending loans can be updated"
            );
        }

        loan.setStatus(newStatus);

        Loan updatedLoan = loanRepository.save(loan);

        // Automatically generate EMI schedule after approval
        if (newStatus == LoanStatus.APPROVED) {

            installmentService.generateInstallments(loanId);

            logger.info(
                    "EMI schedule generated for approved loan. Loan ID: {}",
                    loanId
            );
        }

        String action = newStatus == LoanStatus.APPROVED
                ? "LOAN_APPROVED"
                : "LOAN_REJECTED";

        auditLogService.logAction(
                action,
                adminEmail,
                "Loan ID: " + loanId
                        + ", New Status: " + newStatus
        );

        logger.info(
                "Loan status updated. Loan ID: {}, Status: {}",
                loanId,
                newStatus
        );

        return updatedLoan;
    }
}
