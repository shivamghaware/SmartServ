package com.smartserv.dto.jobCard;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignMechanicDto {
	@NotNull(message = "mechanic id cannot be null.")
	private Long mechanicId;
}
