package com.events.eventsmanagement.controllers;

import com.events.eventsmanagement.models.EventType;
import com.events.eventsmanagement.repositories.EventTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.var;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("eventTypes")
public class EventTypeController extends BaseController {

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @PostMapping("/create")
    public ResponseEntity<EventType> addEventType(@RequestBody EventType eventType) {
        var eType = eventTypeRepository.save(eventType);
        return ResponseEntity.ok(eType);
    }

    @GetMapping("/")
    public Iterable<EventType> getEventTypes() {
        return eventTypeRepository.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventType> updateEvType(@PathVariable int id, @RequestBody EventType req){
        var foundType = eventTypeRepository.findById(id);
        req.setId(foundType.get().getId());
        return ResponseEntity.ok( eventTypeRepository.save(req));
    }

}
