package com.iiht.training.eloan.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
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
import com.iiht.training.eloan.service.CustomerService;

@Service
//@Qualifier("cUstomerService")
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	private UsersRepository usersRepository;
	
	@Autowired
	private LoanRepository loanRepository;
	
	@Autowired
	private ProcessingInfoRepository processingInfoRaepository;
	
	@Autowired
	private SanctionInfoRepository sanctionInfoRepository;

	private static final ModelMapper modelMapper = new ModelMapper();
	
	@Override
	public UserDto register(UserDto userDto) {
		Users users =  new Users();
		users.setRole("Customer");
		modelMapper.map(userDto, users);
		usersRepository.save(users);
		return userDto;
	}

	@Override
	public LoanOutputDto applyLoan(Long customerId, LoanDto loanDto) {
		Loan loan = new Loan();
		loan.setCustomerId(customerId);
		loan.setStatus(0);
		loan.setRemark("Applied");
		modelMapper.map(loanDto, loan);
		loanRepository.save(loan);
		LoanOutputDto loanOutputDto = mapLoanOutputDto(loan);
		return loanOutputDto;
	}
	

	@Override
	public LoanOutputDto getStatus(Long loanAppId) throws NotFoundException {
		
		Optional<Loan> obj = loanRepository.findById(loanAppId);
		Loan loan = obj.orElseThrow(() -> new NotFoundException());
		LoanOutputDto loanOutputDto = mapLoanOutputDto(loan);
	    return loanOutputDto;
	}

	@Override
	public List<LoanOutputDto> getStatusAll(Long customerId) {
		List<Loan> loans = loanRepository.getAllLoansByCustomerId(customerId);
		List<LoanOutputDto> dtos = loans
				  .stream()
				  .map(loan -> mapLoanOutputDto(loan))
				  .collect(Collectors.toList());
		return dtos;
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
		
		Optional<ProcessingInfo> obj2 = processingInfoRaepository.getProcessedLoanByLoanAppId(loan.getId());
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
