package com.events.eventsmanagement.repositories;

import com.events.eventsmanagement.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface EventRepository extends JpaRepository<Event, Integer> {
}
