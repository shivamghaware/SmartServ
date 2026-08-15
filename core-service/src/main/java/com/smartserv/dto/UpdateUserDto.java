package com.smartserv.dto;

import com.smartserv.entity.Role;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDto {
	
	
	@Size(min=5, max=50, message="Name must be between 5 and 50")
	private String userName;
	
	@NotNull(message="Role cannot be blank.")
	private Role userRole;
	
	@Pattern(regexp = "^\\d{10}$", message = "Invalid phone number format")
	private String mobile;
	
	@Min(value=1, message="salary must be greater than 1")
	private Double salary;
	
	private Long managerId;
	
	private Boolean isActive;
}
