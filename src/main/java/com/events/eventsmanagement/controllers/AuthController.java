package com.events.eventsmanagement.controllers;

import com.events.eventsmanagement.dto.*;
import com.events.eventsmanagement.models.AppUser;
import com.events.eventsmanagement.repositories.UserRepository;
import com.events.eventsmanagement.security.TokenUtil;
import com.events.eventsmanagement.Services.UserService;
import com.events.eventsmanagement.util.EmailSenderImpl;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
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
        return userService.createUser(appUser);

    }

    /*@PostMapping("/addAdmin")
    public ResponseEntity<?> addAdmin(@RequestBody AppUser admin) {
        userService.createAdmin(admin);
        return ResponseEntity.ok(admin.getAuthorities());
    }*/

    @GetMapping("/adminData")
    public ResponseEntity<?> getAllAdmins() {
        var bdAdmins = userRepository.findAll().stream().filter(u -> u.getRole().getId() == 2);
        System.out.println("Admins --------------- " + bdAdmins);
        List<adminDataDto> adminData = new ArrayList<>();

        bdAdmins.forEach(u -> {
            var localBirthDate = LocalDate.parse(u.getBirthDate().toString().split(" ")[0]);
            var age = Period.between(localBirthDate, LocalDate.now()).getYears();
            var revenue = u.getCreatedEvents().stream().filter(y -> !y.getClientReservations().isEmpty())
                    .map(x -> x.getClientReservations().stream()
                            .mapToDouble(a -> a.getEvent().getTicketPrice() * a.getNumOfPeople())
                            .sum()).mapToDouble(s -> s).sum();
            var admin = new adminDataDto(u.getId(), u.getEmail(), u.getDisplayName(), age, u.getCountry(), u.getIsAccNonLocked(), revenue);
            adminData.add(admin);
        });
        return ResponseEntity.ok(adminData);
    }

    @GetMapping("/lockUnlockAdminAccount/{id}")
    public ResponseEntity<?> lockUnlockAdminAccount(@PathVariable int id) {
        var user = userRepository.findById(id);

        user.map(u -> {
                    u.setIsAccNonLocked(!u.getIsAccNonLocked());
                    userRepository.save(u);
                    return ResponseEntity.ok(u.getIsAccNonLocked());
                }
        );

        String msg = user.get().getIsAccNonLocked() ?
                user.get().getDisplayName() + "'s Account Has been unlocked !" :
                user.get().getDisplayName() + "'s Account Has been locked !";

        return ResponseEntity.ok(msg);
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
        } catch (LockedException le) {
            var err = new errorResponse("Your Account is locked, Please check with your supervisor");
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

        return ResponseEntity.ok("An email with a link has been sent to your mail address, please follow the link so you can reset your password.");
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody resetPasswordDto resetDto) {
        return userService.resetPassword(resetDto);
    }

    @PatchMapping("/updateProfile")
    public ResponseEntity<?> updateProfile(@RequestBody updateUserDto userDto) {
        return userService.updateUserInfo(userDto);
    }

    @PatchMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(@RequestBody updatePasswordDto updatePasswordDto) {
        return userService.updatePassword(updatePasswordDto);
    }

}