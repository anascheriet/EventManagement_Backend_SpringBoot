package com.events.eventsmanagement.common;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class CommonConstants {
    private CommonConstants() {
    }

    public static final String ADMIN = "Admin";
    public static final String SUPER_ADMIN = "SuperAdmin";

    public static final String EIGHTEEN_TO_TWENTY_NINE = "18 to 29";
    public static final String THIRTY_TO_THIRTY_NINE = "30 to 39";
    public static final String FORTY_TO_SIXTY_FIVE = "40 to 65";
    public static final String ABOVE_SIXTY_SIX = "Above 66";

    public static final String JANUARY = "January";
    public static final String FEBRUARY = "February";
    public static final String MARCH = "March";
    public static final String APRIL = "April";
    public static final String MAY = "May";
    public static final String JULY = "July";
    public static final String AUGUST = "August";
    public static final String SEPTEMBER = "September";
    public static final String OCTOBER = "October";
    public static final String NOVEMBER = "November";
    public static final String DECEMBER = "December";
    public static final String JUNE = "June";

    public static ZonedDateTime formatDate(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault());
    }
}
