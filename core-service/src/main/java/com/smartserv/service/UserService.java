package com.smartserv.service;

import java.util.List;
import org.springframework.data.domain.Page;
import com.smartserv.entity.Role;
import com.smartserv.dto.CreateUserDto;
import com.smartserv.dto.UpdateUserDto;
import com.smartserv.dto.UserResponseDto;

public interface UserService {

	UserResponseDto createUser(CreateUserDto dto);

	List<UserResponseDto> getUsers();

	Page<UserResponseDto> getUsers(int page, int size, Role role);

	UserResponseDto getUserById(Long userId);

	UserResponseDto updateUser(Long targetUserId, UpdateUserDto dto);

	void deleteUser(Long userId);

	List<UserResponseDto> findActiveUsers();

	List<UserResponseDto> getAllCustomers();

	List<UserResponseDto> getAllManagers();
	
	List<UserResponseDto> getAllMechanics();

	UserResponseDto getCustomerById(Long customerId);

	UserResponseDto getManager(Long managerId);

	UserResponseDto getMechanic(Long mechanicId);

	List<UserResponseDto> getMechanicsUnderManager(Long managerId);

	UserResponseDto assignManagerToMechanic(Long mechanicId, Long managerId);
	
	
	
}
