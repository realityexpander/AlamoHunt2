/**
 * Filename: MainActivity.java
 * Author: Chris Athanas
 *
 * MainActivity handles:
 *  - Autocomplete search
 *  - Logo Animation
 */

package com.realityexpander.alamohunt;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_ACCESS_FINE_LOCATION = 1;

    public void searchButtonClick(View view) {
        Intent i = new Intent(getApplicationContext(), PlacePickerActivity.class);
        String searchString = ((AutoCompleteTextView)findViewById(R.id.search)).getText().toString();
        i.putExtra( "_search", searchString );
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // CDA Leave this in case we want to make it location based in next revision
        // Requests for location permissions at runtime (required for API >= 23)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        }

        // setup the autocomplete search edit text
        final CustomBackAutoCompleteTextView searchTextView = (CustomBackAutoCompleteTextView) findViewById(R.id.search);
        AutocompleteAdapter autoCompleteAdapter = new AutocompleteAdapter(this, R.layout.auto_complete_layout);
        searchTextView.setThreshold(3);
        searchTextView.setAdapter(autoCompleteAdapter);

        //when autocomplete search result is clicked
        searchTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Venue venueName = (Venue) parent.getItemAtPosition(position);

                // Fill in the Search text with the item just clicked
                searchTextView.setText(venueName.getName());
                searchTextView.setSelection(searchTextView.getText().length()); // Put edit text cursor at end of the line

                // Start the search
                Intent i = new Intent(getApplicationContext(), PlacePickerActivity.class);
                String searchString = venueName.getName();
                i.putExtra( "_search", searchString );
                startActivity(i);
            }
        });

        // When keyboard clicks enter or search button
        searchTextView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchTextView.setOnEditorActionListener(new AutoCompleteTextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Intent i = new Intent(getApplicationContext(), PlacePickerActivity.class);
                    String searchString = ((AutoCompleteTextView)findViewById(R.id.search)).getText().toString();
                    i.putExtra( "_search", searchString );
                    startActivity(i);
                    return true;
                }
                return false;
            }
        });
    }


}
