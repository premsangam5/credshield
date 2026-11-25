
package com.credshield.credshield.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Service
public class EmiCalculator {

    private static final BigDecimal ANNUAL_INTEREST_RATE =
            new BigDecimal("0.12");

    private static final BigDecimal MONTHS_IN_YEAR =
            new BigDecimal("12");

    private static final MathContext MC =
            MathContext.DECIMAL128;

    public BigDecimal calculateEmi(
            BigDecimal principal,
            int tenureMonths) {

        if (principal == null ||
                principal.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Loan amount must be greater than zero"
            );
        }

        if (tenureMonths <= 0) {
            throw new IllegalArgumentException(
                    "Loan tenure must be greater than zero"
            );
        }

        BigDecimal monthlyRate =
                ANNUAL_INTEREST_RATE.divide(
                        MONTHS_IN_YEAR,
                        MC
                );

        BigDecimal rateFactor =
                BigDecimal.ONE.add(monthlyRate)
                        .pow(tenureMonths, MC);

        BigDecimal numerator =
                principal.multiply(monthlyRate, MC)
                        .multiply(rateFactor, MC);

        BigDecimal denominator =
                rateFactor.subtract(BigDecimal.ONE);

        BigDecimal emi = numerator.divide(
                denominator,
                MC
        );

        return emi.setScale(2, RoundingMode.HALF_UP);
    }
}
