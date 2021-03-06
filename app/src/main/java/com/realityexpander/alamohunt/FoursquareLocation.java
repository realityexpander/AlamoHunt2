/**
 * Filename: FoursquareLocation.java
 * Author: Matthew Huie
 *
 * FoursquareLocation describes a location object from the Foursquare API.
 */

package com.realityexpander.alamohunt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FoursquareLocation implements Serializable {

    // The address of the location.
    String address;

    // The latitude of the location.
    double lat;

    // The longitude of the location.
    double lng;

    // The distance of the location, calculated from the specified location.
    int distance;

    String postalCode;

    String city;

    String state;

    List<String> formattedAddress = new ArrayList<String>();

}