package com.events.eventsmanagement.controllers;

import com.events.eventsmanagement.models.EventType;
import com.events.eventsmanagement.repositories.EventTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("eventType")
public class EventTypeController {

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @PostMapping("/create")
    public EventType addEventType(@RequestBody EventType eventType)
    {
        return eventTypeRepository.save(eventType);
    }

}
