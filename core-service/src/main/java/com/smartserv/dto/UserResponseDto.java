package com.smartserv.dto;

import com.smartserv.entity.Role;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

public class UserResponseDto {
	private Long userId;
	private String userName;
	private String email;
	private Role userRole;
	private String mobile;
	private Boolean isActive;
	
	
	private String managerName;
	private Long managerId;
}
