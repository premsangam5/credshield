
package com.credshield.credshield.controller;

import com.credshield.credshield.dto.AuditLogResponse;
import com.credshield.credshield.dto.InstallmentResponse;
import com.credshield.credshield.dto.UpdateLoanStatusRequest;

import com.credshield.credshield.entity.AuditLog;
import com.credshield.credshield.entity.Installment;

import com.credshield.credshield.service.AuditLogService;
import com.credshield.credshield.service.InstallmentService;
import com.credshield.credshield.service.LoanService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final LoanService loanService;
    private final AuditLogService auditLogService;
    private final InstallmentService installmentService;

    public AdminController(
            LoanService loanService,
            AuditLogService auditLogService,
            InstallmentService installmentService) {

        this.loanService = loanService;
        this.auditLogService = auditLogService;
        this.installmentService = installmentService;
    }

    // Get all loan applications
    @GetMapping("/loans")
    public ResponseEntity<?> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    // Search and filter loans
    @GetMapping("/loans/search")
    public ResponseEntity<?> searchLoans(
            @RequestParam(required = false) String email,
            @RequestParam(required = false)
            com.credshield.credshield.entity.LoanStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        return ResponseEntity.ok(
                loanService.searchLoans(
                        email,
                        status,
                        page,
                        size,
                        sortBy,
                        direction
                )
        );
    }

    // Approve or reject a loan
    @PatchMapping("/loans/{id}/status")
    public ResponseEntity<?> updateLoanStatus(
            @PathVariable Long id,
            @RequestBody UpdateLoanStatusRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                loanService.updateLoanStatus(
                        id,
                        request.getStatus(),
                        authentication.getName()
                )
        );
    }

    // Generate EMI schedule for an approved loan
    @PostMapping("/loans/{loanId}/installments/generate")
    public ResponseEntity<?> generateInstallments(
            @PathVariable Long loanId) {

        List<Installment> installments =
                installmentService.generateInstallments(loanId);

        List<InstallmentResponse> response = installments.stream()
                .map(InstallmentResponse::new)
                .toList();

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Installment schedule generated successfully",
                        "installments",
                        response
                )
        );
    }

    // Get audit logs
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs() {

        List<AuditLogResponse> response = auditLogService
                .getAllAuditLogs()
                .stream()
                .map(AuditLogResponse::new)
                .toList();

        return ResponseEntity.ok(response);
    }
}
