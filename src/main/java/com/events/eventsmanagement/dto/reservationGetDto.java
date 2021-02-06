package com.events.eventsmanagement.dto;

import com.events.eventsmanagement.models.Reservation;
import lombok.Data;

import java.util.Date;

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
