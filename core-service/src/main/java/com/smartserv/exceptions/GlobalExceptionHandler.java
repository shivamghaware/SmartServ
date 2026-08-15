package com.smartserv.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.smartserv.dto.ApiResponse;

@RestControllerAdvice // This makes it listen for exceptions across all Controllers
public class GlobalExceptionHandler {

	// 1. Handle our custom exception (e.g., if we throw this when email exists)
	@ExceptionHandler(ResourceAlreadyExists.class)
	public ResponseEntity<ApiResponse> handleResourceAlreadyExists(ResourceAlreadyExists e) {
		// Returns 409 Conflict
		ApiResponse response = new ApiResponse(e.getMessage(), "Registration failed due to duplicate entry.");
		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	// 2. Handle 404 Not Found (If, for example, a User is not found by ID)
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse> handleResourceNotFound(ResourceNotFoundException e) {
		// Returns 404 Not Found
		ApiResponse response = new ApiResponse(e.getMessage(), "Resource not found.");
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	// 3. Handle unexpected server errors (The catch-all)
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponse> handleGenericRuntimeException(RuntimeException e) {
		// Returns 500 Internal Server Error
		ApiResponse response = new ApiResponse(e.getMessage(), "Internal Server Error.");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

	}

	// 4. Method Argument Not valid exception
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
		Map<String, String> errors = new HashMap<>();
		e.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
	}

	// 5. Handle invalid date exception
	@ExceptionHandler(InvalidDateException.class)
	public ResponseEntity<?> handleInvalidDateException(InvalidDateException e) {
		ApiResponse response = new ApiResponse(e.getMessage(), "Invalid date");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	// 6. Handle Duplicate job creation
	@ExceptionHandler(DuplicateJobCreationException.class)
	public ResponseEntity<?> duplicateJobCreationException(DuplicateJobCreationException e) {
		ApiResponse response = new ApiResponse(e.getMessage(), "Duplicate job");
		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	// 7. Handle unauthorized access
	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<?> unauthorizedException(UnauthorizedException e) {
		ApiResponse response = new ApiResponse(e.getMessage(), "unauthorized access");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	// 8. User does not exist
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<?> userNotFoundException(UserNotFoundException e) {
		ApiResponse response = new ApiResponse(e.getMessage(), "User does not exist");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	// 9. invalid user role
	@ExceptionHandler(InvalidRoleException.class)
	public ResponseEntity<?> invalidRoleException(InvalidRoleException e) {
		ApiResponse response = new ApiResponse(e.getMessage(), "User role is invalid");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	// 10. payment exception handler
	@ExceptionHandler(PaymentException.class)
	public ResponseEntity<?> handlePaymentException(PaymentException ex) {
		ApiResponse response = new ApiResponse(ex.getMessage(), "Payment processing failed");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

}