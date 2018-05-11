package com.realityexpander.alamohunt;

import java.io.Serializable;

/**
 * Filename: AutocompleteAdapter.java
 * Author: Chris Athanas
 *
 * AutocompleteAdapter represents the adapter for attaching venue data to the ArrayAdapter within
 * MainActivity.  This adapter will handle a list of suggested venues from FoursquareResults and parse them
 * into the view.
 */

public class FoursquareIcon implements Serializable {

    // The id of the category.
    String prefix;

    // The name of the category.
    String suffix;

}

