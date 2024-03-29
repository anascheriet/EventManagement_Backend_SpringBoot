package com.events.eventsmanagement.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.events.eventsmanagement.dto.eventDto;
import com.events.eventsmanagement.dto.eventGetDto;
import com.events.eventsmanagement.models.Event;
import com.events.eventsmanagement.repositories.EventRepository;
import com.events.eventsmanagement.repositories.EventTypeRepository;
import com.events.eventsmanagement.repositories.UserRepository;
import com.events.eventsmanagement.util.fileUploadService;

import lombok.var;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
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
    public ResponseEntity<?> addEvent(@RequestBody eventDto eventDto) throws IOException {
        var user = userRepository.findById(getCurrentUser().getId());
        var eventType = eventTypeRepository.findById(eventDto.getEventtypeid());

        if (!user.isPresent() || !eventType.isPresent()) {
            return ResponseEntity.unprocessableEntity().build();
        }
        //saveUploadedFile(eventDto.getImage());

        Event createdEvent = new Event(eventDto.getEventName(), eventType.get(), eventDto.getCountry(), eventDto.getCity(), eventDto.getDescription(),
                eventDto.getEventDate(), eventDto.getTicketprice(), eventDto.getAvailabletickets(), user.get(), eventDto.getImage());
        eventRepository.save(createdEvent);
        return ResponseEntity.ok("Event added successfully !");
    }

    @GetMapping("/")
    public ResponseEntity<List<eventGetDto>> getAllEvents() {
        var events = eventRepository.findAll();
        List<eventGetDto> returnedEvents = new ArrayList<>();

        events.forEach(ev -> {
            //var event = eventRepository.findById(ev.getId());
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
    public ResponseEntity<?> patchEvent(@PathVariable int id, @RequestBody eventDto req) {
        var foundEvent = eventRepository.findById(id);
        foundEvent.map(ev -> {
            if (req.getEventDate() != null) {
                ev.setEventDate(req.getEventDate());
            }
            ev.setEventName(req.getEventName());
            ev.setAvailableTickets(req.getAvailabletickets());
            ev.setCountry(req.getCountry());
            ev.setCity(req.getCity());
            ev.setDescription(req.getDescription());
            ev.setImagePath(req.getImage());
            ev.setTicketPrice(req.getTicketprice());
            return ResponseEntity.ok(eventRepository.save(ev));
        }).orElse(null);

        return ResponseEntity.ok("Event Updated !");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable int id) {
        var event = eventRepository.findById(id);
        if (event.get().getClientReservations().size() > 0) {
            return ResponseEntity.badRequest().body("You can't delete this event, it has been booked by clients");
        } else
            eventRepository.deleteById(id);
        return ResponseEntity.ok("Event Deleted !");
    }

    @PostMapping("/image")
    public String uploadImage(MultipartFile file) {
        System.out.println((file));
        return fileUploadService.singleFileUpload(file);
    }


}
