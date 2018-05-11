/**
 * Filename: FoursquareService.java
 * Author: Chris Athanas
 *
 * FoursquareService provides a Retrofit interface for the Foursquare API.
 */

package com.realityexpander.alamohunt;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FoursquareService {


    // A request to search for nearby places via Foursquare API
    @GET("search/recommendations?v=20161101&near=Austin,TX")
    Call<FoursquareJSON> searchForPlace(@Query("client_id") String clientID,
                                        @Query("client_secret") String clientSecret,
                                        @Query("query") String searchQuery);

    // A request to search for nearby recommendations via the Foursquare API.
    @GET("search/recommendations?v=20161101&limit=20&near=Austin,TX")
    Call<FoursquareJSON> searchAutoComplete(@Query("client_id") String clientID,
                                            @Query("client_secret") String clientSecret,
                                            @Query("query") String autoCompleteString      );

    // Request venue information
    @GET("venues/{venue_id}?v=20161101")
    Call<FoursquareJSON> searchVenueID( @Path("venue_id") String venueID,
                                        @Query("client_id") String clientID,
                                        @Query("client_secret") String clientSecret );
}