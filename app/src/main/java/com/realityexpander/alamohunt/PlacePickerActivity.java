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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class PlacePickerActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // The client object for connecting to the Google API
    private GoogleApiClient mGoogleApiClient;

    // The TextView for displaying the current location
    private TextView snapToPlace;

    // The RecyclerView and associated objects for displaying the nearby coffee spots
    private RecyclerView placePicker;
    private LinearLayoutManager placePickerManager;
    private RecyclerView.Adapter placePickerAdapter;

    // The base URL for the Foursquare API
    private String foursquareBaseURL = "https://api.foursquare.com/v2/";

    // The client ID and client secret for authenticating with the Foursquare API
    private String foursquareClientID;
    private String foursquareClientSecret;
    private String searchString;

    // The list of frsResults from the Foursquare API
    List<FoursquareResults> frsResults;
    private ArrayList<Venue> venueResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_picker);

        // Setup the toolbar UI elements
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Searching...");
        // CDA FIX - ADD Twirling wait progress indicator
        toolbar.setNavigationIcon(android.support.design.R.drawable.abc_ic_ab_back_material);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
//                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });

        // The FAB shows all the venues on a map
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(frsResults == null) {
                        Snackbar.make(view, "One moment... data is loading", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                    return;
                }

                // Creates an intent to direct the user to a multi-venue map view
                Context context = getApplicationContext();
                Intent i = new Intent(context, MapsActivity.class);

                // Build the list of venues from the foursquare response
                venueResults = new ArrayList<>();
                for(int n = 0; n< frsResults.size(); n++) {
                    venueResults.add(new Venue( frsResults.get(n).venue.name,
                                                frsResults.get(n).venue.id,
                                                frsResults.get(n).venue.categories.get(0).name,
                                                frsResults.get(n).venue.location.lat,
                                                frsResults.get(n).venue.location.lng,
                                                frsResults.get(n).venue.categories.get(0).icon.prefix
                                              + "bg_88" // CDA FIX - Make constant FOURSQUARE_ICON_SIZE
                                              + frsResults.get(n).venue.categories.get(0).icon.suffix,
                                      "https://foursquare.com/v/"+frsResults.get(n).venue.id
                    ) );
                }
                // Passes the crucial venue details onto the map view
                i.putExtra("venuesList", venueResults);

                // Transitions to the map view.
                context.startActivity(i);
            }
        });

        // Get the search from prev screen
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            searchString = extras.getString("_search");
        }

        // The visible TextView and RecyclerView objects
//        snapToPlace = (TextView)findViewById(R.id.snapToPlace);
        placePicker = (RecyclerView)findViewById(R.id.placePickerList); // CDA FIX change name

        // Sets the dimensions, LayoutManager, and dividers for the RecyclerView
        placePicker.setHasFixedSize(true);
        placePickerManager = new LinearLayoutManager(this);
        placePicker.setLayoutManager(placePickerManager);
        placePicker.addItemDecoration(new DividerItemDecoration(placePicker.getContext(), placePickerManager.getOrientation()));

        // Creates a connection to the Google API for location services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Gets the stored Foursquare API client ID and client secret from XML
        foursquareClientID = getResources().getString(R.string.foursquare_client_id);
        foursquareClientSecret = getResources().getString(R.string.foursquare_client_secret);
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        // Checks for location permissions at runtime (required for API >= 23)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Makes a Google API request for the user's last known location
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {

                Toolbar toolbar = findViewById(R.id.toolbar);
                toolbar.setTitle("Search: " + searchString);
//                setSupportActionBar(toolbar);
//                snapToPlace.setText("Here's some "+ searchString +" nearby in Austin, TX");

                // Builds Retrofit and FoursquareService objects for calling the Foursquare API and parsing with GSON
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(foursquareBaseURL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                FoursquareService foursquare = retrofit.create(FoursquareService.class);

                // Calls the Foursquare API to explore nearby places
                Call<FoursquareJSON> searchCall = foursquare.searchForPlace(
                        foursquareClientID,
                        foursquareClientSecret,
                        // userLL,
                        // userLLAcc,
                        searchString);
                searchCall.enqueue(new Callback<FoursquareJSON>() {
                    @Override
                    public void onResponse(Call<FoursquareJSON> call, Response<FoursquareJSON> response) {

                        // Gets the venue object from the JSON response
                        FoursquareJSON fjson = response.body();
                        FoursquareResponse fr = fjson.response;
                        FoursquareGroup fg = fr.group;
                        List<FoursquareResults> frs = fg.results;

                        // Displays the frsResults in the RecyclerView
                        placePickerAdapter = new PlacePickerAdapter(getApplicationContext(), frs);
                        placePicker.setAdapter(placePickerAdapter);

                        frsResults = frs;
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
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "Can't connect to Google's servers!", Toast.LENGTH_LONG).show();
        finish();
    }

}


