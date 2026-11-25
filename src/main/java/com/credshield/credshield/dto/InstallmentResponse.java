
package com.credshield.credshield.dto;

import com.credshield.credshield.entity.Installment;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InstallmentResponse {

    private Long installmentId;
    private Long loanId;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal amountDue;
    private BigDecimal amountPaid;
    private BigDecimal remainingAmount;
    private String status;

    public InstallmentResponse(Installment installment) {

        this.installmentId = installment.getId();

        this.loanId = installment.getLoan().getId();

        this.installmentNumber =
                installment.getInstallmentNumber();

        this.dueDate = installment.getDueDate();

        this.amountDue = installment.getAmountDue();

        this.amountPaid = installment.getAmountPaid();

        this.remainingAmount = installment.getAmountDue()
                .subtract(installment.getAmountPaid());

        this.status = installment.getStatus().name();
    }

    public Long getInstallmentId() {
        return installmentId;
    }

    public Long getLoanId() {
        return loanId;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public BigDecimal getAmountDue() {
        return amountDue;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public String getStatus() {
        return status;
    }
}
