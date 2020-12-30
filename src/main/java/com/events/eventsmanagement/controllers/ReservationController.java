package com.events.eventsmanagement.controllers;

import com.events.eventsmanagement.dto.reservationDto;
import com.events.eventsmanagement.models.Reservation;
import com.events.eventsmanagement.repositories.EventTypeRepository;
import com.events.eventsmanagement.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.var;

import com.events.eventsmanagement.repositories.ReservationRepository;

@RestController
@RequestMapping("reservation")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create")
    public String addReservation(@RequestBody reservationDto reservationDto) {
        var user = userRepository.findById(reservationDto.getUserid());

        if (user.isPresent()) {
            Reservation res = new Reservation(reservationDto.getReservationDate(), reservationDto.getNumOfPeople(), user.get());
            reservationRepository.save(res);
        }
        return "";
    }
}
