/**
 * Filename: PlacePickerAdapter.java
 * Author: Matthew Huie
 *
 * PlacePickerAdapter represents the adapter for attaching venue data to the RecyclerView within
 * PlacePickerActivity.  This adapter will handle a list of incoming FoursquareResults and parse them
 * into the view.
 */

package com.realityexpander.alamohunt;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlacePickerAdapter extends RecyclerView.Adapter<PlacePickerAdapter.ViewHolder> {

    // The application context for getting resources
    private Context context;

    // The list of frsResults from the Foursquare API
    private List<FoursquareResults> results;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // The venue fields to display
        TextView name;
        TextView address;
        TextView rating;
        TextView distance;
        ImageView ivCategoryIcon;

        // Venue internal data
        Venue  venueDetails = new Venue("","","");

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);

            // Gets the appropriate view for each venue detail
            name = (TextView)v.findViewById(R.id.placePickerItemName);
            address = (TextView)v.findViewById(R.id.placePickerItemAddress);
            rating = (TextView)v.findViewById(R.id.placePickerItemRating);
            distance = (TextView)v.findViewById(R.id.placePickerItemDistance);
            ivCategoryIcon = (ImageView)v.findViewById(R.id.placePickerCategoryIcon);
        }

        @Override
        public void onClick(View v) {

            // Creates an intent to direct the user to the single venue map view
            Context context = name.getContext();
             Intent i = new Intent(context, MapsActivity.class);

            // Build the one-item venue list
            // Passes the crucial venue details onto the map view
            ArrayList<Venue> venueResults = new ArrayList<>();
            venueResults.add(new Venue(
                        venueDetails.getName(),
                        venueDetails.getId(),
                        venueDetails.getCategoryName(),
                        venueDetails.getLatitude(),
                        venueDetails.getLongitude(),
                        venueDetails.getCategoryIconURL(),
                        venueDetails.getVenueURL() ));
            i.putExtra("venuesList", venueResults);

            // Transitions to the map view.
            context.startActivity(i);
        }
    }

    public PlacePickerAdapter(Context context, List<FoursquareResults> results) {
        this.context = context;
        this.results = results;
    }

    @Override
    public PlacePickerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place_picker, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        // Sets each view with the appropriate venue details
        holder.name.setText(results.get(position).venue.name);
        holder.address.setText(results.get(position).venue.categories.get(0).name); // CDA FIX Change .address to .category

        // Calc distance to center of Austin, TX (30.2672° N, 97.7431° W)
        Location locationAustinCenter = new Location("Austin, TX");
        locationAustinCenter.setLatitude(30.2672); // CDA FIX put into strings
        locationAustinCenter.setLongitude(-97.7431);

        Location locationVenue = new Location(results.get(position).venue.name);
        locationVenue.setLatitude(results.get(position).venue.location.lat);
        locationVenue.setLongitude(results.get(position).venue.location.lng);
        int distance = (int) locationAustinCenter.distanceTo(locationVenue);
        holder.distance.setText(Integer.toString(distance) + "m");

//        // Sets the proper rating colour, referenced from the Foursquare Brand Guide
////        double ratingRaw = frsResults.get(position).venue.rating;
//        double ratingRaw = distance; // Rating based on distance from Austin, TX, further away is more red
//        if (ratingRaw <= 2500.0) {
//            holder.rating.setBackgroundColor(ContextCompat.getColor(context, R.color.FSQKale));
//        } else if (ratingRaw <= 4000.0) {
//            holder.rating.setBackgroundColor(ContextCompat.getColor(context, R.color.FSQGuacamole));
//        } else if (ratingRaw <= 6000.0) {
//            holder.rating.setBackgroundColor(ContextCompat.getColor(context, R.color.FSQLime));
//        } else if (ratingRaw <= 8000.0) {
//            holder.rating.setBackgroundColor(ContextCompat.getColor(context, R.color.FSQBanana));
//        } else if (ratingRaw <= 12000.0) {
//            holder.rating.setBackgroundColor(ContextCompat.getColor(context, R.color.FSQOrange));
//        } else if (ratingRaw <= 16000.0) {
//            holder.rating.setBackgroundColor(ContextCompat.getColor(context, R.color.FSQMacCheese));
//        } else {
//            holder.rating.setBackgroundColor(ContextCompat.getColor(context, R.color.FSQStrawberry));
//        }
//        double rating =  Math.ceil(Math.max(16000-ratingRaw,0.0)/160.0)/10.0; // 0.0 -> 10.0 based on max distance of 16000m
//        holder.rating.setText( Double.toString(rating) ); //String.valueOf((int) rating) );


        // Stores item venue detail fields for the map view from the foursquare results // CDA FIX Do this here?
        holder.venueDetails.setName(results.get(position).venue.name);
        holder.venueDetails.setId(results.get(position).venue.id);
        holder.venueDetails.setLatitude(results.get(position).venue.location.lat);
        holder.venueDetails.setLongitude(results.get(position).venue.location.lng);
        holder.venueDetails.setCategoryName(results.get(position).venue.categories.get(0).name);
        // Load the category icon
        holder.venueDetails.setCategoryIconURL(
                results.get(position).venue.categories.get(0).icon.prefix + "bg_88"
              + results.get(position).venue.categories.get(0).icon.suffix );
        holder.venueDetails.setVenueURL("https://foursquare.com/v/"+results.get(position).venue.id);

        Picasso.with(context)
                .load(holder.venueDetails.getCategoryIconURL())
                .resize(95, 95)
                .into(holder.ivCategoryIcon);


        // *** Get the rating
        // The base URL for the Foursquare API
        String foursquareBaseURL = "https://api.foursquare.com/v2/";

        // The client ID and client secret for authenticating with the Foursquare API
        String foursquareClientID;
        String foursquareClientSecret;

        // Get details for the venueID
        // Builds Retrofit and FoursquareService objects for calling the Foursquare API and parsing with GSON
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(foursquareBaseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        FoursquareService foursquare = retrofit.create(FoursquareService.class);

        // Gets the stored Foursquare API client ID and client secret from XML
        foursquareClientID = context.getResources().getString(R.string.foursquare_client_id);
        foursquareClientSecret = context.getResources().getString(R.string.foursquare_client_secret);

        // Calls the Foursquare API to get venue details
        Call<FoursquareJSON> searchCall = foursquare.searchVenueID(
                holder.venueDetails.getId(),
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

                // Set the rating and text color
                holder.rating.setText(Double.toString(fv.rating));
                holder.rating.setBackgroundColor(Color.parseColor("#"+fv.ratingColor));
            }

            @Override
            public void onFailure(Call<FoursquareJSON> call, Throwable t) {
                Toast.makeText(context, "Can't connect to Foursquare's servers!", Toast.LENGTH_LONG).show();
                //finish();
            }
        });




    }

    @Override
    public int getItemCount() {
        return results.size();
    }
}