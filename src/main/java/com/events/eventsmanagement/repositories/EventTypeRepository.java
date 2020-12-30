package com.events.eventsmanagement.repositories;

import com.events.eventsmanagement.models.EventType;
import org.springframework.data.repository.CrudRepository;

public interface EventTypeRepository extends CrudRepository<EventType, Integer> {
}
