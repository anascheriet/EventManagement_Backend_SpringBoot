package com.events.eventsmanagement.models;

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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    public Reservation(Date reservationDate, int numOfPeople, User user)
    {
        this.bookedAt = new Date();
        this.reservationDate = reservationDate;
        this.numOfPeople = numOfPeople;
        this.user = user;
    }

}
