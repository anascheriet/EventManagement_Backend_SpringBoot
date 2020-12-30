package com.events.eventsmanagement.controllers;

import com.events.eventsmanagement.models.Event;
import com.events.eventsmanagement.repositories.EventRepository;
import com.events.eventsmanagement.repositories.EventTypeRepository;
import com.events.eventsmanagement.repositories.UserRepository;
import com.events.eventsmanagement.dto.eventDto;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/event")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @PostMapping("/create")
    public String addEvent(@RequestBody eventDto eventDto) {
        var user = userRepository.findById(eventDto.getUserid());
        var eventType = eventTypeRepository.findById(eventDto.getEventtypeid());
        System.out.println(eventType);
        if (user.isPresent() && eventType.isPresent()) {
            Event event = new Event(eventDto.getEventName(),eventDto.getEventDate(),user.get(), eventType.get());
            System.out.println(event);
            eventRepository.save(event);
        }
        return "dfe";
    }

}
