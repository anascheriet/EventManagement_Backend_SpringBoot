package com.events.eventsmanagement.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String eventName;
    private Date eventDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private Date createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @OneToOne
    @JoinColumn(name = "eventtype_id", referencedColumnName = "id")
    private EventType eventType;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Reservation> ClientReservations = new ArrayList<>();

    public Event(String eventName, Date eventDate, User user, EventType eventType) {
        this.createdAt = new Date();
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.user = user;
        this.eventType = eventType;
    }
}