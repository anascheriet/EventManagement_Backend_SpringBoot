package com.events.eventsmanagement.controllers;

import com.events.eventsmanagement.models.Event;
import com.events.eventsmanagement.repositories.EventRepository;
import com.events.eventsmanagement.repositories.EventTypeRepository;
import com.events.eventsmanagement.repositories.UserRepository;
import com.events.eventsmanagement.dto.eventDto;
import com.events.eventsmanagement.dto.eventGetDto;
import com.events.eventsmanagement.security.UserService;
import com.events.eventsmanagement.util.fileUploadService;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/events")
public class EventController extends BaseController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @Autowired
    private fileUploadService fileUploadService;


    @PostMapping("/create")
    public ResponseEntity<eventDto> addEvent(@RequestBody eventDto eventDto) throws IOException {
        var user = userRepository.findById(eventDto.getUserid());
        var eventType = eventTypeRepository.findById(eventDto.getEventtypeid());

        if (!user.isPresent() || !eventType.isPresent()) {
            return ResponseEntity.unprocessableEntity().build();
        }
        //saveUploadedFile(eventDto.getImage());

        Event createdEvent = new Event(eventDto.getEventName(), eventDto.getEventDate(), user.get(), eventType.get());
        eventRepository.save(createdEvent);
        return ResponseEntity.ok(eventDto);
    }

    @GetMapping("/")
    public ResponseEntity<Iterable<eventGetDto>> getAllUsers() {
        var events = eventRepository.findAll();
        List<eventGetDto> returnedEvents = new ArrayList<>();

        events.forEach(ev -> {
            var event = eventRepository.findById(ev.getId());
            var returnedEvent = new eventGetDto(ev, ev.getAppUser().getId(), ev.getAppUser().getDisplayName());
            returnedEvents.add(returnedEvent);
        });
        return ResponseEntity.ok(returnedEvents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<eventGetDto> getEventById(@PathVariable int id) {
        var event = eventRepository.findById(id);
        var returnedEvent = new eventGetDto(event.get(), event.get().getAppUser().getId(), event.get().getAppUser().getDisplayName());
        return ResponseEntity.ok(returnedEvent);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<eventDto> patchEvent(@PathVariable int id, @RequestBody eventDto req) {
        var foundEvent = eventRepository.findById(id);
        foundEvent.map(ev -> {
            ev.setEventDate(req.getEventDate());
            ev.setEventName(req.getEventName());
            return ResponseEntity.ok(eventRepository.save(ev));
        }).orElse(null);

        return ResponseEntity.ok(req);
    }

    @PostMapping("/image")
    public String uploadImage(MultipartFile file) {
        return fileUploadService.singleFileUpload(file);
    }

}
