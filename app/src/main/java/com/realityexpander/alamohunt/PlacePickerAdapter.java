/**
 * Filename: PlacePickerAdapter.java
 * Author: Chris Athanas
 *
 * PlacePickerAdapter represents the adapter for attaching venue data to the RecyclerView within
 * PlacePickerActivity.  This adapter will handle a list of incoming FoursquareResults and parse them
 * into the view.
 */

package com.realityexpander.alamohunt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PlacePickerAdapter extends RecyclerView.Adapter<PlacePickerAdapter.ViewHolder> {

    // The application context for getting resources
    private Context context;

    // The list of frsResults from the Foursquare API
    private List<FoursquareResults> results;

    // Favorite venue ID's from shared preferences
    ArrayList<String> favoriteVenueIDs;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // The venue fields to display
        TextView name;
        TextView category;
        TextView rating;
        TextView distance;
        ImageView ivCategoryIcon;
        ImageView ivFavorite;

        // Venue internal data
        Venue  venueDetails = new Venue();

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);

            // Gets the appropriate view for each venue detail
            name = (TextView)v.findViewById(R.id.placePickerItemName);
            category = (TextView)v.findViewById(R.id.placePickerItemAddress);
            rating = (TextView)v.findViewById(R.id.placePickerItemRating);
            distance = (TextView)v.findViewById(R.id.placePickerItemDistance);
            ivCategoryIcon = (ImageView)v.findViewById(R.id.placePickerCategoryIcon);
            ivFavorite = (ImageView)v.findViewById(R.id.placePickerFavorite);
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

        favoriteVenueIDs = LoadPrefs("favoriteVenueIDs");
        if (favoriteVenueIDs == null)
            favoriteVenueIDs = new ArrayList<>();
    }

    @Override
    public PlacePickerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place_picker, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        FoursquareVenue fv = results.get(position).venue;

        // Sets each view with the appropriate venue details
        holder.name.setText(fv.name);
        if (fv.categories.get(0) != null )
            holder.category.setText(fv.categories.get(0).name);

        // Calc distance to center of Austin, TX (30.2672° N, 97.7431° W)
        Location locationAustinCenter = new Location("Austin, TX");
        locationAustinCenter.setLatitude(30.2672); // CDA FIX put into strings
        locationAustinCenter.setLongitude(-97.7431);

        Location locationVenue = new Location(fv.name);
        locationVenue.setLatitude(fv.location.lat);
        locationVenue.setLongitude(fv.location.lng);
        int distance = (int) locationAustinCenter.distanceTo(locationVenue);
        holder.distance.setText(Integer.toString(distance) + "m");

        // Stores item venue detail fields for the map view from the foursquare results // CDA FIX refactor?
        holder.venueDetails.setName(fv.name);
        holder.venueDetails.setId(fv.id);
        holder.venueDetails.setLatitude(fv.location.lat);
        holder.venueDetails.setLongitude(fv.location.lng);
        holder.venueDetails.setCategoryName(fv.categories.get(0).name);

        // Load the category icon
        holder.venueDetails.setCategoryIconURL(
                fv.categories.get(0).icon.prefix + "bg_88"
              + fv.categories.get(0).icon.suffix );
        holder.venueDetails.setVenueURL("https://foursquare.com/v/"+fv.id); // CDA FIX refactor?

        // Set favorited
        holder.ivFavorite.setVisibility(View.INVISIBLE); // defaults to invisible
        if ( favoriteVenueIDs != null) {
            for (int i = 0; i < favoriteVenueIDs.size(); i++)
                if (favoriteVenueIDs.get(i).equals(fv.id)) {
                    holder.ivFavorite.setVisibility(View.VISIBLE);
                    break;
                }
        }

        // Category icon
        Picasso.with(context)
                .load(holder.venueDetails.getCategoryIconURL())
                .resize(95, 95)
                .into(holder.ivCategoryIcon);

        // Set the rating and text background color to match
        if( fv.rating > 0) {
            holder.rating.setVisibility(View.VISIBLE);
            holder.rating.setText(Double.toString(fv.rating));
            if (fv.ratingColor != null)
                holder.rating.setBackgroundColor(Color.parseColor("#" + fv.ratingColor));
        } else
            holder.rating.setVisibility(View.INVISIBLE);

    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public ArrayList<String> LoadPrefs( String key ) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);
    }
}