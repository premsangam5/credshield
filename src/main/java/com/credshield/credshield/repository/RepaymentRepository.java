
package com.credshield.credshield.repository;

import com.credshield.credshield.entity.Repayment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepaymentRepository
        extends JpaRepository<Repayment, Long> {

    List<Repayment> findByLoan_IdOrderByPaidAtDesc(Long loanId);

}
