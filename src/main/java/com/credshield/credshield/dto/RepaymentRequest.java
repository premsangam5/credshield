
package com.credshield.credshield.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class RepaymentRequest {

    @NotNull(message = "Repayment amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Repayment amount must be greater than zero"
    )
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
