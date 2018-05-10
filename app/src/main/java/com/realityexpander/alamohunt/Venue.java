/**
 * Filename: Venue.java
 * Author: Chris Athanas
 *
 * Holds Venue information passed from PlacePickerActivity to MapsActivity.
 * Reduces information from the foursquare API for use within our app.
 */

package com.realityexpander.alamohunt;

import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;

public class Venue implements Serializable {
    private String name;
    private String Id;
    private String categoryName;
    private Double latitude;
    private Double longitude;
    private Marker marker;
    private String categoryIconURL;
    private String venueURL;

    public Venue() {
        this("","","");
    }

    public Venue(String name,
                 String id,
                 String categoryName){
        this.setName(name);
        this.setId(id);
        this.setCategoryName(categoryName);
    }

    public Venue(String name,
                 String id,
                 String categoryName,
                 Double latitude,
                 Double longitude){
        this(name, id, categoryName);
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }

    public Venue(String name,
                 String id,
                 String categoryName,
                 Double latitude,
                 Double longitude,
                 String categoryIconURL,
                 String venueURL){
        this(name, id, categoryName, latitude, longitude);
        this.setCategoryIconURL(categoryIconURL);
        this.setVenueURL(venueURL);
    }

    public String getName() {
        return name;
    }
    public String getId() {
        return Id;
    }
    public String getCategoryName() {
        return categoryName;
    }
    public Double getLatitude() {return latitude;}
    public Double getLongitude() {return longitude;}
    public Marker getMarker(){return marker;}
    public String getCategoryIconURL() {
        return categoryIconURL;
    }
    public String getVenueURL() {return venueURL;}

    public void setName(String name) {
        this.name = name;
    }
    public void setId(String id) {
        this.Id = id;
    }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public void setMarker(Marker marker) {this.marker = marker;}
    public void setCategoryIconURL(String categoryIconURL) {
        this.categoryIconURL = categoryIconURL;
    }
    public void setVenueURL(String venueURL) {this.venueURL = venueURL;}

}