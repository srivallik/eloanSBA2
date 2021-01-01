package com.iiht.training.eloan.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iiht.training.eloan.dto.LoanDto;
import com.iiht.training.eloan.dto.LoanOutputDto;
import com.iiht.training.eloan.dto.ProcessingDto;
import com.iiht.training.eloan.dto.SanctionOutputDto;
import com.iiht.training.eloan.dto.UserDto;
import com.iiht.training.eloan.entity.Loan;
import com.iiht.training.eloan.entity.ProcessingInfo;
import com.iiht.training.eloan.entity.SanctionInfo;
import com.iiht.training.eloan.entity.Users;
import com.iiht.training.eloan.repository.LoanRepository;
import com.iiht.training.eloan.repository.ProcessingInfoRepository;
import com.iiht.training.eloan.repository.SanctionInfoRepository;
import com.iiht.training.eloan.repository.UsersRepository;
import com.iiht.training.eloan.service.ClerkService;

@Service
//@Qualifier("cLerkService")
public class ClerkServiceImpl implements ClerkService {

	@Autowired
	private UsersRepository usersRepository;
	
	@Autowired
	private LoanRepository loanRepository;
	
	@Autowired
	private ProcessingInfoRepository processingInfoRepository;
	
	@Autowired
	private SanctionInfoRepository sanctionInfoRepository;
	
	private static final ModelMapper modelMapper = new ModelMapper();
	
	@Override
	public List<LoanOutputDto> allAppliedLoans() {
		
		List<Loan> appliedLoansList = loanRepository.allAppliedLoans();
		List<LoanOutputDto> dtos = appliedLoansList
				  .stream()
				  .map(loan -> mapLoanOutputDto(loan))
				  .collect(Collectors.toList());
		return dtos;
	}

	@Override
	public ProcessingDto processLoan(Long clerkId, Long loanAppId, ProcessingDto processingDto) {
		
		Loan loan = null;
		Optional<Loan> obj = loanRepository.findById(loanAppId);
		if(obj.isPresent()) {
		    loan = obj.orElse(new Loan());
			loan.setStatus(1);
			loan.setRemark("Processed");
			loanRepository.save(loan);
		}
        ProcessingInfo processingInfo = new ProcessingInfo();
        processingInfo.setLoanAppId(loanAppId);
        processingInfo.setLoanClerkId(clerkId);
		modelMapper.map(processingDto, processingInfo);
		processingInfoRepository.save(processingInfo);
		return processingDto;
	}
	
    private LoanOutputDto mapLoanOutputDto(Loan loan) {
		
		LoanOutputDto loanOutputDto = null;
		
		LoanDto loanDto = modelMapper.map(loan, LoanDto.class);
		
		Optional <Users> obj1 = usersRepository.findById(loan.getCustomerId());
		Users users = obj1.isPresent() ? obj1.orElse(new Users()) : new Users();
		
		UserDto userDto = new UserDto();
		userDto.setEmail(users.getEmail());
		userDto.setFirstName(users.getFirstName());
		userDto.setId(users.getId());
		userDto.setLastName(users.getLastName());
		userDto.setMobile(users.getMobile());
		
		Optional<ProcessingInfo> obj2 = processingInfoRepository.getProcessedLoanByLoanAppId(loan.getId());
		ProcessingInfo processingInfo = obj2.isPresent() ? obj2.orElse(new ProcessingInfo()) : new ProcessingInfo();
		
		ProcessingDto processingDto = new ProcessingDto();
		processingDto.setAcresOfLand(processingInfo.getAcresOfLand());
		processingDto.setAddressOfProperty(processingInfo.getAddressOfProperty());
		processingDto.setAppraisedBy(processingInfo.getAppraisedBy());
		processingDto.setLandValue(processingInfo.getLandValue());
		processingDto.setSuggestedAmountOfLoan(processingInfo.getSuggestedAmountOfLoan());
		processingDto.setValuationDate(processingInfo.getValuationDate());
		
		Optional<SanctionInfo> obj3 = sanctionInfoRepository.getSanctionedLoanByLoanAppId(loan.getId());
		SanctionInfo sanctionInfo = obj3.isPresent() ? obj3.orElse(new SanctionInfo()) : new SanctionInfo();
		
		SanctionOutputDto sanctionOutputDto = new SanctionOutputDto();
		sanctionOutputDto.setLoanAmountSanctioned(sanctionInfo.getLoanAmountSanctioned());
		sanctionOutputDto.setLoanClosureDate(sanctionInfo.getLoanClosureDate());
		sanctionOutputDto.setMonthlyPayment(sanctionInfo.getMonthlyPayment());
		sanctionOutputDto.setPaymentStartDate(sanctionInfo.getPaymentStartDate());
		sanctionOutputDto.setTermOfLoan(sanctionInfo.getTermOfLoan());
		
		
	    loanOutputDto = new LoanOutputDto();
		loanOutputDto.setCustomerId(loan.getCustomerId());
		loanOutputDto.setLoanAppId(loan.getId());
		loanOutputDto.setLoanDto(loanDto);
		loanOutputDto.setProcessingDto(processingDto);
		loanOutputDto.setRemark(loan.getRemark());
		loanOutputDto.setSanctionOutputDto(sanctionOutputDto);
		loanOutputDto.setStatus(String.valueOf(loan.getStatus()));
		loanOutputDto.setUserDto(userDto);
		
		return loanOutputDto;
	}
}
