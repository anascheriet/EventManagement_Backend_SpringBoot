package com.events.eventsmanagement.controllers;

import java.time.ZoneId;

import org.joda.time.Days;
import org.joda.time.LocalDate;
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

import com.events.eventsmanagement.dto.reservationDto;
import com.events.eventsmanagement.dto.reservationGetDto;
import com.events.eventsmanagement.models.Reservation;
import com.events.eventsmanagement.repositories.EventRepository;
import com.events.eventsmanagement.repositories.ReservationRepository;
import com.events.eventsmanagement.repositories.UserRepository;
import com.events.eventsmanagement.services.StatsService;

import lombok.var;

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

    @Autowired
    private StatsService statsService;

    @PostMapping("/create")
    public ResponseEntity<?> addReservation(@RequestBody reservationDto reservationDto) {
        var user = userRepository.findById(getCurrentUser().getId());
        var event = eventRepository.findById(reservationDto.getEventid());

        if (!user.isPresent()) {
            return ResponseEntity.badRequest().body("There's no user with the provided id, please make sure you are authenticated");
        }

        if (event.get().getAvailableTickets() - reservationDto.getNumOfPeople() < 0) {
            return ResponseEntity.badRequest().body("Can't Book more than " + event.get().getAvailableTickets() + " tickets");
        } else {
            event.get().setAvailableTickets(event.get().getAvailableTickets() - reservationDto.getNumOfPeople());
        }

        Reservation res = new Reservation(reservationDto.getNumOfPeople(), user.get(), event.get());
        reservationRepository.save(res);
        return ResponseEntity.ok("Booking Made ! Your total to pay is $" + event.get().getTicketPrice() * reservationDto.getNumOfPeople() + "! Please check your Bookings list to see all of your bookings.");
    }


    @GetMapping("/")
    public ResponseEntity<?> getAllReservations() {
        return ResponseEntity.ok(statsService.allBookings());
    }

    @GetMapping("/MyBookings")
    public ResponseEntity<?> getMyReservations() {
        var userId = userRepository.findById(getCurrentUser().getId()).get().getId();
        var myBookings = statsService.allBookings().stream().filter(x -> x.getClientId() == userId);
        return ResponseEntity.ok(myBookings);
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
        resDto.setEventId(oneRes.get().getEvent().getId());
        resDto.setEventDate(oneRes.get().getEvent().getEventDate());
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

    @DeleteMapping("/CancelBooking/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable int id) {
        var booking = reservationRepository.findById(id);
        LocalDate nowDate = LocalDate.now();
        java.time.LocalDate tempDate = booking.get().getEvent().getEventDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate eventDate = new LocalDate(tempDate.getYear(), tempDate.getMonthValue(), tempDate.getDayOfMonth());
        long diff = Math.abs(Days.daysBetween(nowDate, eventDate).getDays());

        //if theres less than 3 days to th event don't cancel
        if (diff < 3) {
            return ResponseEntity.badRequest().body("You can Only Cancel Bookings where there's more than 3 days left to the event.");
        }

        //Update num of available tickets
        var event = eventRepository.findById(booking.get().getEvent().getId());
        event.get().setAvailableTickets(event.get().getAvailableTickets() + booking.get().getNumOfPeople());
        reservationRepository.deleteById(id);

        return ResponseEntity.ok("Booking Canceled.");
    }


    //Stats For dashboard

    @GetMapping("/income")
    public ResponseEntity<?> getIncomeStats() {
        return statsService.getAllIncome();
    }

    @GetMapping("/byClientCountry")
    public ResponseEntity<?> reservationsByCountryStats() {
        return statsService.classReservationsByCountry();
    }

    @GetMapping("/byClientAge")
    public ResponseEntity<?> reservationsByClientAgeStats() {
        return statsService.classReservationsByClientAge();
    }


    @GetMapping("/byBookingMonth")
    public ResponseEntity<?> reservationsByMonthStats() {
        return statsService.classReservationsByMonth();
    }

    @GetMapping("/byEventType")
    public ResponseEntity<?> classReservationsByEventType() {
        return statsService.classReservationsByEventType();
    }
}
