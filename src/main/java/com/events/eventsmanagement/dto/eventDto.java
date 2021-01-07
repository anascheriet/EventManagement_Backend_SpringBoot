package com.events.eventsmanagement.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Data
public class eventDto {
    private String eventName;
    private Date eventDate;
    private int eventtypeid;
    private MultipartFile image;
}
