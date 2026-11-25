
package com.credshield.credshield.repository;

import com.credshield.credshield.entity.Installment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstallmentRepository
        extends JpaRepository<Installment, Long> {

    // Get all installments for a loan in EMI order
    List<Installment> findByLoan_IdOrderByInstallmentNumberAsc(
            Long loanId
    );

    // Check whether installments already exist for a loan
    boolean existsByLoan_Id(Long loanId);
}
