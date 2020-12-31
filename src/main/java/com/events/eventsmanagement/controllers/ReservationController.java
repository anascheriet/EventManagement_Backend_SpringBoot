package com.events.eventsmanagement.controllers;

import com.events.eventsmanagement.dto.reservationDto;
import com.events.eventsmanagement.models.Reservation;
import com.events.eventsmanagement.repositories.EventRepository;
import com.events.eventsmanagement.repositories.EventTypeRepository;
import com.events.eventsmanagement.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.events.eventsmanagement.dto.reservationGetDto;

import lombok.var;

import com.events.eventsmanagement.repositories.ReservationRepository;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("reservations")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @PostMapping("/create")
    public ResponseEntity<reservationDto> addReservation(@RequestBody reservationDto reservationDto) {
        var user = userRepository.findById(reservationDto.getUserid());
        var event = eventRepository.findById(reservationDto.getEventid());

        if (!user.isPresent()) {
            return ResponseEntity.unprocessableEntity().build();
        }

        Reservation res = new Reservation(reservationDto.getReservationDate(), reservationDto.getNumOfPeople(), user.get(), event.get());
        reservationRepository.save(res);
        return ResponseEntity.ok(reservationDto);
    }

    @GetMapping("/")
    public ResponseEntity<List<reservationGetDto>> getAllReservations() {
        var reservations = reservationRepository.findAll();
        List<reservationGetDto> displayedReservations = new ArrayList<>();
        reservations.forEach(res -> {
            var oneRes = reservationRepository.findById(res.getId());
            var resDto = new reservationGetDto();
            resDto.setReservation(oneRes.get());
            resDto.setClientId(oneRes.get().getUser().getId());
            resDto.setClientName(oneRes.get().getUser().getDisplayName());
            resDto.setEventId(oneRes.get().getEvent().getId());
            resDto.setEventName(oneRes.get().getEvent().getEventName());

            displayedReservations.add(resDto);
        });

        return ResponseEntity.ok(displayedReservations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<reservationGetDto> getReservationById(@PathVariable int id) {
        var oneRes = reservationRepository.findById(id);

        if (!oneRes.isPresent()) {
            return ResponseEntity.unprocessableEntity().build();
        }
        var resDto = new reservationGetDto();

        resDto.setReservation(oneRes.get());
        resDto.setClientId(oneRes.get().getUser().getId());
        resDto.setClientName(oneRes.get().getUser().getDisplayName());
        resDto.setEventId(oneRes.get().getEvent().getId());
        resDto.setEventName(oneRes.get().getEvent().getEventName());

        return ResponseEntity.ok(resDto);
    }


}
