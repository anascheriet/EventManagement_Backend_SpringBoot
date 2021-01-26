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

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    private EventTypeRepository eventTypeRepository;

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
        System.out.println("currentWeekIncome " + currentWeekIncome);
        System.out.println("lastWeekIncome " + lastWeekIncome);
        var thisWeekAvg = (currentWeekIncome - lastWeekIncome) * 100;

        System.out.println("thisWeekAvg " + thisWeekAvg);

        /*last Month range*/
        final ZonedDateTime thisMonthStart = todayDate.minusMonths(1);
        final ZonedDateTime lastMonth = todayDate.minusMonths(2);

        var currentMonthIncome = extractCurrentIncome(thisMonthStart);
        var lastMonthIncome = extractLastIncome(lastMonth, thisMonthStart);
        var thisMonthAvg = (currentMonthIncome - lastMonthIncome) * 100;

        var user = userRepository.findById(getCurrentUser().getId());

        /*Total Income*/
        /*var totalIncome = reservationRepository.findAll().stream().mapToDouble(x -> x.getEvent().getTicketPrice() * x.getNumOfPeople()).sum();*/
        var totalIncome =
                user.get().getCreatedEvents().stream()
                        .map(x -> x.getClientReservations().stream()
                                .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople()).sum()).mapToDouble(s -> s).sum();

        var totalIncomeLastMonth =
                user.get().getCreatedEvents().stream()
                        .map(x -> x.getClientReservations().stream().filter(y -> formatDate(y.getBookedAt()).isBefore(thisMonthStart))
                                .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople()).sum()).mapToDouble(s -> s).sum();

        var totalAvg = (totalIncome - totalIncomeLastMonth) * 100;

        incomeData.put("weekIncome", currentWeekIncome);
        incomeData.put("monthIncome", currentMonthIncome);
        incomeData.put("totalIncome", totalIncome);
        incomeData.put("weekAvg", thisWeekAvg);
        incomeData.put("monthAvg", thisMonthAvg);
        incomeData.put("totalAvg", totalAvg);

        return ResponseEntity.ok(incomeData);
    }

    @GetMapping("/byClientCountry")
    public ResponseEntity<?> classReservationsByCountry() {

        Map<String, Integer> countries = new HashMap<String, Integer>();

        var reser = userRepository.findById(getCurrentUser().getId())
                .get().getCreatedEvents().stream().filter(y -> !y.getClientReservations().isEmpty());

        reser.forEach(x -> {
            x.getClientReservations().forEach(a -> {
                if (!countries.containsKey(a.getAppUser().getCountry())) {
                    countries.put(a.getAppUser().getCountry(), 1);
                } else {
                    int count = countries.get(a.getAppUser().getCountry());
                    countries.put(a.getAppUser().getCountry(), count + 1);
                }
            });
        });
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/byClientAge")
    public ResponseEntity<?> classReservationsByClientAge() {

        Map<String, Integer> ageGroup = new HashMap<String, Integer>();

        ageGroup.put("18 to 29", 0);
        ageGroup.put("30 to 39", 0);
        ageGroup.put("40 to 65", 0);
        ageGroup.put("Above 66", 0);

        var reserv = userRepository.findById(getCurrentUser().getId())
                .get().getCreatedEvents().stream().filter(y -> y.getClientReservations().size() != 0);


        reserv.forEach(a -> {
            a.getClientReservations().forEach(
                    x -> {
                        System.out.println(x.getAppUser().getDisplayName());
                        if (x.getAppUser().getAge() >= 18 && x.getAppUser().getAge() <= 29) {
                            int count = ageGroup.get("18 to 29");
                            ageGroup.put("18 to 29", count + 1);
                        } else if (x.getAppUser().getAge() >= 30 && x.getAppUser().getAge() <= 39) {
                            int count = ageGroup.get("30 to 39");
                            ageGroup.put("30 to 39", count + 1);
                        } else if (x.getAppUser().getAge() >= 40 && x.getAppUser().getAge() <= 65) {
                            int count = ageGroup.get("40 to 65");
                            ageGroup.put("40 to 65", count + 1);
                        } else if (x.getAppUser().getAge() > 66) {
                            int count = ageGroup.get("Above 66");
                            ageGroup.put("Above 66", count + 1);
                        }
                    });
        });

        return ResponseEntity.ok(ageGroup);

    }


    public String getMonthString(int month) {
        switch (month) {
            case 0:
                return "January";
            case 1:
                return "February";
            case 2:
                return "March";
            case 3:
                return "April";
            case 4:
                return "May";
            case 5:
                return "June";
            case 6:
                return "July";
            case 7:
                return "August";
            case 8:
                return "September";
            case 9:
                return "October";
            case 10:
                return "November";
            case 11:
                return "December";
            default:
                return " ";

        }
    }

    @GetMapping("/byBookingMonth")
    public ResponseEntity<?> classReservationsByMonth() {
        Map<String, Integer> monthGroup = new HashMap<String, Integer>();

        monthGroup.put("January", 0);
        monthGroup.put("February", 0);
        monthGroup.put("March", 0);
        monthGroup.put("April", 0);
        monthGroup.put("May", 0);
        monthGroup.put("June", 0);
        monthGroup.put("July", 0);
        monthGroup.put("August", 0);
        monthGroup.put("September", 0);
        monthGroup.put("October", 0);
        monthGroup.put("November", 0);
        monthGroup.put("December", 0);

        var reserv = userRepository.findById(getCurrentUser().getId())
                .get().getCreatedEvents().stream().filter(y -> y.getClientReservations().size() != 0);

        reserv.forEach(x -> {
            x.getClientReservations().forEach(
                    a -> {
                        //Loop over MonthGroup keys
                        for (String key : monthGroup.keySet()) {
                            if (getMonthString(a.getBookedAt().getMonth()).equals(key)) {
                                int count = monthGroup.get(key);
                                monthGroup.put(key, count + 1);
                            }
                        }
                    }
            );
        });
        return ResponseEntity.ok(monthGroup);
    }

    @GetMapping("/byEventType")
    public ResponseEntity<?> classReservationsByEventType() {
        HashMap<String, Integer> eventTypeGroup = new HashMap<>();

        var types = eventTypeRepository.findAll();

        types.forEach(a -> {
            eventTypeGroup.put(a.getName(), 0);
        });

        var reserv = userRepository.findById(getCurrentUser().getId())
                .get().getCreatedEvents().stream().filter(y -> y.getClientReservations().size() != 0);


        reserv.forEach(x -> {
            x.getClientReservations().forEach(
                    r -> {
                        for (String key : eventTypeGroup.keySet()) {
                            if (r.getEvent().getEventType().getName().equals(key)) {
                                int count = eventTypeGroup.get(key);
                                eventTypeGroup.put(key, count + 1);
                            }
                        }
                    }
            );
        });

        return ResponseEntity.ok(eventTypeGroup);
    }
}
