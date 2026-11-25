
package com.credshield.credshield.dto;

import com.credshield.credshield.entity.LoanStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateLoanStatusRequest {

    @NotNull(message = "Loan status is required")
    private LoanStatus status;

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }
}
