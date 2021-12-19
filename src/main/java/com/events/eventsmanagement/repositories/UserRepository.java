package com.events.eventsmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.events.eventsmanagement.models.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Integer> {

    /*User findByEmailAndPassword(String email, String Password) throws AuthException;


    Integer getCountByEmail(String email);

    User findById (int id);*/
    public AppUser findByEmail(String email);
}
