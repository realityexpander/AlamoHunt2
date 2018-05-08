/**
 * Filename: MapActivity.java
 * Author: Chris Athanas
 * Based on Mr. Jitters Foursquare sample app
 *
 * MapActivity represents a map view of a specific venue from PlacePickerActivity.  This activity will
 * allow a user to link back to the Foursquare venue page.
 */

package com.realityexpander.alamohunt;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import static android.view.View.GONE;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    // The Google Maps object.
    private GoogleMap mMap;

    // The details of the venue that is being displayed.
    private String venueID;
    private String venueName;
    private double venueLatitude;
    private double venueLongitude;

    private ArrayList<Venue> venuesList;
    private Marker markerAustin;

    private static final double AUSTIN_TX_LATITUDE = 30.2672;
    private static final double AUSTIN_TX_LONGITUDE = -97.7431;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Retrieves venue(s) details from the intent sent from PlacePickerActivity
        Bundle venue = getIntent().getExtras();
        venuesList = (ArrayList<Venue>)getIntent().getSerializableExtra("venuesList");

        SupportMapFragment mapFragment = null;
        if (venuesList == null) { // Show Single venue screen
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            // Hide the multi venue map view
            LinearLayout ll = findViewById(R.id.multiVenueMap);
            ll.setVisibility(GONE);

            // Single venue details
            venueID = venue.getString("ID");
            venueName = venue.getString("name");
            venueLatitude = venue.getDouble("latitude");
            venueLongitude = venue.getDouble("longitude");
            setTitle(venueName);

        } else { // Show multi venue screen
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map2);

            // Hide the single venue map views
            LinearLayout ll = findViewById(R.id.appbar);
            ll.setVisibility(GONE);
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setVisibility(GONE);
        }
        mapFragment.getMapAsync(this);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        int w = getResources().getDisplayMetrics().widthPixels;
        int h = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (w * 0.12); // offset from edges of the map 12% of screen

//        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
//            @Override
//            public void onMapLoaded() {
//                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 250));
//            }
//        });

                Marker marker=null;
                // Creates and displays marker and info window for the venue
                if(venuesList == null) { // only one venue
                    // Centers and zooms the map into the selected venue
                    LatLng venueLatLong = new LatLng(venueLatitude, venueLongitude);
                    marker = mMap.addMarker(new MarkerOptions()
                            .position(venueLatLong)
                            .title(venueName)
                            .snippet("View on Foursquare"));

                    // Add center of Austin
                    LatLng austinLatLong = new LatLng(AUSTIN_TX_LATITUDE, AUSTIN_TX_LONGITUDE);
                    markerAustin = mMap.addMarker(new MarkerOptions()
                            .position(austinLatLong)
                            .title("Austin, Texas")
                            .snippet("Home of Alamo Drafthouse"));

                    LatLngBounds bounds = new LatLngBounds.Builder()
                            .include(venueLatLong)
                            .include(austinLatLong)
                            .build();

//                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 250));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, w, h, padding));
                } else { // the list of venues

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for(int i=0; i<venuesList.size(); i++) {
                        Double lat = venuesList.get(i).getLatitude();
                        Double lng = venuesList.get(i).getLongitude();
                        LatLng venueLatLong = new LatLng(lat, lng );

                        builder.include(venueLatLong);

                        marker = mMap.addMarker(new MarkerOptions()
                                .position(venueLatLong)
                                .title(venuesList.get(i).getName())
                                .snippet(venuesList.get(i).getCategoryName()));
                        venuesList.get(i).setMarker(marker);
                    }
                    LatLngBounds bounds = builder.build();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, w, h, padding));
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                }
                marker.showInfoWindow();

//            }
//        });


        mMap.setOnInfoWindowClickListener(this);

//        LatLng austinLatLong = new LatLng(AUSTIN_TX_LATITUDE, AUSTIN_TX_LONGITUDE);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(austinLatLong, 16));

        // Checks for location permissions at runtime (required for API >= 23)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Shows the user's current location
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        String theVenueID = null;

        // Opens the Foursquare venue page when a user clicks on the info window of the venue

        // one or many venues?
        if(venuesList==null) {
            theVenueID = venueID;
            if (!marker.equals(markerAustin)) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://foursquare.com/v/" + theVenueID));
                startActivity(browserIntent);
        }
        } else {
            // Look thru the venueList for a matching marker
            for (int i = 0; i < venuesList.size(); i++) {
                if (venuesList.get(i).getMarker().equals(marker)) {
                    theVenueID = venuesList.get(i).getId();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://foursquare.com/v/" + theVenueID));
                    startActivity(browserIntent);
                }
            }
        }


    }
}
