
package com.credshield.credshield.dto;

import java.math.BigDecimal;

public class RepaymentSummaryResponse {

    private Long loanId;
    private BigDecimal principal;
    private Integer tenureMonths;
    private BigDecimal emi;
    private BigDecimal totalPayable;
    private BigDecimal totalPaid;
    private BigDecimal outstandingBalance;

    public RepaymentSummaryResponse(
            Long loanId,
            BigDecimal principal,
            Integer tenureMonths,
            BigDecimal emi,
            BigDecimal totalPayable,
            BigDecimal totalPaid,
            BigDecimal outstandingBalance) {

        this.loanId = loanId;
        this.principal = principal;
        this.tenureMonths = tenureMonths;
        this.emi = emi;
        this.totalPayable = totalPayable;
        this.totalPaid = totalPaid;
        this.outstandingBalance = outstandingBalance;
    }

    public Long getLoanId() {
        return loanId;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public BigDecimal getEmi() {
        return emi;
    }

    public BigDecimal getTotalPayable() {
        return totalPayable;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }
}
