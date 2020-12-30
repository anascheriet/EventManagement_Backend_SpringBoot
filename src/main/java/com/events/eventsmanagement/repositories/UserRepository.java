package com.events.eventsmanagement.repositories;

import com.events.eventsmanagement.models.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
}
