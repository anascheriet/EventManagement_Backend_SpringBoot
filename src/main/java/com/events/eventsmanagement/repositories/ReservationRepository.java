package com.events.eventsmanagement.repositories;

import com.events.eventsmanagement.models.Reservation;
import org.springframework.data.repository.CrudRepository;

public interface ReservationRepository extends CrudRepository<Reservation, Integer> {
}
