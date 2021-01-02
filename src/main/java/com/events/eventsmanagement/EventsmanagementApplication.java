package com.events.eventsmanagement;

import com.events.eventsmanagement.models.AppUser;
import com.events.eventsmanagement.models.Role;
import com.events.eventsmanagement.repositories.RoleRepository;
import com.events.eventsmanagement.security.UserService;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import java.util.Arrays;
import java.util.List;


@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class EventsmanagementApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(EventsmanagementApplication.class, args);
    }


    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    private final Log logger = LogFactory.getLog(EventsmanagementApplication.class);

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            Role sRole = new Role("SuperAdmin");
            Role aRole = new Role("Admin");
            Role cRole = new Role("Client");

            List<Role> roles = Arrays.asList(sRole, aRole, cRole);

            roleRepository.saveAll(roles);
        }

       /* if (userService.getUsersCount() == 0) {
            logger.info("No user accounts Found, Creating some Users");
            AppUser user = new AppUser("anas", "anas@live.fr", "male", "Moroccan", 22, "anas");
            userService.createUser(user);
        }*/


    }
}
