/**
 * Filename: AutocompleteAdapter.java
 * Author: Chris Athanas
 *
 * AutocompleteAdapter represents the adapter for attaching venue data to the ArrayAdapter within
 * MainActivity.  This adapter will handle a list of suggested venues from FoursquareResults and parse them
 * into the view.
 */

package com.realityexpander.alamohunt;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AutocompleteAdapter extends ArrayAdapter<Venue> {
    private ArrayList<Venue> mVenues;
    private Context mContext;
    private int mResource; // The custom arrayAdapter view for each suggested venue

    // RETROFIT
    // The base URL for the Foursquare API
    private String foursquareBaseURL;
    // The client ID and client secret for authenticating with the Foursquare API
    private String foursquareClientID;
    private String foursquareClientSecret;

    public AutocompleteAdapter(Context context, int resource) {
        super(context, resource);
        mContext = context;
        mVenues = new ArrayList<>();
        mResource = resource;

        foursquareClientID = mContext.getResources().getString(R.string.foursquare_client_id);
        foursquareClientSecret = mContext.getResources().getString(R.string.foursquare_client_secret);
        foursquareBaseURL = mContext.getResources().getString(R.string.foursquare_base_URL);
    }

    @Override
    public int getCount() {
        if (mVenues == null)
            return 0;

        return mVenues.size();
    }

    @Override
    public Venue getItem(int position) {
        return mVenues.get(position);
    }

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    String term = constraint.toString();
                    if(mVenues!=null)
                        mVenues.clear();
                    try {
                        //get suggestions from the foursquare api
                        // (Done synchronously here, we are already off the UI thread)
                        mVenues = findVenues(term);
                    } catch (Exception e) {
                        Log.d("DEADBEEF", "EXCEPTION " + e);
                    }
                    filterResults.values = mVenues;
                    if(mVenues != null)
                        filterResults.count = mVenues.size();
                    else
                        filterResults.count = 0;
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };

        return myFilter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource, parent, false);
        }

        ((TextView) convertView.findViewById(R.id.placeName)).setText(getItem(position).getName());
        ((TextView) convertView.findViewById(R.id.categoryName)).setText(getItem(position).getCategoryName());

        return convertView;
    }


    private ArrayList<Venue> findVenues(String searchString) {

        final ArrayList<Venue> suggestList = new ArrayList<>();

        try {

            // Builds Retrofit and FoursquareService objects for calling the Foursquare API and parsing with GSON
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(foursquareBaseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            FoursquareService foursquare = retrofit.create(FoursquareService.class);

            Call<FoursquareJSON> autoCompleteCall = foursquare.searchAutoComplete(
                    foursquareClientID,
                    foursquareClientSecret,
                    URLEncoder.encode(searchString, "UTF-8"));

            // Synchronous call here to allow menu to update properly, this is in its own thread.
            Response<FoursquareJSON> response = autoCompleteCall.execute();

            // Gets the venue objects from the JSON response
            FoursquareJSON fjson = response.body();

            if(response.body() == null) {
                try {
                    // Get the error message from Foursquare server
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(mContext, ((JSONObject)jObjError.get("meta")).getString("errorDetail"), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                return null;
            }

            FoursquareResponse fr = fjson.response;
            FoursquareGroup fg = fr.group;
            List<FoursquareResults> frs = fg.results;

            Venue theVenue;
            for (int i = 0; i < frs.size(); i++) {
                String category;
                // Store the venues to the suggestion list
                if(frs.get(i).venue.categories.size() == 0) // category may be empty for some venues
                    category = "N/A";
                else
                    category = frs.get(i).venue.categories.get(0).name;
                theVenue = new Venue(frs.get(i).venue.name,
                                     frs.get(i).venue.id,
                                     category
                );
                suggestList.add(theVenue);
            }


        } catch (Exception e) {
            Log.d("DEADBEEF", "EXCEPTION " + e);
            Toast.makeText(mContext, "Can't connect to Foursquare's servers!", Toast.LENGTH_LONG).show();
            return null;
        }

        return suggestList;
    }
}
