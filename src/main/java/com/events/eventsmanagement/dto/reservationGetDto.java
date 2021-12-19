package com.events.eventsmanagement.dto;

import java.util.Date;

import com.events.eventsmanagement.models.Reservation;

import lombok.Data;

@Data
public class reservationGetDto {
    Reservation reservation;
    private int clientId;
    private int eventId;
    private String eventName;
    private String imagePath;
    private float toPay;
    private Date eventDate;
}
