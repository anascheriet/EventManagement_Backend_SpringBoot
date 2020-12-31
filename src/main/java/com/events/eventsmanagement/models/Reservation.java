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
    private Date reservationDate;
    private int numOfPeople;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "booked_at", nullable = false, updatable = false)
    @CreatedDate
    private Date bookedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id")
    @JsonBackReference
    private Event event;

    public Reservation(Date reservationDate, int numOfPeople, User user, Event event)
    {
        this.bookedAt = new Date();
        this.reservationDate = reservationDate;
        this.numOfPeople = numOfPeople;
        this.user = user;
        this.event = event;
    }

}
