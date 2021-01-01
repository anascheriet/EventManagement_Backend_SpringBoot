package com.events.eventsmanagement.security;

import com.events.eventsmanagement.models.AppUser;
import com.events.eventsmanagement.repositories.UserRepository;
import javassist.NotFoundException;
import lombok.SneakyThrows;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Bean
    private PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        AppUser user = userRepository.findByEmail(userName);
        if (user == null) {
            throw new UsernameNotFoundException("User Not found");
        } else
            return user;
    }

    public ResponseEntity<AppUser> createUser(AppUser user) {
        var userExists = userRepository.findByEmail(user.getEmail());
        if (userExists != null) {
            return ResponseEntity.unprocessableEntity().build();
        }

        else

        user.setPassword(passwordEncoder().encode(user.getPassword()));
        return ResponseEntity.ok(this.userRepository.save(user));
    }

    public Iterable<AppUser> getAll() {
        return userRepository.findAll();
    }
}
