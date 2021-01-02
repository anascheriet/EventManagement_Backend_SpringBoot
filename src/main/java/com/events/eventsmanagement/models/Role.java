package com.events.eventsmanagement.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue
    private int id;
    private String name;

    public Role(String name) {
        this.name = name;
    }
}