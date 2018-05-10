package com.realityexpander.alamohunt;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_ACCESS_FINE_LOCATION = 1;

    public void searchButtonClick(View view) {
        Intent i = new Intent(getApplicationContext(), PlacePickerActivity.class);
//        Intent i = new Intent(getApplicationContext(), MapsActivity.class);

        String searchString = ((AutoCompleteTextView)findViewById(R.id.search)).getText().toString();
        i.putExtra( "_search", searchString );
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                searchTextView.setText(venueName.getName());
                searchTextView.setSelection(searchTextView.getText().length()); // End point Cursor
            }
        });

        searchTextView.setOnDismissListener(new AutoCompleteTextView.OnDismissListener() {
            @Override
            public void onDismiss() {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), 0);
            }
        });


    }
}
