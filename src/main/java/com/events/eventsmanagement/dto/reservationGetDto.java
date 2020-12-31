package com.events.eventsmanagement.dto;

import com.events.eventsmanagement.models.Reservation;
import lombok.Data;

@Data
public class reservationGetDto {
    Reservation reservation;
    private int clientId;
    private String clientName;
    private int eventId;
    private String eventName;
}
