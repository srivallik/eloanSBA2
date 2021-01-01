package com.iiht.training.eloan.service;

import java.util.List;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;

import com.iiht.training.eloan.dto.LoanDto;
import com.iiht.training.eloan.dto.LoanOutputDto;
import com.iiht.training.eloan.dto.UserDto;

public interface CustomerService {
	
	public UserDto register(UserDto userDto);
	
	public LoanOutputDto applyLoan(Long customerId,
								   LoanDto loanDto);
	
	public LoanOutputDto getStatus(Long loanAppId) throws NotFoundException;
		
	public List<LoanOutputDto> getStatusAll(Long customerId);
}
