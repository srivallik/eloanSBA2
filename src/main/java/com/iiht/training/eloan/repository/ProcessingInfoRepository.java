package com.iiht.training.eloan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iiht.training.eloan.entity.ProcessingInfo;

@Repository
public interface ProcessingInfoRepository extends JpaRepository<ProcessingInfo, Long>{
	
	@Query(value = "SELECT * FROM eloandb.processing_info", nativeQuery = true)
	List<ProcessingInfo> getAllProcessedLoans();
	
	@Query(value = "SELECT * FROM eloandb.processing_info u WHERE u.loan_app_id = :loanAppId", nativeQuery = true)
	Optional<ProcessingInfo> getProcessedLoanByLoanAppId(@Param("loanAppId") Long loanAppId);
}
