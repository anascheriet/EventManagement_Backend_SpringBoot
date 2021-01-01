package com.events.eventsmanagement.repositories;

import com.events.eventsmanagement.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<AppUser, Integer> {

    /*User findByEmailAndPassword(String email, String Password) throws AuthException;


    Integer getCountByEmail(String email);

    User findById (int id);*/
    public AppUser findByEmail(String email);
}
