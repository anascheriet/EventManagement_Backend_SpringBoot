package com.events.eventsmanagement.controllers;

import com.events.eventsmanagement.dto.JwtResponse;
import com.events.eventsmanagement.models.AppUser;
import com.events.eventsmanagement.repositories.UserRepository;
import com.events.eventsmanagement.security.TokenUtil;
import com.events.eventsmanagement.security.UserService;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.events.eventsmanagement.dto.loginDto;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController extends BaseController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> Register(@RequestBody AppUser appUser) {
        return userService.createUser(appUser);
    }

    /*@GetMapping("/")
    public ResponseEntity<Iterable<AppUser>> getAllUsers() {
        var users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }*/

    @GetMapping("/loggedInUser")
    public ResponseEntity<Optional<AppUser>> getLoggedInUser() {
        var userId = getCurrentUser().getId();
        var user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return ResponseEntity.unprocessableEntity().build();
        } else
            return ResponseEntity.ok(user);
    }

    @PostMapping("/")
    public ResponseEntity<JwtResponse> LogIn(@RequestBody loginDto loginRequest) {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = userService.loadUserByUsername(loginRequest.getUsername());
        String token = tokenUtil.generateToken(userDetails);

        JwtResponse response = new JwtResponse(token);

        return ResponseEntity.ok(response);
    }

}
