
package com.credshield.credshield.dto;

import com.credshield.credshield.entity.Loan;
import com.credshield.credshield.entity.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoanResponse {

    private Long id;
    private String applicantEmail;
    private BigDecimal amount;
    private Integer tenureMonths;
    private String purpose;
    private LoanStatus status;
    private LocalDateTime appliedAt;

    public LoanResponse(Loan loan) {
        this.id = loan.getId();
        this.applicantEmail = loan.getApplicantEmail();
        this.amount = loan.getAmount();
        this.tenureMonths = loan.getTenureMonths();
        this.purpose = loan.getPurpose();
        this.status = loan.getStatus();
        this.appliedAt = loan.getAppliedAt();
    }

    public Long getId() {
        return id;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public String getPurpose() {
        return purpose;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }
}
