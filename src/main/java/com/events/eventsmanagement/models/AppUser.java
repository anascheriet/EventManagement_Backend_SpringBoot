package com.events.eventsmanagement.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;

@Data
@NoArgsConstructor
@Entity
@Setter
@Getter
public class AppUser implements UserDetails {

    @Id
    @GeneratedValue
    private int id;
    private String displayName;
    /*@NotEmpty*/
    private String email;
    private String gender;
    private String nationality;
    private int age;
    private String password;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL)
    @JsonManagedReference(value="user-events")
    private List<Event> createdEvents = new ArrayList<>();

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL)
    @JsonManagedReference(value="user-bookings")
    private List<Reservation> bookedReservations = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    private Role role;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
