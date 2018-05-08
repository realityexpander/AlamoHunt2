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
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

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
        String id;
        double latitude;
        double longitude;
        String categoryIconURL;

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

            // Passes the crucial venue details onto the map view
            i.putExtra("name", name.getText());
            i.putExtra("ID", id);
            i.putExtra("latitude", latitude);
            i.putExtra("longitude", longitude);
            i.putExtra("categoryIconURL", categoryIconURL);

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
    public void onBindViewHolder(ViewHolder holder, int position) {

        // Sets each view with the appropriate venue details
        holder.name.setText(results.get(position).venue.name);
//        holder.address.setText(frsResults.get(position).venue.location.address);
        holder.address.setText(results.get(position).venue.categories.get(0).name);
//        holder.distance.setText(Integer.toString(frsResults.get(position).venue.location.distance) + "m");

        // Calc distance to center of Austin, TX (30.2672° N, 97.7431° W)
        Location locationAustinCenter = new Location("Austin, TX");
        locationAustinCenter.setLatitude(30.2672); // CDA FIX put into strings
        locationAustinCenter.setLongitude(-97.7431);

        Location locationVenue = new Location(results.get(position).venue.name);
        locationVenue.setLatitude(results.get(position).venue.location.lat);
        locationVenue.setLongitude(results.get(position).venue.location.lng);
        int distance = (int) locationAustinCenter.distanceTo(locationVenue);
        holder.distance.setText(Integer.toString(distance) + "m");

        // Sets the proper rating colour, referenced from the Foursquare Brand Guide
//        double ratingRaw = frsResults.get(position).venue.rating;
        double ratingRaw = distance; // Rating based on distance from Austin, TX, further away is more red
        if (ratingRaw <= 2500.0) {
            holder.rating.setBackgroundColor(ContextCompat.getColor(context, R.color.FSQKale));
        } else if (ratingRaw <= 4000.0) {
            holder.rating.setBackgroundColor(ContextCompat.getColor(context, R.color.FSQGuacamole));
        } else if (ratingRaw <= 6000.0) {
            holder.rating.setBackgroundColor(ContextCompat.getColor(context, R.color.FSQLime));
        } else if (ratingRaw <= 8000.0) {
            holder.rating.setBackgroundColor(ContextCompat.getColor(context, R.color.FSQBanana));
        } else if (ratingRaw <= 12000.0) {
            holder.rating.setBackgroundColor(ContextCompat.getColor(context, R.color.FSQOrange));
        } else if (ratingRaw <= 16000.0) {
            holder.rating.setBackgroundColor(ContextCompat.getColor(context, R.color.FSQMacCheese));
        } else {
            holder.rating.setBackgroundColor(ContextCompat.getColor(context, R.color.FSQStrawberry));
        }
        double rating =  Math.ceil(Math.max(16000-ratingRaw,0.0)/160.0)/10.0; // 0.0 -> 10.0 based on max distance of 16000m
        holder.rating.setText( Double.toString(rating) ); //String.valueOf((int) rating) );


        // Stores additional venue details for the map view
        holder.id = results.get(position).venue.id;
        holder.latitude = results.get(position).venue.location.lat;
        holder.longitude = results.get(position).venue.location.lng;

        // Load the category icon  CDA FIX
       holder.categoryIconURL = results.get(position).venue.categories.get(0).icon.prefix + "bg_88" +
                results.get(position).venue.categories.get(0).icon.suffix;
        // Picasso.with(context).load(holder.categoryIconURL).into(holder.ivCategoryIcon);
        Picasso.with(context)
                .load(holder.categoryIconURL)
                .resize(95, 95)
                .into(holder.ivCategoryIcon);

    }

    @Override
    public int getItemCount() {
        return results.size();
    }
}