package com.events.eventsmanagement.dto;

import lombok.Data;

@Data
public class updatePasswordDto {
    private String password;
    private String confirmPassword;
}
