package com.events.eventsmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.events.eventsmanagement.models.Event;

public interface EventRepository extends JpaRepository<Event, Integer> {
}
