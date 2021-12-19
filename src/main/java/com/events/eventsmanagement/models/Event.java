package com.events.eventsmanagement.models;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @Column(length = 3000)
    private String description;
    private String country;
    private String city;
    private int availableTickets;
    private Float ticketPrice;
    private Date eventDate;
    private String imagePath;
    //private MultipartFile image;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private Date createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appuser_id")
    @JsonBackReference(value = "user-events")
    private AppUser appUser;

    @OneToOne
    @JoinColumn(name = "eventtype_id", referencedColumnName = "id")
    private EventType eventType;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Reservation> clientReservations = new ArrayList<>();

    public Event(String eventName, EventType eventType, String country, String city, String description, Date eventDate, Float ticketPrice, int availableTickets, AppUser appUser, String imagePath) {
        this.createdAt = new Date();
        this.eventName = eventName;
        this.description = description;
        this.country = country;
        this.city = city;
        this.availableTickets = availableTickets;
        this.ticketPrice = ticketPrice;
        this.eventDate = eventDate;
        this.eventType = eventType;
        this.appUser = appUser;
        this.imagePath = imagePath;
    }
}
