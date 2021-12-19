package com.events.eventsmanagement.services;

import static com.events.eventsmanagement.common.CommonConstants.SUPER_ADMIN;
import static com.events.eventsmanagement.common.CommonConstants.formatDate;

import java.time.ZonedDateTime;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.events.eventsmanagement.models.AppUser;
import com.events.eventsmanagement.models.Event;
import com.events.eventsmanagement.repositories.EventRepository;

@Service
public class SuperAdminStatsService {

    @Autowired
    private EventRepository eventRepository;

    double getSuperAdminLastMonthIncome(ZonedDateTime currentMonthStart, AppUser appUser) {
        return appUser.getRole().getName().equals(SUPER_ADMIN)
                ? eventRepository
                        .findAll()
                            .stream()
                            .map(
                                    x -> x
                                            .getClientReservations()
                                                .stream()
                                                .filter(y -> formatDate(y.getBookedAt()).isBefore(currentMonthStart))
                                                .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople())
                                                .sum())
                            .mapToDouble(s -> s)
                            .sum()
                : 0;
    }

    double getSuperAdminTotalIncome(AppUser appUser) {
        return appUser.getRole().getName().equals(SUPER_ADMIN)
                ? eventRepository
                        .findAll()
                            .stream()
                            .map(
                                    x -> x
                                            .getClientReservations()
                                                .stream()
                                                .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople())
                                                .sum())
                            .mapToDouble(s -> s)
                            .sum()
                : 0;
    }

    double getSuperAdminLastIncome(ZonedDateTime afterDate, ZonedDateTime beforeDate, AppUser appUser) {
        return appUser.getRole().getName().equals(SUPER_ADMIN)
                ? eventRepository
                        .findAll()
                            .stream()
                            .map(
                                    x -> x
                                            .getClientReservations()
                                                .stream()
                                                .filter(
                                                        y -> formatDate(y.getBookedAt()).isAfter(afterDate)
                                                                && formatDate(y.getBookedAt()).isBefore(beforeDate))
                                                .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople())
                                                .sum())
                            .mapToDouble(s -> s)
                            .sum()
                : 0;
    }

    public double getSuperAdminCurrentIncome(ZonedDateTime afterDate, AppUser appUser) {
        return appUser.getRole().getName().equals(SUPER_ADMIN)
                ? eventRepository
                        .findAll()
                            .stream()
                            .map(
                                    x -> x
                                            .getClientReservations()
                                                .stream()
                                                .filter(y -> formatDate(y.getBookedAt()).isAfter(afterDate))
                                                .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople())
                                                .sum())
                            .mapToDouble(s -> s)
                            .sum()
                : 0;
    }

    public Stream<Event> getSuperAdminBookings(AppUser appUser) {
        return appUser.getRole().getName().equals(SUPER_ADMIN)
                ? eventRepository.findAll().stream().filter(y -> !y.getClientReservations().isEmpty())
                : null;
    }
}
