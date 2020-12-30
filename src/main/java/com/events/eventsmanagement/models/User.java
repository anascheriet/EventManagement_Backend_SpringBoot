package com.events.eventsmanagement.models;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Setter
@Getter
public class User {

    @Id
    @GeneratedValue
    private int id;
    private String displayName;
    private String email;
    private String gender;
    private String Nationality;
    private int age;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Event> events = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Reservation> reservations = new HashSet<>();


}
