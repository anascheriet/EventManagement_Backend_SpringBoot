package com.events.eventsmanagement.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@Data
public class eventDto {
    private String eventName;
    private Date eventDate;
    private int userid;
    private int eventtypeid;
}
