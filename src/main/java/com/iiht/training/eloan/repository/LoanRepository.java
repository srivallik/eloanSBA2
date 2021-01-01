package com.iiht.training.eloan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iiht.training.eloan.entity.Loan;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long>{
	
	@Query(value = "SELECT * FROM eloandb.LOAN u WHERE u.customer_id = :customerId", nativeQuery = true)
	List<Loan> getAllLoansByCustomerId( @Param("customerId") Long customerId);
	
	@Query(value = "SELECT * FROM eloandb.LOAN u WHERE u.status = 0", nativeQuery = true)
	List<Loan> allAppliedLoans( );
}
