package com.events.eventsmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class adminDataDto {
    private int id;
    private String email;
    private String displayName;
    private int age;
    private String country;
    private Boolean isAccNonLocked;
    private Double totalRevenue;
}
