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

    private Double extractCurrentIncome(ZonedDateTime afterDate) {
        return userRepository.findById(getCurrentUser().getId()).get().getCreatedEvents().stream()
                .map(x -> x.getClientReservations().stream()
                        .filter(y -> formatDate(y.getBookedAt()).isAfter(afterDate))
                        .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople()).sum()).mapToDouble(s -> s).sum();
    }

    private Double extractLastIncome(ZonedDateTime afterDate, ZonedDateTime beforeDate) {
        return userRepository.findById(getCurrentUser().getId()).get().getCreatedEvents().stream()
                .map(x -> x.getClientReservations().stream()
                        .filter(y -> formatDate(y.getBookedAt()).isAfter(afterDate) && formatDate(y.getBookedAt()).isBefore(beforeDate))
                        .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople()).sum()).mapToDouble(s -> s).sum();
    }

    @GetMapping("/income")
    public ResponseEntity<?> getIncome() {

        final ZonedDateTime todayDate = ZonedDateTime.now();
        final ZonedDateTime startOfLastWeek = todayDate.minusWeeks(1).with(DayOfWeek.MONDAY);

        /*Last week range*/
        final ZonedDateTime endOfLastWeek = startOfLastWeek.plusDays(6);
        /*HashMap to return*/
        HashMap incomeData = new HashMap();

        var currentWeekIncome = extractCurrentIncome(endOfLastWeek);
        var lastWeekIncome = extractLastIncome(startOfLastWeek, endOfLastWeek);
        var thisWeekAvg = (currentWeekIncome - lastWeekIncome) * 100 + "%";

        /*last Month range*/
        final ZonedDateTime thisMonthStart = todayDate.minusMonths(1);
        final ZonedDateTime lastMonth = todayDate.minusMonths(2);

        var currentMonthIncome = extractCurrentIncome(thisMonthStart);
        var lastMonthIncome = extractLastIncome(lastMonth, thisMonthStart);
        var thisMonthAvg = (currentMonthIncome - lastMonthIncome) * 100 + "%";

        var user = userRepository.findById(getCurrentUser().getId());

        /*Total Income*/
        /*var totalIncome = reservationRepository.findAll().stream().mapToDouble(x -> x.getEvent().getTicketPrice() * x.getNumOfPeople()).sum();*/
        var totalIncome =
                user.get().getCreatedEvents().stream()
                        .map(x -> x.getClientReservations().stream()
                                .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople()).sum()).mapToDouble(s -> s).sum();

        incomeData.put("weekIncome", currentWeekIncome);
        incomeData.put("monthIncome", currentMonthIncome);
        incomeData.put("totalIncome", totalIncome);
        incomeData.put("weekAvg", thisWeekAvg);
        incomeData.put("monthAvg", thisMonthAvg);

        return ResponseEntity.ok(incomeData);
    }


}
