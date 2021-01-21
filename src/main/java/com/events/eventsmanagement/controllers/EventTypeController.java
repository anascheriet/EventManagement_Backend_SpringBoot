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
    public ResponseEntity<Iterable<EventType>> getEventTypes() {
        var eventTypes = eventTypeRepository.findAll();
        return ResponseEntity.ok(eventTypes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventType> getEventTypeByID(@PathVariable int id) {
        var eventTypes = eventTypeRepository.findById(id);
        return ResponseEntity.ok(eventTypes.get());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventType> updateEvType(@PathVariable int id, @RequestBody EventType req) {
        var foundType = eventTypeRepository.findById(id);
        req.setId(foundType.get().getId());
        return ResponseEntity.ok(eventTypeRepository.save(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEvType(@PathVariable int id) {
        eventTypeRepository.deleteById(id);
        return ResponseEntity.ok("Event type with id " + id + "has been deleted !");
    }

}
