package com.events.eventsmanagement.Services;

import com.events.eventsmanagement.controllers.BaseController;
import com.events.eventsmanagement.dto.reservationGetDto;
import com.events.eventsmanagement.models.AppUser;
import com.events.eventsmanagement.models.Event;
import com.events.eventsmanagement.repositories.EventRepository;
import com.events.eventsmanagement.repositories.EventTypeRepository;
import com.events.eventsmanagement.repositories.UserRepository;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import com.events.eventsmanagement.repositories.ReservationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class StatsService extends BaseController {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventTypeRepository eventTypeRepository;

    public List<reservationGetDto> allBookings() {
        var reservations = reservationRepository.findAll();
        List<reservationGetDto> displayedReservations = new ArrayList<>();
        reservations.forEach(res -> {
            var oneRes = reservationRepository.findById(res.getId());
            var resDto = new reservationGetDto();
            resDto.setReservation(oneRes.get());
            resDto.setClientId(oneRes.get().getAppUser().getId());
            resDto.setEventId(oneRes.get().getEvent().getId());
            resDto.setEventName(oneRes.get().getEvent().getEventName());
            resDto.setImagePath(oneRes.get().getEvent().getImagePath());
            resDto.setEventDate(oneRes.get().getEvent().getEventDate());
            var price = oneRes.get().getEvent().getTicketPrice() * oneRes.get().getNumOfPeople();
            resDto.setToPay(price);

            displayedReservations.add(resDto);
        });
        return displayedReservations;
    }

    private Optional<AppUser> loggedUser() {
        return userRepository.findById(getCurrentUser().getId());
    }

    private ZonedDateTime formatDate(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault());
    }

    public Double extractCurrentIncome(ZonedDateTime afterDate) {
        return loggedUser().get().getRole().getName().equals("Admin") ? loggedUser().get().getCreatedEvents().stream()
                .map(x -> x.getClientReservations().stream()
                        .filter(y -> formatDate(y.getBookedAt()).isAfter(afterDate))
                        .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople()).sum()).mapToDouble(s -> s).sum()
                :
                loggedUser().get().getRole().getName().equals("SuperAdmin") ?
                        eventRepository.findAll().stream().map(x -> x.getClientReservations().stream()
                                .filter(y -> formatDate(y.getBookedAt()).isAfter(afterDate))
                                .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople()).sum()).mapToDouble(s -> s).sum()
                        : 0;
    }


    public Double extractLastIncome(ZonedDateTime afterDate, ZonedDateTime beforeDate) {
        return loggedUser().get().getRole().getName().equals("Admin") ? loggedUser().get().getCreatedEvents().stream()
                .map(x -> x.getClientReservations().stream()
                        .filter(y -> formatDate(y.getBookedAt()).isAfter(afterDate) && formatDate(y.getBookedAt()).isBefore(beforeDate))
                        .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople()).sum()).mapToDouble(s -> s).sum()
                :
                loggedUser().get().getRole().getName().equals("SuperAdmin") ?
                        eventRepository.findAll().stream().map(x -> x.getClientReservations().stream()
                                .filter(y -> formatDate(y.getBookedAt()).isAfter(afterDate) && formatDate(y.getBookedAt()).isBefore(beforeDate))
                                .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople()).sum()).mapToDouble(s -> s).sum() : 0;

    }

    public Stream<Event> getBookings() {
        return
                loggedUser().get().getRole().getName().equals("Admin") ?
                        loggedUser()
                                .get().getCreatedEvents().stream().filter(y -> !y.getClientReservations().isEmpty())
                        :
                        loggedUser().get().getRole().getName().equals("SuperAdmin") ?
                                eventRepository.findAll().stream().filter(y -> !y.getClientReservations().isEmpty()) : null;

    }

    public ResponseEntity<?> getAllIncome() {
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


        var thisWeekAvg = (currentWeekIncome - lastWeekIncome) / lastWeekIncome * 100;

        System.out.println("thisWeekAvg " + thisWeekAvg);

        /*last Month range*/
        final ZonedDateTime thisMonthStart = todayDate.minusMonths(1);

        var currentMonthIncome = extractCurrentIncome(thisMonthStart);
        var lastMonthIncome = extractLastIncome(todayDate.minusMonths(2), thisMonthStart);
        var thisMonthAvg = lastMonthIncome == 0 ? 100 : (currentMonthIncome - lastMonthIncome) / lastMonthIncome * 100;

        /*Total Income*/
        /*var totalIncome = reservationRepository.findAll().stream().mapToDouble(x -> x.getEvent().getTicketPrice() * x.getNumOfPeople()).sum();*/
        var totalIncome = loggedUser().get().getRole().getName().equals("Admin") ?
                loggedUser().get().getCreatedEvents().stream()
                        .map(x -> x.getClientReservations().stream()
                                .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople()).sum()).mapToDouble(s -> s).sum()
                :
                loggedUser().get().getRole().getName().equals("SuperAdmin") ?
                        eventRepository.findAll().stream().map(x -> x.getClientReservations().stream()
                                .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople()).sum()).mapToDouble(s -> s).sum() : 0;

        var totalIncomeLastMonth = loggedUser().get().getRole().getName().equals("Admin") ?
                loggedUser().get().getCreatedEvents().stream()
                        .map(x -> x.getClientReservations().stream().filter(y -> formatDate(y.getBookedAt()).isBefore(thisMonthStart))
                                .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople()).sum()).mapToDouble(s -> s).sum()
                :
                loggedUser().get().getRole().getName().equals("SuperAdmin") ? eventRepository.findAll().stream()
                        .map(x -> x.getClientReservations().stream().filter(y -> formatDate(y.getBookedAt()).isBefore(thisMonthStart))
                                .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople()).sum()).mapToDouble(s -> s).sum()
                        : 0;

        var totalAvg = totalIncomeLastMonth == 0 ? 100 : ((totalIncome - totalIncomeLastMonth) / totalIncomeLastMonth * 100);


        incomeData.put("weekIncome", currentWeekIncome);
        incomeData.put("monthIncome", currentMonthIncome);
        incomeData.put("LastMonthIncome", lastMonthIncome);
        incomeData.put("totalIncome", totalIncome);
        incomeData.put("weekAvg", thisWeekAvg);
        incomeData.put("monthAvg", thisMonthAvg);
        incomeData.put("totalAvg", totalAvg);


        return ResponseEntity.ok(incomeData);

    }


    public ResponseEntity<?> classReservationsByCountry() {

        Map<String, Integer> countries = new HashMap<String, Integer>();

        getBookings().forEach(x -> {
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


    public ResponseEntity<?> classReservationsByClientAge() {

        Map<String, Integer> ageGroup = new HashMap<String, Integer>();

        ageGroup.put("18 to 29", 0);
        ageGroup.put("30 to 39", 0);
        ageGroup.put("40 to 65", 0);
        ageGroup.put("Above 66", 0);


        getBookings().forEach(a -> {
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

        getBookings().forEach(x -> {
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


    public ResponseEntity<?> classReservationsByEventType() {
        HashMap<String, Integer> eventTypeGroup = new HashMap<>();

        var types = eventTypeRepository.findAll();

        types.forEach(a -> {
            eventTypeGroup.put(a.getName(), 0);
        });

        getBookings().forEach(x -> {
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
