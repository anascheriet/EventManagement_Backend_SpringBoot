package com.events.eventsmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class EventsmanagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventsmanagementApplication.class, args);
    }

}
