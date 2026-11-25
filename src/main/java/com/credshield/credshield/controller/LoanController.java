
package com.credshield.credshield.controller;

import com.credshield.credshield.dto.LoanApplicationRequest;
import com.credshield.credshield.dto.LoanResponse;
import com.credshield.credshield.dto.RepaymentRequest;
import com.credshield.credshield.dto.RepaymentResponse;
import com.credshield.credshield.dto.RepaymentSummaryResponse;
import com.credshield.credshield.dto.InstallmentResponse;

import com.credshield.credshield.entity.Loan;
import com.credshield.credshield.entity.Repayment;
import com.credshield.credshield.entity.Installment;

import com.credshield.credshield.service.LoanService;
import com.credshield.credshield.service.RepaymentService;
import com.credshield.credshield.service.InstallmentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;
    private final RepaymentService repaymentService;
    private final InstallmentService installmentService;

    public LoanController(
            LoanService loanService,
            RepaymentService repaymentService,
            InstallmentService installmentService) {

        this.loanService = loanService;
        this.repaymentService = repaymentService;
        this.installmentService = installmentService;
    }

    // Apply for a loan
    @PostMapping("/apply")
    public ResponseEntity<?> applyForLoan(
            @Valid @RequestBody LoanApplicationRequest request,
            Authentication authentication) {

        String applicantEmail = authentication.getName();

        Loan loan = loanService.applyForLoan(
                request,
                applicantEmail
        );

        Map<String, Object> response = Map.of(
                "message", "Loan application submitted successfully",
                "loan", new LoanResponse(loan)
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // Get logged-in user's loans
    @GetMapping("/my-loans")
    public ResponseEntity<List<LoanResponse>> getMyLoans(
            Authentication authentication) {

        String applicantEmail = authentication.getName();

        List<LoanResponse> loans = loanService
                .getMyLoans(applicantEmail)
                .stream()
                .map(LoanResponse::new)
                .toList();

        return ResponseEntity.ok(loans);
    }

    // Make a loan repayment
    @PostMapping("/{loanId}/repayments")
    public ResponseEntity<?> makeRepayment(
            @PathVariable Long loanId,
            @Valid @RequestBody RepaymentRequest request,
            Authentication authentication) {

        String applicantEmail = authentication.getName();

        Repayment repayment = repaymentService.makeRepayment(
                loanId,
                request.getAmount(),
                applicantEmail
        );

        Map<String, Object> response = Map.of(
                "message", "Loan repayment successful",
                "repayment", new RepaymentResponse(repayment)
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // Get repayment history
    @GetMapping("/{loanId}/repayments")
    public ResponseEntity<List<RepaymentResponse>> getRepaymentHistory(
            @PathVariable Long loanId,
            Authentication authentication) {

        String applicantEmail = authentication.getName();

        List<RepaymentResponse> repayments = repaymentService
                .getMyRepayments(loanId, applicantEmail)
                .stream()
                .map(RepaymentResponse::new)
                .toList();

        return ResponseEntity.ok(repayments);
    }

    // Get repayment summary
    @GetMapping("/{loanId}/repayment-summary")
    public ResponseEntity<RepaymentSummaryResponse> getRepaymentSummary(
            @PathVariable Long loanId,
            Authentication authentication) {

        String applicantEmail = authentication.getName();

        RepaymentSummaryResponse summary =
                repaymentService.getRepaymentSummary(
                        loanId,
                        applicantEmail
                );

        return ResponseEntity.ok(summary);
    }

    // Get monthly EMI installment schedule
    @GetMapping("/{loanId}/installments")
    public ResponseEntity<List<InstallmentResponse>> getInstallments(
            @PathVariable Long loanId,
            Authentication authentication) {

        String applicantEmail = authentication.getName();

        List<InstallmentResponse> installments = installmentService
                .getMyInstallments(loanId, applicantEmail)
                .stream()
                .map(InstallmentResponse::new)
                .toList();

        return ResponseEntity.ok(installments);
    }
}
