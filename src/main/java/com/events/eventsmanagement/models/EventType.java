package com.events.eventsmanagement.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class EventType {
    @Id
    @GeneratedValue
    private int id;
    private String name;
}
