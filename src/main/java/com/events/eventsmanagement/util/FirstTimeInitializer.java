package com.events.eventsmanagement.util;

import com.events.eventsmanagement.models.AppUser;
import com.events.eventsmanagement.security.UserService;
import org.apache.juli.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.apache.juli.logging.Log;

public class FirstTimeInitializer implements CommandLineRunner {

    private final Log logger = LogFactory.getLog(FirstTimeInitializer.class);

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        if (userService.getAll() == null) {
            logger.info("No user accounts Found, Creating some Users");
            AppUser user = new AppUser("anas", "anas@live.fr", "male", "Moroccan", "SuperAdmin", 22, "password");
            userService.createUser(user);
        }
    }
}
