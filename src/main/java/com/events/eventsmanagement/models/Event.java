package com.events.eventsmanagement.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.web.multipart.MultipartFile;

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
    //private MultipartFile image;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private Date createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appuser_id")
    @JsonBackReference(value="user-events")
    private AppUser appUser;

    @OneToOne
    @JoinColumn(name = "eventtype_id", referencedColumnName = "id")
    private EventType eventType;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Reservation> clientReservations = new ArrayList<>();

    public Event(String eventName, Date eventDate, AppUser appUser, EventType eventType) {
        this.createdAt = new Date();
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.appUser = appUser;
        this.eventType = eventType;
    }
}