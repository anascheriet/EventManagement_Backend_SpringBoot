package com.events.eventsmanagement.controllers;

import com.events.eventsmanagement.dto.reservationDto;
import com.events.eventsmanagement.models.Reservation;
import com.events.eventsmanagement.repositories.EventRepository;
import com.events.eventsmanagement.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.events.eventsmanagement.dto.reservationGetDto;

import lombok.var;

import com.events.eventsmanagement.repositories.ReservationRepository;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/reservations")
public class ReservationController extends BaseController {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @PostMapping("/create")
    public ResponseEntity<?> addReservation(@RequestBody reservationDto reservationDto) {
        var user = userRepository.findById(getCurrentUser().getId());
        var event = eventRepository.findById(reservationDto.getEventid());
        if (!user.isPresent()) {
            return ResponseEntity.badRequest().body("There's no user with the provided id, please make sure you are authenticated");
        }

        if (event.get().getAvailableTickets() - reservationDto.getNumOfPeople() < 0) {
            return ResponseEntity.badRequest().body("Can't Book more than " + event.get().getAvailableTickets());
        } else {
            event.get().setAvailableTickets(event.get().getAvailableTickets() - reservationDto.getNumOfPeople());
        }

        Reservation res = new Reservation(reservationDto.getNumOfPeople(), user.get(), event.get());
        reservationRepository.save(res);
        return ResponseEntity.ok("Booking Made ! Your total to pay is $" + event.get().getTicketPrice() * reservationDto.getNumOfPeople() + "! Please check your Bookings list to see all of your bookings.");
    }

    @GetMapping("/")
    public ResponseEntity<List<reservationGetDto>> getAllReservations() {
        var reservations = reservationRepository.findAll();
        List<reservationGetDto> displayedReservations = new ArrayList<>();
        reservations.forEach(res -> {
            var oneRes = reservationRepository.findById(res.getId());
            var resDto = new reservationGetDto();
            resDto.setReservation(oneRes.get());
            resDto.setClientId(oneRes.get().getAppUser().getId());
            resDto.setClientName(oneRes.get().getAppUser().getDisplayName());
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
        resDto.setClientId(oneRes.get().getAppUser().getId());
        resDto.setClientName(oneRes.get().getAppUser().getDisplayName());
        resDto.setEventId(oneRes.get().getEvent().getId());
        resDto.setEventName(oneRes.get().getEvent().getEventName());

        return ResponseEntity.ok(resDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<reservationDto> updateReservation(@RequestBody reservationDto req, @PathVariable int id) {
        var foundRes = reservationRepository.findById(id);
        if (!foundRes.isPresent()) {
            return ResponseEntity.unprocessableEntity().build();
        }

        foundRes.map(res -> {
            res.setNumOfPeople(req.getNumOfPeople());
            return ResponseEntity.ok(reservationRepository.save(res));
        }).orElseGet(null);

        return ResponseEntity.ok(req);
    }


    private ZonedDateTime formatDate(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault());
    }

    @GetMapping("/income")
    public ResponseEntity<?> getIncome() {

        var user = userRepository.findById(getCurrentUser().getId());

        final ZonedDateTime todayDate = ZonedDateTime.now();
        final ZonedDateTime startOfLastWeek = todayDate.minusWeeks(1).with(DayOfWeek.MONDAY);

        /*HashMap to return*/
        HashMap incomeData = new HashMap();

        /*Last week range*/
        final ZonedDateTime endOfLastWeek = startOfLastWeek.plusDays(6);
        /*var weekIncome = reservationRepository.findAll().stream()
                .filter(x -> formatDate(x.getBookedAt())
                        .isAfter(endOfLastWeek)).mapToDouble(x -> x.getEvent().getTicketPrice() * x.getNumOfPeople()).sum();*/

        var weekIncome = user.get().getCreatedEvents().stream()
                .map(x -> x.getClientReservations().stream()
                        .filter(y -> formatDate(y.getBookedAt()).isAfter(endOfLastWeek))
                        .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople()).sum()).mapToDouble(s -> s).sum();
        ;

        /*last Month range*/
        final ZonedDateTime lastMonth = todayDate.minusMonths(1);

       /* var monthIncome = reservationRepository.findAll().stream().filter(x -> formatDate(x.getBookedAt())
                .isAfter(lastMonth)).mapToDouble(x -> x.getEvent().getTicketPrice() * x.getNumOfPeople()).sum();*/


        var monthIncome =
                user.get().getCreatedEvents().stream()
                        .map(x -> x.getClientReservations().stream()
                                .filter(y -> formatDate(y.getBookedAt()).isAfter(lastMonth))
                                .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople()).sum()).mapToDouble(s -> s).sum();



        /*Total Income*/
        /*var totalIncome = reservationRepository.findAll().stream().mapToDouble(x -> x.getEvent().getTicketPrice() * x.getNumOfPeople()).sum();*/
        var totalIncome =
                user.get().getCreatedEvents().stream()
                        .map(x -> x.getClientReservations().stream()
                                .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople()).sum()).mapToDouble(s -> s).sum();

        incomeData.put("weekIncome", weekIncome);
        incomeData.put("monthIncome", monthIncome);
        incomeData.put("totalIncome", totalIncome);

        return ResponseEntity.ok(incomeData);
    }


}
