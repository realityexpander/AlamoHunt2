/**
 * Filename: PlacePickerActivity.java
 * Author: Chris Athanas
 *
 * PlacePickerActivity represents a list view of venues of interest and a FAB
 *
 * - Shows basic venue information like category, rating, rating color, category icon,
 *   distance from Austin city center.
 * - If click on a single list item, show the venue details.
 * - If click FAB will show the map of all venues.
 */

package com.realityexpander.alamohunt;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class PlacePickerActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // The client object for connecting to the Google API
    private GoogleApiClient mGoogleApiClient;

    // The TextView for displaying the current location
    private ProgressBar spinner;

    // The RecyclerView and associated objects for displaying the nearby coffee spots
    private RecyclerView placePicker;
    private LinearLayoutManager placePickerManager;
    private RecyclerView.Adapter placePickerAdapter;

    // The base URL for the Foursquare API
    private String foursquareBaseURL;
    // The client ID and client secret for authenticating with the Foursquare API
    private String foursquareClientID;
    private String foursquareClientSecret;

    // Users search string
    private String searchString;

    // The list of frsResults from the Foursquare API
    private ArrayList<FoursquareResults> frsResults;
    private ArrayList<Venue> venueResults;

    private static int FIRST_CATEGORY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_picker);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            searchString = extras.getString("_search");
        }

        // The visible TextView and RecyclerView objects
        placePicker = (RecyclerView)findViewById(R.id.placePickerList);
        // Sets the dimensions, LayoutManager, and dividers for the RecyclerView
        placePicker.setHasFixedSize(true);
        placePickerManager = new LinearLayoutManager(this);
        placePicker.setLayoutManager(placePickerManager);
        placePicker.addItemDecoration(new DividerItemDecoration(placePicker.getContext(), placePickerManager.getOrientation()));


        // Get saved instance data for orientation change
        if (savedInstanceState != null && savedInstanceState.containsKey("frsResults")) {
            frsResults = (ArrayList<FoursquareResults>)savedInstanceState.getSerializable("frsResults");
        }

        // Setup the toolbar UI elements
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(android.support.design.R.drawable.abc_ic_ab_back_material);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        spinner = (ProgressBar)findViewById(R.id.progressBar1);


        // The FAB shows all the venues for the MapsActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(frsResults == null) { // User clicked FAB too fast, should prolly just ignore it, but we will give a message
                        Snackbar.make(view, "One moment... data is loading", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                    return;
                }

                // Creates intent to direct the user to a multi-venue map view
                Context context = getApplicationContext();
                Intent i = new Intent(context, MapsActivity.class);

                // Build the list of venues from the foursquare response
                venueResults = new ArrayList<>();
                for(int n = 0; n< frsResults.size(); n++) {
                    String category;
                    String categoryIconURL;

                    // Check for missing category
                    if (frsResults.get(n).venue.categories.size() == 0) {
                        category = "";
                        categoryIconURL = null;
                    } else {
                        category = frsResults.get(n).venue.categories.get(FIRST_CATEGORY).name;
                        // Build the category icon url string
                        categoryIconURL = frsResults.get(n).venue.categories.get(FIRST_CATEGORY).icon.prefix
                                + getString(R.string.FoursquareIconTypeAndSize)
                                + frsResults.get(n).venue.categories.get(FIRST_CATEGORY).icon.suffix;
                    }

                    venueResults.add(new Venue( frsResults.get(n).venue.name,
                                                frsResults.get(n).venue.id,
                                                category,
                                                frsResults.get(n).venue.location.lat,
                                                frsResults.get(n).venue.location.lng,
                                                categoryIconURL,
                                                getString(R.string.FoursquareIconURLPrefix)+frsResults.get(n).venue.id
                    ) );
                }
                // Passes the crucial venue details onto the map view
                i.putExtra("venuesList", venueResults);
                i.putExtra("_search", searchString);

                // Transitions to the map view.
                context.startActivity(i);
            }
        });


        // Creates a connection to the Google API for location services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Gets the stored Foursquare API client ID and client secret from XML
        foursquareBaseURL = getResources().getString(R.string.foursquare_base_URL);
        foursquareClientID = getResources().getString(R.string.foursquare_client_id);
        foursquareClientSecret = getResources().getString(R.string.foursquare_client_secret);
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Search: " + searchString);

        // Already loaded data from the Foursquare? (Orientation change/nav back from prev activity)
        if (frsResults != null) {
            spinner.setVisibility(View.GONE);

            // Save the scroll position
            boolean resetScroll = false;
            int firstItem = 0;
            float topOffset = 0;
            LinearLayoutManager manager=null;
            if (placePicker != null) {
                manager = (LinearLayoutManager) placePicker.getLayoutManager();
                firstItem = manager.findFirstVisibleItemPosition();
                View firstItemView = manager.findViewByPosition(firstItem);
                if(firstItemView!=null) {
                    topOffset = firstItemView.getTop();
                    resetScroll = true;
                }
            }
            placePickerAdapter = new PlacePickerAdapter(getApplicationContext(), frsResults);
            placePicker.setAdapter(placePickerAdapter);

            // Reset the scroll after updating the placePickerAdapter
            if (resetScroll)
                manager.scrollToPositionWithOffset(firstItem, (int) topOffset);

            return;
        }

        // Checks for location permissions at runtime (required for API >= 23)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Makes a Google API request for the user's last known location
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation != null ) {

                // ***
                // SEARCH FOR VENUES
                // Find venues matching the search criteria
                // Builds Retrofit and FoursquareService objects for calling the Foursquare API and parsing with GSON
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(foursquareBaseURL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                final FoursquareService foursquare = retrofit.create(FoursquareService.class);

                // Calls the Foursquare API to explore nearby places
                Call<FoursquareJSON> searchCall = foursquare.searchForPlace(
                        foursquareClientID,
                        foursquareClientSecret,
                        // userLL,
                        // userLLAcc, // CDA todo if we want to make it more location based
                        searchString);
                searchCall.enqueue(new Callback<FoursquareJSON>() {
                    @Override
                    public void onResponse(Call<FoursquareJSON> call, Response<FoursquareJSON> response) {

                        if(response.body() == null) {
                            try {
                                JSONObject jObjError = new JSONObject(response.errorBody().string());
                                Toast.makeText(getApplicationContext(), ((JSONObject)jObjError
                                                                        .get("meta"))
                                                                        .getString("errorDetail"), Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Log.d("DEADBEEF", "EXCEPTION " + e);
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                            finish();
                            return;
                        }

                        // ***
                        // GET THE FOURSQUARE RESPONSE
                        // Gets the venue object from the JSON response
                        FoursquareJSON fjson = response.body();
                        FoursquareResponse fr = fjson.response;
                        FoursquareGroup fg = fr.group;
                        final ArrayList<FoursquareResults> frs = (ArrayList<FoursquareResults>)fg.results;

                        // Displays the frsResults in the RecyclerView
                        placePickerAdapter = new PlacePickerAdapter(getApplicationContext(), frs);
                        placePicker.setAdapter(placePickerAdapter);

                        frsResults = frs;

                        spinner.setVisibility(View.GONE);

                        // ***
                        // GET THE RATINGS FOR EACH VENUE
                        // Go thru each of the venues and get the ratings with another call to /venues/VENUE_ID
                        // for venues[0..n], Get the rating & rating color.
                        // Fill in frs.rating with the rating from /venues/VENUE_ID endpoint
                        for( int i=0; i < frs.size(); i++) {

                            try {
                                // Calls the Foursquare API to get venue details
                                Call<FoursquareJSON> searchCall2 = foursquare.searchVenueID(
                                        frs.get(i).venue.id,
                                        foursquareClientID,
                                        foursquareClientSecret
                                );
                                searchCall2.enqueue(new Callback<FoursquareJSON>() {
                                    @Override
                                    public void onResponse(Call<FoursquareJSON> call, Response<FoursquareJSON> response) {

                                        // No response body? Prolly cuz quota exceeded...
                                        if(response.body() == null) {
                                            try {
                                                JSONObject jObjError = new JSONObject(response.errorBody().string());
                                                Toast.makeText(getApplicationContext(), ((JSONObject)jObjError
                                                        .get("meta"))
                                                        .getString("errorDetail"), Toast.LENGTH_LONG).show();
                                            } catch (Exception e) {
                                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                            finish();
                                            return;
                                        }

                                        // Gets the single venue object from the JSON response
                                        FoursquareJSON fjson2 = response.body();
                                        FoursquareResponse fr = fjson2.response;
                                        FoursquareVenue fv = fr.venue;

                                        // Get the rating and rating text color from Venue Info call & update the frs variable & PickerListAdapter
                                        if (fv.ratingColor != null) {
                                            // search the list for the matching ID
                                            for(int n=0; n<frs.size(); n++) {
                                                if (frs.get(n).venue.id.equals(fv.id)
                                                        &&  frs.get(n).venue.rating != fv.rating)  { // if rating is already set, no need to update the Recyclerview
                                                    frs.get(n).venue.rating = fv.rating;
                                                    if (fv.ratingColor != null)
                                                        frs.get(n).venue.ratingColor = fv.ratingColor;

                                                    // Displays the frsResults in the RecyclerView
                                                    placePickerAdapter = new PlacePickerAdapter(getApplicationContext(), frs);
                                                    placePicker.setAdapter(placePickerAdapter);
                                                    frsResults = frs;
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<FoursquareJSON> call, Throwable t) {
                                        Toast.makeText(getApplicationContext(), "Can't connect to Foursquare's servers!", Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                });

                            } catch (Exception e) {
                                Log.d("DEADBEEF", "EXCEPTION " + e);
                                Toast.makeText(getApplicationContext(), "Can't connect to Foursquare's servers!", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<FoursquareJSON> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), "Can't connect to Foursquare's servers!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "Can't determine your current location!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reconnects to the Google API
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Disconnects from the Google API
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save already loaded database records to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putSerializable("frsResults", frsResults);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.

        frsResults = (ArrayList<FoursquareResults>)savedInstanceState.getSerializable("frsResults");
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "Can't connect to Google's servers!", Toast.LENGTH_LONG).show();
        finish();
    }

}


