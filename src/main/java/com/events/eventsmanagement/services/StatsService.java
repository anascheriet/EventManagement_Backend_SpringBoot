package com.events.eventsmanagement.services;

import static com.events.eventsmanagement.common.CommonConstants.ABOVE_SIXTY_SIX;
import static com.events.eventsmanagement.common.CommonConstants.ADMIN;
import static com.events.eventsmanagement.common.CommonConstants.APRIL;
import static com.events.eventsmanagement.common.CommonConstants.AUGUST;
import static com.events.eventsmanagement.common.CommonConstants.DECEMBER;
import static com.events.eventsmanagement.common.CommonConstants.EIGHTEEN_TO_TWENTY_NINE;
import static com.events.eventsmanagement.common.CommonConstants.FEBRUARY;
import static com.events.eventsmanagement.common.CommonConstants.FORTY_TO_SIXTY_FIVE;
import static com.events.eventsmanagement.common.CommonConstants.JANUARY;
import static com.events.eventsmanagement.common.CommonConstants.JULY;
import static com.events.eventsmanagement.common.CommonConstants.JUNE;
import static com.events.eventsmanagement.common.CommonConstants.MARCH;
import static com.events.eventsmanagement.common.CommonConstants.MAY;
import static com.events.eventsmanagement.common.CommonConstants.NOVEMBER;
import static com.events.eventsmanagement.common.CommonConstants.OCTOBER;
import static com.events.eventsmanagement.common.CommonConstants.SEPTEMBER;
import static com.events.eventsmanagement.common.CommonConstants.THIRTY_TO_THIRTY_NINE;
import static com.events.eventsmanagement.common.CommonConstants.formatDate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.events.eventsmanagement.controllers.BaseController;
import com.events.eventsmanagement.dto.reservationGetDto;
import com.events.eventsmanagement.models.AppUser;
import com.events.eventsmanagement.models.Event;
import com.events.eventsmanagement.models.Reservation;
import com.events.eventsmanagement.repositories.EventRepository;
import com.events.eventsmanagement.repositories.EventTypeRepository;
import com.events.eventsmanagement.repositories.ReservationRepository;
import com.events.eventsmanagement.repositories.UserRepository;

import lombok.var;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StatsService extends BaseController {

    @Autowired
    private SuperAdminStatsService superAdminStatsService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @Autowired
    private UserService userService;

    public List<reservationGetDto> allBookings() {
        var reservations = reservationRepository.findAll();
        List<reservationGetDto> displayedReservations = new ArrayList<>();
        reservations.forEach(res -> {
            var oneRes = reservationRepository.findById(res.getId()).orElse(new Reservation());
            var resDto = new reservationGetDto();
            resDto.setReservation(oneRes);
            resDto.setClientId(oneRes.getAppUser().getId());
            resDto.setEventId(oneRes.getEvent().getId());
            resDto.setEventName(oneRes.getEvent().getEventName());
            resDto.setImagePath(oneRes.getEvent().getImagePath());
            resDto.setEventDate(oneRes.getEvent().getEventDate());
            var price = oneRes.getEvent().getTicketPrice() * oneRes.getNumOfPeople();
            resDto.setToPay(price);

            displayedReservations.add(resDto);
        });

        return displayedReservations;
    }

    private AppUser loggedUser() {
        return userRepository.findById(getCurrentUser().getId()).orElse(new AppUser());
    }

    public Double extractCurrentIncome(ZonedDateTime afterDate) {
        double currentIncome = 0.0;
        try {
            currentIncome = loggedUser().getRole().getName().equals(ADMIN)
                    ? loggedUser()
                            .getCreatedEvents()
                                .stream()
                                .map(
                                        x -> x
                                                .getClientReservations()
                                                    .stream()
                                                    .filter(y -> formatDate(y.getBookedAt()).isAfter(afterDate))
                                                    .mapToDouble(
                                                            a -> a.getEvent().getTicketPrice() * a.getNumOfPeople())
                                                    .sum())
                                .mapToDouble(s -> s)
                                .sum()
                    : superAdminStatsService.getSuperAdminCurrentIncome(afterDate, userService.loggedUser());
        }
        catch (NullPointerException e) {
            log.error("Couldn't find Logged in user", e);
        }
        return currentIncome;
    }

    public Double extractLastIncome(ZonedDateTime afterDate, ZonedDateTime beforeDate) {
        double lastIncome = 0.0;
        try {
            lastIncome = loggedUser().getRole().getName().equals(ADMIN)
                    ? loggedUser()
                            .getCreatedEvents()
                                .stream()
                                .map(
                                        x -> x
                                                .getClientReservations()
                                                    .stream()
                                                    .filter(
                                                            y -> formatDate(y.getBookedAt()).isAfter(afterDate)
                                                                    && formatDate(y.getBookedAt()).isBefore(beforeDate))
                                                    .mapToDouble(
                                                            a -> a.getEvent().getTicketPrice() * a.getNumOfPeople())
                                                    .sum())
                                .mapToDouble(s -> s)
                                .sum()
                    : superAdminStatsService.getSuperAdminLastIncome(afterDate, beforeDate, userService.loggedUser());
        }
        catch (NullPointerException e) {
            log.error("Couldn't find Logged in user", e);
        }
        return lastIncome;

    }

    public Stream<Event> getBookings() {
        return loggedUser().getRole().getName().equals(ADMIN)
                ? loggedUser().getCreatedEvents().stream().filter(y -> !y.getClientReservations().isEmpty())
                : superAdminStatsService.getSuperAdminBookings(userService.loggedUser());

    }

    public ResponseEntity<?> getAllIncome() {
        final ZonedDateTime todayDate = ZonedDateTime.now();
        final ZonedDateTime startOfLastWeek = todayDate.minusWeeks(1).with(DayOfWeek.MONDAY);

        /* Last week range */
        final ZonedDateTime endOfLastWeek = startOfLastWeek.plusDays(6);
        /* HashMap to return */
        HashMap<String, Double> incomeData = new HashMap<>();

        var currentWeekIncome = extractCurrentIncome(endOfLastWeek);
        var lastWeekIncome = extractLastIncome(startOfLastWeek, endOfLastWeek);
        log.debug("currentWeekIncome: {}", currentWeekIncome);
        log.debug("lastWeekIncome: {}", lastWeekIncome);

        var thisWeekAvg = (currentWeekIncome - lastWeekIncome) / lastWeekIncome * 100;

        log.debug("thisWeekAvg: {}", thisWeekAvg);

        /* last Month range */
        final ZonedDateTime thisMonthStart = todayDate.minusMonths(1);

        var currentMonthIncome = extractCurrentIncome(thisMonthStart);
        var lastMonthIncome = extractLastIncome(todayDate.minusMonths(2), thisMonthStart);
        var thisMonthAvg = lastMonthIncome == 0 ? 100 : (currentMonthIncome - lastMonthIncome) / lastMonthIncome * 100;

        /* Total Income */
        /*
         * var totalIncome = reservationRepository.findAll().stream().mapToDouble(x ->
         * x.getEvent().getTicketPrice() * x.getNumOfPeople()).sum();
         */
        var totalIncome = loggedUser().getRole().getName().equals(ADMIN)
                ? loggedUser()
                        .getCreatedEvents()
                            .stream()
                            .map(
                                    x -> x
                                            .getClientReservations()
                                                .stream()
                                                .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople())
                                                .sum())
                            .mapToDouble(s -> s)
                            .sum()
                : superAdminStatsService.getSuperAdminTotalIncome(userService.loggedUser());

        var totalIncomeLastMonth = loggedUser().getRole().getName().equals(ADMIN)
                ? loggedUser()
                        .getCreatedEvents()
                            .stream()
                            .map(
                                    x -> x
                                            .getClientReservations()
                                                .stream()
                                                .filter(y -> formatDate(y.getBookedAt()).isBefore(thisMonthStart))
                                                .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople())
                                                .sum())
                            .mapToDouble(s -> s)
                            .sum()
                : superAdminStatsService.getSuperAdminLastMonthIncome(thisMonthStart, userService.loggedUser());

        var totalAvg = totalIncomeLastMonth == 0 ? 100
                : ((totalIncome - totalIncomeLastMonth) / totalIncomeLastMonth * 100);

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

        Map<String, Integer> countries = new HashMap<>();

        getBookings().forEach(x -> x.getClientReservations().forEach(a -> {
            if (!countries.containsKey(a.getAppUser().getCountry())) {
                countries.put(a.getAppUser().getCountry(), 1);
            }
            else {
                int count = countries.get(a.getAppUser().getCountry());
                countries.put(a.getAppUser().getCountry(), count + 1);
            }
        }));
        return ResponseEntity.ok(countries);
    }

    public ResponseEntity<?> classReservationsByClientAge() {
        Map<String, Integer> ageGroup = new HashMap<>();

        ageGroup.put(EIGHTEEN_TO_TWENTY_NINE, 0);
        ageGroup.put(THIRTY_TO_THIRTY_NINE, 0);
        ageGroup.put(FORTY_TO_SIXTY_FIVE, 0);
        ageGroup.put(ABOVE_SIXTY_SIX, 0);

        getBookings().forEach(a -> a.getClientReservations().forEach(x -> {
            var localBirthDate = LocalDate.parse(x.getAppUser().getBirthDate().toString().split(" ")[0]);
            var age = Period.between(localBirthDate, LocalDate.now()).getYears();

            if (age >= 18 && age <= 29) {
                int count = ageGroup.get(EIGHTEEN_TO_TWENTY_NINE);
                ageGroup.put(EIGHTEEN_TO_TWENTY_NINE, count + 1);
            }
            else if (age >= 30 && age <= 39) {
                int count = ageGroup.get(THIRTY_TO_THIRTY_NINE);
                ageGroup.put(THIRTY_TO_THIRTY_NINE, count + 1);
            }
            else if (age >= 40 && age <= 65) {
                int count = ageGroup.get(FORTY_TO_SIXTY_FIVE);
                ageGroup.put(FORTY_TO_SIXTY_FIVE, count + 1);
            }
            else if (age > 66) {
                int count = ageGroup.get(ABOVE_SIXTY_SIX);
                ageGroup.put(ABOVE_SIXTY_SIX, count + 1);
            }
        }));

        return ResponseEntity.ok(ageGroup);

    }

    public String getMonthString(int month) {
        switch (month) {
        case 0:
            return JANUARY;
        case 1:
            return FEBRUARY;
        case 2:
            return MARCH;
        case 3:
            return APRIL;
        case 4:
            return MAY;
        case 5:
            return JUNE;
        case 6:
            return JULY;
        case 7:
            return AUGUST;
        case 8:
            return SEPTEMBER;
        case 9:
            return OCTOBER;
        case 10:
            return NOVEMBER;
        case 11:
            return DECEMBER;
        default:
            return " ";

        }
    }

    public ResponseEntity<?> classReservationsByMonth() {
        Map<String, Integer> monthGroup = new HashMap<>();

        monthGroup.put(JANUARY, 0);
        monthGroup.put(FEBRUARY, 0);
        monthGroup.put(MARCH, 0);
        monthGroup.put(APRIL, 0);
        monthGroup.put(MAY, 0);
        monthGroup.put(JUNE, 0);
        monthGroup.put(JULY, 0);
        monthGroup.put(AUGUST, 0);
        monthGroup.put(SEPTEMBER, 0);
        monthGroup.put(OCTOBER, 0);
        monthGroup.put(NOVEMBER, 0);
        monthGroup.put(DECEMBER, 0);

        getBookings().forEach(x -> {
            x
                    .getClientReservations()
                        .forEach(
                                a -> {
                                    // Loop over MonthGroup keys
                                    for (String key : monthGroup.keySet()) {
                                        if (getMonthString(a.getBookedAt().getMonth()).equals(key)) {
                                            int count = monthGroup.get(key);
                                            monthGroup.put(key, count + 1);
                                        }
                                    }
                                });
        });
        return ResponseEntity.ok(monthGroup);
    }

    public ResponseEntity<?> classReservationsByEventType() {
        HashMap<String, Integer> eventTypeGroup = new HashMap<>();

        var types = eventTypeRepository.findAll();

        types.forEach(a -> eventTypeGroup.put(a.getName(), 0));

        getBookings().forEach(x -> x.getClientReservations().forEach(r -> {
            for (String key : eventTypeGroup.keySet()) {
                if (r.getEvent().getEventType().getName().equals(key)) {
                    int count = eventTypeGroup.get(key);
                    eventTypeGroup.put(key, count + 1);
                }
            }
        }));

        return ResponseEntity.ok(eventTypeGroup);
    }

}
