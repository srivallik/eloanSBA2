package com.iiht.training.eloan.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iiht.training.eloan.dto.UserDto;
import com.iiht.training.eloan.entity.Users;
import com.iiht.training.eloan.repository.UsersRepository;
import com.iiht.training.eloan.service.AdminService;

@Service
public class AdminServiceImpl implements AdminService {

	
	@Autowired
	private UsersRepository usersRepository;
	
	private static final ModelMapper modelMapper = new ModelMapper();
	
	@Override
	public UserDto registerClerk(UserDto userDto) {

		Users users =  new Users();
		users.setRole("Clerk");
		modelMapper.map(userDto, users);
		usersRepository.save(users);
		return userDto;
	}

	@Override
	public UserDto registerManager(UserDto userDto) {
		Users users = new Users();
		users.setRole("Manager");
		modelMapper.map(userDto, users);
		usersRepository.save(users);
		return userDto;
	}

	@Override
	
	public List<UserDto> getAllClerks() {
		List<Users> users = usersRepository.getAllClerks();
		List<UserDto> dtos = users
				  .stream()
				  .map(user -> modelMapper.map(user, UserDto.class))
				  .collect(Collectors.toList());
		return dtos;
	}

	@Override
	public List<UserDto> getAllManagers() {
		List<Users> users = usersRepository.getAllManagers();
		List<UserDto> dtos = users
				  .stream()
				  .map(user -> modelMapper.map(user, UserDto.class))
				  .collect(Collectors.toList());
		return dtos;
	}

}
