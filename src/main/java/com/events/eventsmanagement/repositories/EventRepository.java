package com.events.eventsmanagement.repositories;

import com.events.eventsmanagement.models.Event;
import org.springframework.data.repository.CrudRepository;

public interface EventRepository extends CrudRepository<Event, Integer> {
}
