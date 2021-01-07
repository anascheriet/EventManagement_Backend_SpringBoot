package com.events.eventsmanagement.security;

import com.events.eventsmanagement.controllers.BaseController;
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


}