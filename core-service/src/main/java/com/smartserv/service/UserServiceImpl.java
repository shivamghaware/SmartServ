package com.smartserv.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartserv.dto.CreateUserDto;
import com.smartserv.dto.UpdateUserDto;
import com.smartserv.dto.UserResponseDto;
import com.smartserv.entity.Role;
import com.smartserv.entity.User;
import com.smartserv.exceptions.ResourceAlreadyExists;
import com.smartserv.exceptions.ResourceNotFoundException;
import com.smartserv.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Service
@Transactional
@RequiredArgsConstructor
@ToString
public class UserServiceImpl implements UserService {
	private final UserRepository userRepo;
	private final ModelMapper mapper;
	private final PasswordEncoder encoder;

	@Override
	public UserResponseDto createUser(CreateUserDto dto) {
		String cleanEmail = dto.getEmail() != null ? dto.getEmail().trim().toLowerCase() : null;
		dto.setEmail(cleanEmail);
		if (userRepo.existsByEmail(cleanEmail)) {
			throw new ResourceAlreadyExists("User with email " + cleanEmail + " already exists.");
		}
		User entity = mapper.map(dto, User.class);
		entity.setEmail(cleanEmail);
		entity.setPassword(encoder.encode(dto.getPassword()));

		if (dto.getUserRole() == Role.MECHANIC && dto.getManagerId() != null) {
			User manager = userRepo.findById(dto.getManagerId())
					.orElseThrow(() -> new ResourceNotFoundException("Manager not found."));
			entity.setManager(manager);
		}
		User savedUser = userRepo.save(entity);

		return mapUserToResponseDto(savedUser);

	}

	@Override
	public List<UserResponseDto> getUsers() {
		List<User> users = userRepo.findByIsActiveTrue();

		return users.stream().map(this::mapUserToResponseDto).collect(Collectors.toList());
	}

	@Override
	public UserResponseDto getUserById(Long userId) {
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("user with specified id not found."));

		return mapUserToResponseDto(user);
	}

	@Override
	public UserResponseDto updateUser(Long targetUserId, UpdateUserDto dto) {
		User user = userRepo.findById(targetUserId)
				.orElseThrow(() -> new ResourceNotFoundException("user with specified id not found."));

		if (dto.getUserName() != null)
			user.setUserName(dto.getUserName());

		if (dto.getMobile() != null)
			user.setMobile(dto.getMobile());

		if (dto.getUserRole() != null)
			user.setUserRole(dto.getUserRole());

		if (dto.getSalary() != null)
			user.setSalary(dto.getSalary());

		if (dto.getIsActive() != null)
			user.setActive(dto.getIsActive());

		if (dto.getManagerId() != null) {
			User newManager = userRepo.findById(dto.getManagerId())
					.orElseThrow(() -> new ResourceNotFoundException("manager with specified id not found."));
			user.setManager(newManager);
		}

		User savedUser = userRepo.save(user);
		return mapUserToResponseDto(savedUser);
	}

	@Override
	public void deleteUser(Long userId) {
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("user with specified id not found."));

		user.setActive(false);
		userRepo.save(user);

	}

	@Override
	public List<UserResponseDto> findActiveUsers() {
		List<User> activeUsers = userRepo.findByIsActiveTrue();

		return activeUsers.stream().map(this::mapUserToResponseDto).collect(Collectors.toList());
	}

	@Override
	public List<UserResponseDto> getAllCustomers() {
		List<User> customers = userRepo.findByUserRoleAndIsActiveTrue(Role.CUSTOMER);
		return customers.stream().map(this::mapUserToResponseDto).collect(Collectors.toList());
	}

	@Override
	public List<UserResponseDto> getAllManagers() {
		List<User> managers = userRepo.findByUserRoleAndIsActiveTrue(Role.MANAGER);

		return managers.stream().map(this::mapUserToResponseDto).collect(Collectors.toList());
	}

	@Override
	public List<UserResponseDto> getAllMechanics() {
		List<User> mechanics = userRepo.findByUserRoleAndIsActiveTrue(Role.MECHANIC);
		return mechanics.stream().map(this::mapUserToResponseDto).collect(Collectors.toList());
	}

	@Override
	public UserResponseDto getCustomerById(Long customerId) {
		User user = userRepo.findById(customerId)
				.orElseThrow(() -> new ResourceNotFoundException("User with specified not found."));
		if (user.getUserRole() != Role.CUSTOMER && user.isActive()) {
			throw new ResourceNotFoundException("ID " + customerId + " does not belongs to Customer");
		}
		return mapUserToResponseDto(user);
	}

	@Override
	public UserResponseDto getManager(Long managerId) {
		User user = userRepo.findById(managerId)
				.orElseThrow(() -> new ResourceNotFoundException("User with specified id not found"));

		if (user.getUserRole() != Role.MANAGER && user.isActive()) {
			throw new ResourceNotFoundException("ID " + managerId + " does not belongs to manager.");
		}
		return mapUserToResponseDto(user);
	}

	@Override
	public UserResponseDto getMechanic(Long mechanicId) {
		User user = userRepo.findById(mechanicId)
				.orElseThrow(() -> new ResourceNotFoundException("User with specified id not found."));

		if (user.getUserRole() != Role.MECHANIC && user.isActive()) {
			throw new ResourceNotFoundException("ID " + " does not belong to mechanic.");
		}
		return mapUserToResponseDto(user);
	}

	@Override
	public List<UserResponseDto> getMechanicsUnderManager(Long managerId) {
		List<User> mechanics = userRepo.findByManagerIdAndIsActiveTrue(managerId);

		return mechanics.stream().map(this::mapUserToResponseDto).collect(Collectors.toList());
	}

	@Override
	public UserResponseDto assignManagerToMechanic(Long mechanicId, Long managerId) {
		User mechanic = userRepo.findById(mechanicId)
				.orElseThrow(() -> new ResourceNotFoundException("user with specified id does not exist"));

		if (mechanic.getUserRole() != Role.MECHANIC) {
			throw new ResourceNotFoundException("ID " + mechanicId + " does not belong to mechanic.");
		}

		if (!mechanic.isActive()) {
			throw new RuntimeException("Cannot assign manager to an inactive/deleted mechanic.");
		}

		User manager = userRepo.findById(managerId)
				.orElseThrow(() -> new ResourceNotFoundException("manager does not exists with this id"));
		if (manager.getUserRole() != Role.MANAGER) {
			throw new ResourceNotFoundException("ID " + managerId + " does not belong to manager.");
		}

		if (!manager.isActive()) {
			throw new RuntimeException("Cannot assign an inactive/deleted manager.");
		}

		mechanic.setManager(manager);
		userRepo.save(mechanic);
		return mapUserToResponseDto(mechanic);
	}

	private UserResponseDto mapUserToResponseDto(User user) {
		UserResponseDto response = mapper.map(user, UserResponseDto.class);
		response.setUserId(user.getId());
		response.setIsActive(user.isActive());
		if (user.getManager() != null) {
			response.setManagerId(user.getManager().getId());
			response.setManagerName(user.getManager().getUserName());
		}
		return response;
	}

}
