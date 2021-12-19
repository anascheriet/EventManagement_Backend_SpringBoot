package com.events.eventsmanagement.dto;

import com.events.eventsmanagement.models.AppUser;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetUserDto {
    private AppUser user;
    private JwtResponse token;
}
