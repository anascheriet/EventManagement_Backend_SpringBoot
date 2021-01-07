package com.events.eventsmanagement.dto;

import lombok.Data;

import java.util.Date;

@Data

public class reservationDto {
    private Date reservationDate;
    private int numOfPeople;
    private int eventid;
}
