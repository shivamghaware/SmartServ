package com.smartserv.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.smartserv.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserDto {

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    @JsonAlias({"name", "userName"})
    private String userName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,20}$",
            message = "Password must contain at least one digit, one lowercase letter, one uppercase letter, and one special character.")
    private String password;

    @JsonAlias({"role", "userRole"})
    private Role userRole = Role.CUSTOMER;

    @Pattern(regexp = "^\\d{10}$", message = "Invalid phone number format")
    @JsonAlias({"phone", "mobile"})
    private String mobile;

    @Min(value = 1, message = "Salary must be greater than 1")
    private Double salary;

    private boolean isActive = true;

    private Long managerId;
}
