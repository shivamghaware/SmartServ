package com.smartserv.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RsaLocationDto {
	private Double latitude;
	private Double longitude;

	public static RsaLocationDto fromCoordinates(String coordinates) {
		if (coordinates == null || coordinates.trim().isEmpty()) {
			return null;
		}

		String[] parts = coordinates.split(",");
		if (parts.length != 2) {
			return null;
		}

		try {
			String latStr = parts[0].replaceAll("[^0-9.-]", "").trim();
			String lngStr = parts[1].replaceAll("[^0-9.-]", "").trim();
			if (latStr.isEmpty() || lngStr.isEmpty()) return null;
			return new RsaLocationDto(Double.parseDouble(latStr), Double.parseDouble(lngStr));
		} catch (Exception e) {
			return null;
		}
	}

	public String toCoordinates() {
		return latitude + "," + longitude;
	}
}
