package com.events.eventsmanagement.repositories;

import org.springframework.data.repository.CrudRepository;

import com.events.eventsmanagement.models.EventType;

public interface EventTypeRepository extends CrudRepository<EventType, Integer> {
}
