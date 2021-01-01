package com.iiht.training.eloan.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import com.iiht.training.eloan.dto.RejectDto;
import com.iiht.training.eloan.dto.SanctionDto;
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
import com.iiht.training.eloan.service.ManagerService;

@Service
//@Qualifier("mAnagerService")
public class ManagerServiceImpl implements ManagerService {

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
	public List<LoanOutputDto> allProcessedLoans() {
		List<ProcessingInfo> processingInfoList = processingInfoRepository.getAllProcessedLoans();
		List<LoanOutputDto> dtos = processingInfoList
				  .stream()
				  .map(processingInfo -> {
					try {
						return mapLoanOutputDto(processingInfo.getLoanAppId());
					} catch (NotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;
				})
				  .collect(Collectors.toList());
		return dtos;
		
	}

	@Override
	public RejectDto rejectLoan(Long managerId, Long loanAppId, RejectDto rejectDto) throws NotFoundException {
		
		Optional<Loan> obj = loanRepository.findById(loanAppId);
		Loan loan = obj.orElseThrow(() -> new NotFoundException());
		loan.setStatus(-1);
		loan.setRemark("Rejected");
		loanRepository.save(loan);
		return rejectDto;
	}

	@Override
	public SanctionOutputDto sanctionLoan(Long managerId, Long loanAppId, SanctionDto sanctionDto) {
		
		Optional<Loan> obj = loanRepository.findById(loanAppId);
		if(obj.isPresent()) {
			Loan loan = obj.orElse(new Loan());
			loan.setStatus(2);
			loan.setRemark("Sanctioned");
			loanRepository.save(loan);
		}
		// Input Dates should be in "dd/MM/yyyy" formats only
	    DateTimeFormatter stringToLocalDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	    DateTimeFormatter localDateToStringFormatter = DateTimeFormatter.ofPattern("d/MM/uuuu");
	    LocalDate paymentStartDate = LocalDate.parse(sanctionDto.getPaymentStartDate(), stringToLocalDateFormatter); 
	    LocalDate loanClosureDate = paymentStartDate.plusMonths((long) (sanctionDto.getTermOfLoan()*12)); 
	    String loanClosureDateStr = loanClosureDate.format(localDateToStringFormatter);
	    
	    /* Formulae  
	     a. Term payment amount = (Sanctioned loan amount ) * (1 + interest rate/100) ^ (term of loan)
	     b. Monthly payment = (Term payment amount ) / (Term of loan)
        */
	    
	    double termPaymentAmount = (sanctionDto.getLoanAmountSanctioned())* Math.pow((1+((7/12)/100)),sanctionDto.getTermOfLoan());
		double monthlyPayment = (termPaymentAmount / (sanctionDto.getTermOfLoan()*12));
	    
		SanctionInfo sanctionInfo = new SanctionInfo();
		modelMapper.map(sanctionDto, sanctionInfo);
		sanctionInfo.setLoanAppId(loanAppId);
		sanctionInfo.setManagerId(managerId);
		sanctionInfo.setLoanClosureDate(loanClosureDateStr);
		sanctionInfo.setMonthlyPayment(monthlyPayment);
		sanctionInfoRepository.save(sanctionInfo);
		
	    SanctionOutputDto sanctionOutputDto = new SanctionOutputDto();
		sanctionOutputDto.setLoanAmountSanctioned(sanctionDto.getLoanAmountSanctioned());
		sanctionOutputDto.setTermOfLoan(sanctionDto.getTermOfLoan());
		sanctionOutputDto.setPaymentStartDate(sanctionDto.getPaymentStartDate());
		sanctionOutputDto.setLoanClosureDate(loanClosureDateStr);
		sanctionOutputDto.setMonthlyPayment(monthlyPayment);
		return sanctionOutputDto;
	}
	
    private LoanOutputDto mapLoanOutputDto(Long loanAppId) throws NotFoundException {
		
		LoanOutputDto loanOutputDto = null;
		
		Optional<Loan> obj = loanRepository.findById(loanAppId);
		Loan loan = obj.orElseThrow(() -> new NotFoundException());
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
