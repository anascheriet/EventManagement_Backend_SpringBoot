package com.events.eventsmanagement.dto;

import lombok.Data;

@Data
public class resetPasswordDto {
    private JwtResponse confirmationtoken;
    private String email;
    private String password;
    private String confirmpassword;
}
