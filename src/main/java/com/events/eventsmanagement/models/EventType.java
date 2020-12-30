package com.events.eventsmanagement.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class EventType {
    @Id
    @GeneratedValue
    private int id;
    private String name;
}
