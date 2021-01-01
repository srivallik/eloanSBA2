package com.iiht.training.eloan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.iiht.training.eloan.dto.UserDto;
import com.iiht.training.eloan.entity.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long>{

	@Query(value = "SELECT * FROM eloandb.USERS u WHERE u.role = 'Clerk'", nativeQuery = true)
	List<Users> getAllClerks();
	
	@Query(value = "SELECT * FROM eloandb.USERS u WHERE u.role = 'Manager'", nativeQuery = true)
	List<Users> getAllManagers();
}
