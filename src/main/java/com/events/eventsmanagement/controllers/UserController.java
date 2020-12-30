package com.events.eventsmanagement.controllers;

import com.events.eventsmanagement.models.User;
import com.events.eventsmanagement.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public User Register(@RequestBody User user) {
        return userRepository.save(user);
    }
}
