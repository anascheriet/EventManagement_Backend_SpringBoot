package com.events.eventsmanagement.controllers;

import com.events.eventsmanagement.models.EventType;
import com.events.eventsmanagement.repositories.EventRepository;
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

    @Autowired
    private EventRepository eventRepository;

    @PostMapping("/create")
    public ResponseEntity<?> addEventType(@RequestBody EventType eventType) {
        var eType = eventTypeRepository.save(eventType);
        return ResponseEntity.ok("Event type added !");
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
    public ResponseEntity<?> updateEvType(@PathVariable int id, @RequestBody EventType req) {
        var foundType = eventTypeRepository.findById(id);
        req.setId(foundType.get().getId());
        eventTypeRepository.save(req);
        return ResponseEntity.ok("Event type updated !");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEvType(@PathVariable int id) {
        var events = eventRepository.findAll().stream().filter(i -> i.getEventType().getId() == id).count();
        if (events > 0) {
            return ResponseEntity.badRequest().body("Can't delete this event type, it exists in an event");
        } else {
            eventTypeRepository.deleteById(id);
        }
        return ResponseEntity.ok("Event type with id " + id + "has been deleted !");
    }

}
