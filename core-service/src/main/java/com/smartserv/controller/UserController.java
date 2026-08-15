package com.smartserv.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartserv.dto.CreateUserDto;
import com.smartserv.dto.UpdateUserDto;
import com.smartserv.service.UserService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor

public class UserController {
	private final UserService userService;

	@PostMapping
	public ResponseEntity<?> createUser(@RequestBody @Valid CreateUserDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
	}

	@GetMapping({"", "/getUsers"})
	public ResponseEntity<?> getUsers() {
		return ResponseEntity.ok(userService.getUsers());
	}

	@GetMapping({"/{userId}", "/getUserById/{userId}"})
	public ResponseEntity<?> findById(@PathVariable Long userId) {
		return ResponseEntity.ok(userService.getUserById(userId));
	}

	@PutMapping("/{userId}")
	public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UpdateUserDto dto) {
		return ResponseEntity.ok(userService.updateUser(userId, dto));
	}

	@DeleteMapping({"/{userId}", "/deleteUser/{userId}"})
	public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
		userService.deleteUser(userId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/active")
	public ResponseEntity<?> getActiveUsers() {
		return ResponseEntity.ok(userService.findActiveUsers());
	}

	@GetMapping("/customers")
	public ResponseEntity<?> getCustomers() {
		return ResponseEntity.ok(userService.getAllCustomers());
	}

	@GetMapping("/customer/{customerId}")
	public ResponseEntity<?> getCustomer(@PathVariable Long customerId) {
		return ResponseEntity.ok(userService.getCustomerById(customerId));
	}

	@GetMapping("/managers")
	public ResponseEntity<?> getManagers() {
		return ResponseEntity.ok(userService.getAllManagers());
	}

	@GetMapping("/manager/{managerId}")
	public ResponseEntity<?> getManager(@PathVariable Long managerId) {
		return ResponseEntity.ok(userService.getManager(managerId));
	}

	@GetMapping("/managers/{managerId}/mechanics")
	public ResponseEntity<?> getMechanicsUnderManager(@PathVariable Long managerId) {
		return ResponseEntity.ok(userService.getMechanicsUnderManager(managerId));
	}

	@GetMapping("/mechanics")
	public ResponseEntity<?> getMechanics() {
		return ResponseEntity.ok(userService.getAllMechanics());
	}

	@GetMapping("/mechanic/{mechanicId}")
	public ResponseEntity<?> getMechanic(@PathVariable Long mechanicId) {
		return ResponseEntity.ok(userService.getMechanic(mechanicId));
	}

	@PutMapping("/mechanics/{mechanicId}/assign_manager/{managerId}")
	public ResponseEntity<?> assignManager(@PathVariable Long mechanicId, @PathVariable Long managerId) {
		return ResponseEntity.ok(userService.assignManagerToMechanic(mechanicId, managerId));
	}

}
