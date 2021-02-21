package com.events.eventsmanagement.Services;

import com.events.eventsmanagement.controllers.BaseController;
import com.events.eventsmanagement.dto.resetPasswordDto;
import com.events.eventsmanagement.dto.updatePasswordDto;
import com.events.eventsmanagement.dto.updateUserDto;
import com.events.eventsmanagement.models.AppUser;
import com.events.eventsmanagement.repositories.RoleRepository;
import com.events.eventsmanagement.repositories.UserRepository;
import com.events.eventsmanagement.security.TokenUtil;
import com.events.eventsmanagement.util.EmailSenderImpl;
import javassist.NotFoundException;
import lombok.SneakyThrows;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.constraints.Null;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private EmailSenderImpl emailSender;

    @Bean
    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User Not found");
        } else
            return user;
    }

    private long getUsersCount() {
        return userRepository.count();
    }

    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (AppUser) authentication.getPrincipal();
    }

    public ResponseEntity<?> createUser(AppUser user) {

        //Response to return;
        String msg = "";

        //Specifying the user role
        boolean roleShouldBeSuperAdmin = getUsersCount() == 0;
        boolean roleShouldBeAdmin = false;

        try {
            if (getCurrentUser().getRole().getName().equals("SuperAdmin"))
                roleShouldBeAdmin = true;
            else
                roleShouldBeAdmin = false;
        } catch (NullPointerException | ClassCastException c) {
            System.out.print("Caught NullPointerException");
        }

        var EmailExists = userRepository.findByEmail(user.getEmail());

        /* var userNameExists = userRepository.findByDisplayname(user.getDisplayName());*/

        if (EmailExists != null) {
            return ResponseEntity.badRequest().body("The provided email already exists, try providing another one");
      /*  } else if (userNameExists != null) {
            return ResponseEntity.badRequest().body("The provided username already exists, try providing another one");*/
        } else {
            user.setIsAccNonLocked(true);
            if (roleShouldBeSuperAdmin) {
                user.setRole(roleRepository.findRoleByName("SuperAdmin"));
                user.setPassword(passwordEncoder().encode(user.getPassword()));
                msg = "Your Account Has been Created! Please Login";

            } else if (roleShouldBeAdmin) {

                user.setRole(roleRepository.findRoleByName("Admin"));
                //generate Password for admin
                byte[] array = new byte[7]; // length is bounded by 7
                new Random().nextBytes(array);
                String generatedPassword = new String(array, Charset.forName("UTF-8"));
                user.setPassword(passwordEncoder().encode(generatedPassword));

                //send credentials to admin in mail
                var mailContent = "You have been made an admin to use the eventor app." +
                        " \n Here are the credentials you can use to log in: \n" +
                        "Email: " + user.getEmail() + ".\n" +
                        "Password: " + generatedPassword;

                emailSender.sendMail(user.getEmail(), "Eventor: You have been made an Admin.", mailContent);

                msg = "Admin Added.";
            } else {
                user.setRole(roleRepository.findRoleByName("Client"));
                user.setPassword(passwordEncoder().encode(user.getPassword()));
                msg = "Your Account Has been Created! Please Login";
            }
        }
        //Save user
        userRepository.save(user);

        return ResponseEntity.ok(msg);
    }

    public ResponseEntity<?> resetPassword(@RequestBody resetPasswordDto resetDto) {
        if (resetDto.getConfirmpassword() == null || resetDto.getPassword() == null || resetDto.getEmail() == null || resetDto.getConfirmationtoken() == null) {
            return ResponseEntity.badRequest().body("Missing arguments, try again");
        }

        var mail = tokenUtil.extractClaims(resetDto.getConfirmationtoken().getToken()).getSubject();

        if (!mail.equals(resetDto.getEmail())) {
            return ResponseEntity.badRequest().body("Invalid Url, make sure the link you're using is the one that was sent to your mail adress");
        }

        if (!resetDto.getPassword().equals(resetDto.getConfirmpassword())) {
            return ResponseEntity.badRequest().body("The two entered passwords do not match, try again");
        }

        var user = userRepository.findByEmail(resetDto.getEmail());

        if (passwordEncoder().matches(resetDto.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("The entered password matches the old one, try entering a new one");
        }

        user.setPassword(passwordEncoder().encode(resetDto.getPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Your Password Has been Updated! please Log in using your new Password");
    }

    public ResponseEntity<?> updateUserInfo(updateUserDto updateDto) {
        var user = userRepository.findById(getCurrentUser().getId());
        //check if email exists
        var emailExists = userRepository.findByEmail(updateDto.getEmail()) != null;

        //check if user is keeping his email
        var sameEmail = user.get().getEmail().equals(updateDto.getEmail());

        if (emailExists && !sameEmail) {
            return ResponseEntity.badRequest().body("Email already exists, try another one.");
        } else if (updateDto.getDisplayName().equals(null) || updateDto.getEmail().equals(null) || updateDto.getCountry().equals(null)) {
            return ResponseEntity.badRequest().body("Please fill all the fields before submitting");
        } else {
            user.map(u -> {
                u.setEmail(updateDto.getEmail());
                u.setCountry(updateDto.getCountry());
                u.setGender(updateDto.getGender());
                u.setDisplayName(updateDto.getDisplayName());
                return ResponseEntity.ok(userRepository.save(u));
            });
        }
        return ResponseEntity.ok("Your profile has been updated !");
    }

    public ResponseEntity<?> updatePassword(updatePasswordDto updatePassDto) {
        var user = userRepository.findById(getCurrentUser().getId());

        if (!updatePassDto.getPassword().equals(updatePassDto.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("The Two passwords Should Match");
        } else
            user.map(u -> {
                u.setPassword(passwordEncoder().encode(updatePassDto.getPassword()));
                return ResponseEntity.ok(userRepository.save(u));
            });
        return ResponseEntity.ok("Your Password has been updated , please Re-Log in");
    }

}