package com.events.eventsmanagement.dto;

import com.events.eventsmanagement.models.Event;

import lombok.Data;

@Data
public class eventGetDto {
    private Event event;
    private int creatorId;
    private String creatorName;

    public eventGetDto(Event event, int id, String displayName) {
        this.event = event;
        this.creatorId = id;
        this.creatorName = displayName;
    }
}
