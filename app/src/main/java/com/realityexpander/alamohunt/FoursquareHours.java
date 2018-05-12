package com.realityexpander.alamohunt;

import java.io.Serializable;

/**
 * Created by Chris Athanas on 5/11/18.
 */

public class FoursquareHours implements Serializable {

    String status;            // "Closed until 6:00 AM tomorrow"
    Boolean isOpen;           // true
    Boolean isLocalHoliday;   // false
}
