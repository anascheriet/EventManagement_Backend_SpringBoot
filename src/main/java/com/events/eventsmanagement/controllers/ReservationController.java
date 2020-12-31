package com.events.eventsmanagement.controllers;

import com.events.eventsmanagement.dto.reservationDto;
import com.events.eventsmanagement.models.Reservation;
import com.events.eventsmanagement.repositories.EventRepository;
import com.events.eventsmanagement.repositories.EventTypeRepository;
import com.events.eventsmanagement.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.var;

import com.events.eventsmanagement.repositories.ReservationRepository;

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
}
