package com.events.eventsmanagement.controllers;

import com.events.eventsmanagement.models.AppUser;
import com.events.eventsmanagement.repositories.UserRepository;
import com.events.eventsmanagement.security.UserService;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AppUser> Register(@RequestBody AppUser appUser) {
        return ResponseEntity.ok(userService.createUser(appUser));
    }

    @GetMapping("/")
    public ResponseEntity<Iterable<AppUser>> getAllUsers(){
        var users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
}
