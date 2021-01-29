package com.events.eventsmanagement.security;

import com.events.eventsmanagement.controllers.BaseController;
import com.events.eventsmanagement.dto.resetPasswordDto;
import com.events.eventsmanagement.models.AppUser;
import com.events.eventsmanagement.repositories.RoleRepository;
import com.events.eventsmanagement.repositories.UserRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TokenUtil tokenUtil;


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

        var userExists = userRepository.findByEmail(user.getEmail());
        if (userExists != null) {
            return ResponseEntity.unprocessableEntity().build();
        } else

            user.setPassword(passwordEncoder().encode(user.getPassword()));

        if (roleShouldBeSuperAdmin) {
            user.setRole(roleRepository.findRoleByName("SuperAdmin"));
        } else if (roleShouldBeAdmin) {
            user.setRole(roleRepository.findRoleByName("Admin"));
        } else {
            user.setRole(roleRepository.findRoleByName("Client"));
        }


        return ResponseEntity.ok(userRepository.save(user));
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


}