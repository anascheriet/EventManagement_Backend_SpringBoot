package com.events.eventsmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.events.eventsmanagement.models.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
}
