package com.iiht.training.eloan.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iiht.training.eloan.entity.ProcessingInfo;
import com.iiht.training.eloan.entity.SanctionInfo;

@Repository
public interface SanctionInfoRepository extends JpaRepository<SanctionInfo, Long>{

	@Query(value = "SELECT * FROM eloandb.sanction_info u WHERE u.loan_app_id = :loanAppId", nativeQuery = true)
	Optional<SanctionInfo> getSanctionedLoanByLoanAppId(@Param("loanAppId") Long loanAppId);
}
