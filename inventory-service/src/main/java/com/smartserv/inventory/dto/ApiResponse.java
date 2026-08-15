package com.smartserv.inventory.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse {
    private LocalDateTime timestamp;
    private String message;
    private String status;
    
    public ApiResponse(String message, String status) {
        super();
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}
