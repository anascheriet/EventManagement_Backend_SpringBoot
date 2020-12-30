package com.events.eventsmanagement.controllers;

import com.events.eventsmanagement.models.EventType;
import com.events.eventsmanagement.repositories.EventTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.var;
@RestController
@RequestMapping("eventTypes")
public class EventTypeController {

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @PostMapping("/create")
    public ResponseEntity<EventType> addEventType(@RequestBody EventType eventType) {
        var eType = eventTypeRepository.save(eventType);
        return ResponseEntity.ok(eType);
    }

    @GetMapping("/")
    public Iterable<EventType> getEventTypes ()
    {
        return eventTypeRepository.findAll();
    }

}
