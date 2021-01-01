package com.events.eventsmanagement.security;


import com.events.eventsmanagement.models.AppUser;
import com.events.eventsmanagement.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    private PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        return new User("anas", passwordEncoder().encode("anas"), AuthorityUtils.NO_AUTHORITIES);
    }

    public AppUser createUser (AppUser user)
    {
        user.setPassword(passwordEncoder().encode(user.getPassword()));
        return this.userRepository.save(user);
    }

    public Iterable<AppUser> getAll(){
        return userRepository.findAll();
    }
}
