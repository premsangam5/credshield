
package com.credshield.credshield.dto;

import com.credshield.credshield.entity.Repayment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RepaymentResponse {

    private Long repaymentId;
    private Long loanId;
    private BigDecimal amount;
    private LocalDateTime paidAt;

    public RepaymentResponse(Repayment repayment) {

        this.repaymentId = repayment.getId();
        this.loanId = repayment.getLoan().getId();
        this.amount = repayment.getAmount();
        this.paidAt = repayment.getPaidAt();
    }

    public Long getRepaymentId() {
        return repaymentId;
    }

    public Long getLoanId() {
        return loanId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }
}
