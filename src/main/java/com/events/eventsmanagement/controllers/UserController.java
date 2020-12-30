package com.events.eventsmanagement.controllers;

import com.events.eventsmanagement.models.User;
import com.events.eventsmanagement.repositories.UserRepository;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sun.jvm.hotspot.debugger.Page;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<User> Register(@RequestBody User user) {
        User createdUser = userRepository.save(user);
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("/")
    public ResponseEntity<Iterable<User>> getAllUsers(){
        var users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
}
