package com.events.eventsmanagement.controllers;

import com.events.eventsmanagement.dto.GetUserDto;
import com.events.eventsmanagement.dto.JwtResponse;
import com.events.eventsmanagement.dto.errorResponse;
import com.events.eventsmanagement.models.AppUser;
import com.events.eventsmanagement.repositories.UserRepository;
import com.events.eventsmanagement.security.TokenUtil;
import com.events.eventsmanagement.security.UserService;
import com.events.eventsmanagement.util.EmailSenderImpl;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.events.eventsmanagement.dto.loginDto;

import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
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

    @Autowired
    private EmailSenderImpl emailSender;

    @PostMapping("/register")
    public ResponseEntity<?> Register(@RequestBody AppUser appUser) {
        userService.createUser(appUser);
        return ResponseEntity.ok(appUser.getAuthorities());
    }

    @GetMapping("/allUsers")
    public ResponseEntity<Iterable<AppUser>> getAllUsers() {
        var users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/loggedInUser")
    public ResponseEntity<Optional<AppUser>> getLoggedInUser() {
        var userId = getCurrentUser().getId();
        var user = userRepository.findById(userId);
        if (!user.isPresent()) {
            return ResponseEntity.unprocessableEntity().build();
        } else
            return ResponseEntity.ok(user);
    }


    @PostMapping("/login")
    public ResponseEntity<?> LogIn(@RequestBody loginDto loginRequest) {


        try {
            final Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (BadCredentialsException e) {
            var err = new errorResponse("Bad Credentials, please make sure your email and password are correct");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
        }

        UserDetails userDetails = userService.loadUserByUsername(loginRequest.getUsername());
        String token = tokenUtil.generateToken(userDetails);

        JwtResponse response = new JwtResponse(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<?> forgotPassword(@RequestBody loginDto loginDto) {
        var user = userRepository.findByEmail(loginDto.getUsername());
        System.out.println(user);
        if (user == null) {
            return ResponseEntity.badRequest().body("There's no user with this email, try providing your accurate email");
        }
        UserDetails userDetails = userService.loadUserByUsername(user.getEmail());
        String confirmationToken = tokenUtil.generateToken(userDetails);

        var mailContent = "To complete the password reset process, please click here: "
                + "http://localhost:3000/authentication/reset?token=" + confirmationToken + "&email=" + loginDto.getUsername();

        emailSender.sendMail(user.getEmail(), "Reset Your Password", mailContent);

        return ResponseEntity.ok("An email with a link has been sent to your mail address, please follow the link so you can reset your password");
    }
}
