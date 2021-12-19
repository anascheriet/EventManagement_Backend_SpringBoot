package com.events.eventsmanagement.dto;

import java.util.Date;

import lombok.Data;

@Data
public class eventDto {
    private String eventName;
    private String description;
    private Date eventDate;
    private int eventtypeid;
    private String country;
    private String city;
    private int availabletickets;
    private Float ticketprice;
    private String image;
}
