
package com.credshield.credshield.repository;

import com.credshield.credshield.entity.Loan;
import com.credshield.credshield.entity.LoanStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByApplicantEmail(String applicantEmail);

    List<Loan> findByStatus(LoanStatus status);

    long countByStatus(LoanStatus status);

    Page<Loan> findByApplicantEmailContainingIgnoreCase(
            String email,
            Pageable pageable
    );

    Page<Loan> findByStatus(
            LoanStatus status,
            Pageable pageable
    );

    Page<Loan> findByApplicantEmailContainingIgnoreCaseAndStatus(
            String email,
            LoanStatus status,
            Pageable pageable
    );
}
