package com.events.eventsmanagement.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue
    private int id;
    private int numOfPeople;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "booked_at", nullable = false, updatable = false)
    @CreatedDate
    private Date bookedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appuser_id")
    @JsonBackReference(value = "user-bookings")
    private AppUser appUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id")
    @JsonBackReference
    private Event event;

    public Reservation(int numOfPeople, AppUser appUser, Event event) {
        this.bookedAt = new Date();
        this.numOfPeople = numOfPeople;
        this.appUser = appUser;
        this.event = event;
    }
}
