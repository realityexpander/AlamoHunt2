/**
 * Filename: FoursquareVenue.java
 * Author: Matthew Huie
 *
 * FoursquareVenue describes a venue object from the Foursquare API.
 */

package com.realityexpander.alamohunt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FoursquareVenue implements Serializable {

    // The ID of the venue.
    String id;

    // The name of the venue.
    String name;

    // The rating of the venue, if available.
    double rating;

    // Color for the rating
    String ratingColor;

    // A location object within the venue.
    FoursquareLocation location;

    List<FoursquareCategories> categories = new ArrayList<FoursquareCategories>();



}