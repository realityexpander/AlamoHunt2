/**
 * Filename: MapActivity.java
 * Author: Chris Athanas
 * Based on Mr. Jitters Foursquare sample app
 *
 * MapActivity represents a map view of either a specific venue or a list of venues
 * from PlacePickerActivity.  This activity will allow a user click into the ViewVenueActivity.
 */

package com.realityexpander.alamohunt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    // The Google Maps object.
    private GoogleMap mMap;

    // The details of the venue that is being displayed.
    private ArrayList<Venue> venuesList;
    private Marker markerAustin;

    private static final double AUSTIN_TX_LATITUDE = 30.2672;
    private static final double AUSTIN_TX_LONGITUDE = -97.7431;

    // The base URL for the Foursquare API
    private String foursquareBaseURL = "https://api.foursquare.com/v2/";

    // The client ID and client secret for authenticating with the Foursquare API
    private String foursquareClientID;
    private String foursquareClientSecret;

    ArrayList<String> favoriteVenueIDs;
    boolean favorited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Retrieves venue(s) details from the intent sent from PlacePickerActivity
        Bundle venue = getIntent().getExtras();
        venuesList = (ArrayList<Venue>)getIntent().getSerializableExtra("venuesList");

        SupportMapFragment mapFragment = null;
        if (venuesList.size() == 1) { // Show Single venue screen
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            // **** HIDE
            // Hide the multi venue map view
            LinearLayout ll = findViewById(R.id.multiVenueMap);
            ll.setVisibility(GONE);


            // *** FILL IN
            // Fill in data field views for Single venue details
            final Venue cv = venuesList.get(0); // current venue
            ImageView ivCategoryIcon = (ImageView) findViewById(R.id.ivCategoryIcon);
            Picasso.with(getApplicationContext()).load(cv.getCategoryIconURL()).into(ivCategoryIcon);

            TextView tvCategoryName = (TextView) findViewById(R.id.tvCategoryName);
            tvCategoryName.setText(cv.getCategoryName());

            final TextView tvVenueURL = (TextView) findViewById(R.id.tvFoursquareWebsite);
            tvVenueURL.setText(cv.getVenueURL());

            setTitle(venuesList.get(0).getName());

            favoriteVenueIDs = LoadPrefs("favoriteVenueIDs");
            if (favoriteVenueIDs == null)
                favoriteVenueIDs = new ArrayList<>();
            // The FAB favorites the venue true/false
            final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setImageResource(android.R.drawable.star_big_off); // default to off
            // Set the favorite status on the button
            if ( favoriteVenueIDs != null) {
                for (int i = 0; i < favoriteVenueIDs.size(); i++)
                    if (favoriteVenueIDs.get(i).equals(cv.getId())) {
                        favorited = true;
                        fab.setImageResource(android.R.drawable.star_big_on);
                    }
            }
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Toggle the favorites
                    if (!favorited) {
                        // toggle the button image to favorited
                        fab.setImageResource(android.R.drawable.star_big_on);
                        favorited = true;
                        favoriteVenueIDs.add(cv.getId());
                        SavePrefs(favoriteVenueIDs, "favoriteVenueIDs");
                    } else {
                        // toggle the button image
                        fab.setImageResource(android.R.drawable.star_big_off);
                        favorited = false;
                        for (int i = 0; i < favoriteVenueIDs.size(); i++)
                            if (cv.getId().equals(favoriteVenueIDs.get(i)))
                                favoriteVenueIDs.remove(i);

                        SavePrefs(favoriteVenueIDs, "favoriteVenueIDs");
                    }
                }
            });


            // *** GET VENUE DETAILS
            // Get details for the venueID
            // Builds Retrofit and FoursquareService objects for calling the Foursquare API and parsing with GSON
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(foursquareBaseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            FoursquareService foursquare = retrofit.create(FoursquareService.class);

            // Gets the stored Foursquare API client ID and client secret from XML
            foursquareClientID = getResources().getString(R.string.foursquare_client_id);
            foursquareClientSecret = getResources().getString(R.string.foursquare_client_secret);

            // Calls the Foursquare API to get venue details
            Call<FoursquareJSON> searchCall = foursquare.searchVenueID(
                    venuesList.get(0).getId(),
                    foursquareClientID,
                    foursquareClientSecret
                    );
            searchCall.enqueue(new Callback<FoursquareJSON>() {
                @Override
                public void onResponse(Call<FoursquareJSON> call, Response<FoursquareJSON> response) {

                    // Gets the venue object from the JSON response
                    FoursquareJSON fjson = response.body();
                    FoursquareResponse fr = fjson.response;
                    FoursquareVenue fv = fr.venue;

                    tvVenueURL.setText(Double.toString(fv.rating));
                }

                @Override
                public void onFailure(Call<FoursquareJSON> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Can't connect to Foursquare's servers!", Toast.LENGTH_LONG).show();
                    finish();
                }
            });


        } else { // Show multi venue screen
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map2);

            // Hide the single venue map views
            LinearLayout ll = findViewById(R.id.appbar);
            ll.setVisibility(GONE);
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setVisibility(GONE);
            NestedScrollView nsv = findViewById(R.id.nestedScrollView);
            nsv.setVisibility(GONE);

            // Show the multi venue map view
            ll = findViewById(R.id.multiVenueMap);
            ll.setVisibility(VISIBLE);
        }
        mapFragment.getMapAsync(this);


    }

    public void SavePrefs( ArrayList<String> list, String key) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }

    public ArrayList<String> LoadPrefs( String key ) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
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
        int padding = (int) (w * 0.22); // offset from edges of the map 22% of screen

//        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
//            @Override
//            public void onMapLoaded() {
//                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 250));
//            }
//        });

        // Creates and displays marker and info window for the venue
        // If there is a single item in the array, then do a single venue layout.
        // If there are multiple items in the array, then do a multi-venue layout.
        Marker marker=null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        // Centers and zooms the map into the selected venue + city of Austin
        if(venuesList.size() == 1) { // only one venue? Add center of austin
            // Add center of Austin
            LatLng austinLatLong = new LatLng(AUSTIN_TX_LATITUDE, AUSTIN_TX_LONGITUDE);
            markerAustin = mMap.addMarker(new MarkerOptions()
                    .position(austinLatLong)
                    .title("Austin, Texas")
                    .snippet("Home of Alamo Drafthouse"));
            builder.include(austinLatLong);

//                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 250)); // CDA NOTE - calling this sometimes crashes...
        }

         // Find the bounding box for the list of venues
        for(int i=0; i<venuesList.size(); i++) {
            Double lat = venuesList.get(i).getLatitude();
            Double lng = venuesList.get(i).getLongitude();
            LatLng venLatLong = new LatLng(lat, lng );

            builder.include(venLatLong);

            marker = mMap.addMarker(new MarkerOptions()
                    .position(venLatLong)
                    .title(venuesList.get(i).getName())
                    .snippet(venuesList.get(i).getCategoryName()));
            venuesList.get(i).setMarker(marker);
        }
        LatLngBounds bounds = builder.build();

        // Zoom Fudge factor for single venue height layout
        if(venuesList.size()==1)
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, w, h/3, 250));
        else
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, w, h, padding));

        marker.showInfoWindow();

        mMap.setOnInfoWindowClickListener(this);

        // Checks for location permissions at runtime (required for API >= 23)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Shows the user's current location
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        // one or multiple venues?
        if(venuesList.size()==1) {
            if (!marker.equals(markerAustin)) { // Dont show info for austin
                // Opens the Foursquare venue page when a user clicks on the info window of the venue
//                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://foursquare.com/v/" + venuesList.get(0).getId()));
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(venuesList.get(0).getVenueURL()));
                startActivity(browserIntent);
            }
        } else {
            // Look thru the venueList for a matching marker
            for (int i = 0; i < venuesList.size(); i++) {
                if (venuesList.get(i).getMarker().equals(marker)) {
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);

                    // Build the one-item venue list
                    ArrayList<Venue> venueResults = new ArrayList<>();
                    venueResults.add(new Venue(
                            venuesList.get(i).getName(),
                            venuesList.get(i).getId(),
                            venuesList.get(i).getCategoryName(),
                            venuesList.get(i).getLatitude(),
                            venuesList.get(i).getLongitude(),
                            venuesList.get(i).getCategoryIconURL(),
                            venuesList.get(i).getVenueURL()));
                    // Passes the crucial venue details onto the map view
                    intent.putExtra("venuesList", venueResults);
                    startActivity(intent);
                }
            }
        }


    }
}
