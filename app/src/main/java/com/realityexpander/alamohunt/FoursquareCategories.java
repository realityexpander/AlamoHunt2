/**
 * Filename: FoursquareCategories.java
 * Author: Chris Athanas
 *
 * FoursquareCategories describes Categories of the venue within a given venue from the Foursquare API.
 */

package com.realityexpander.alamohunt;

import java.io.Serializable;

/**
 *
 */

public class FoursquareCategories implements Serializable {

    // The id of the category.
    String id;

    // The name of the category.
    String name;

    // The shortName of the category.
    String shortName;

    FoursquareIcon icon;
}