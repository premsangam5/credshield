
package com.credshield.credshield.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class LoanApplicationRequest {

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1000.00",
            message = "Minimum loan amount is 1000")
    private BigDecimal amount;

    @NotNull(message = "Loan tenure is required")
    @Min(value = 1, message = "Minimum tenure is 1 month")
    @Max(value = 360, message = "Maximum tenure is 360 months")
    private Integer tenureMonths;

    @NotBlank(message = "Loan purpose is required")
    @Size(max = 200, message = "Purpose cannot exceed 200 characters")
    private String purpose;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(Integer tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
