package com.example.charlesbai321.myapplication.Activities;

import android.arch.persistence.room.Room;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.charlesbai321.myapplication.Data.MonitoredLocation;
import com.example.charlesbai321.myapplication.Data.MonitoredLocationsDatabase;
import com.example.charlesbai321.myapplication.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class Main2Activity extends AppCompatActivity implements OnMapReadyCallback{

    Place thisplace;
    PlaceAutocompleteFragment placeSuggestion;
    GoogleMap gmap;
    SupportMapFragment mapFragment;
    TextView placetoAdd_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //textbox to display the added place
        placetoAdd_text = findViewById(R.id.placetoadd);
        //autocomplete widget
        placeSuggestion = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.places);

        //on place selected listener for the autocomplete widget
        placeSuggestion.setOnPlaceSelectedListener(new PlaceSelectionListener(){
            @Override
            public void onPlaceSelected(Place place) {
                //TODO: Do something about the place that has been selected
                thisplace = place;

                //move the map to the selected place
                gmap.addMarker(new MarkerOptions().position(place.getLatLng()).
                        title(place.getName().toString()));
                gmap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                placetoAdd_text.setText(thisplace.getName());
            }
            @Override
            public void onError(Status status) {
                //TODO: How does an error even occur???
            }
        });

        //map initialization
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //get it to initialize on another thread (?)
        mapFragment.getMapAsync(this);

    }

    //https://stackoverflow.com/questions/15319431/how-to-convert-a-latlng-and-a-radius-to-a-latlngbounds-in-android-google-maps-ap
    //
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gmap = googleMap;
        //updates autocomplete widget with only relevant suggestions based on map
        gmap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                CameraPosition cp = gmap.getCameraPosition();
                LatLngBounds bounds = gmap.getProjection().getVisibleRegion().latLngBounds;
                placeSuggestion.setBoundsBias(bounds);
            }
        });
    }


    //this is called by clicking on the button, initialized it there as not to clutter activity

    public void addPlace(View view){
        MonitoredLocation monitoredplace = new MonitoredLocation(thisplace.getName().toString(),
                thisplace.getLatLng());
        //we're going to let all this run on the main thread because we want to be sure that
        //it gets added before the application is closed, destroyed, etc
        MonitoredLocationsDatabase db = Room.databaseBuilder(this,
                MonitoredLocationsDatabase.class, MonitoredLocation.DATABASE_KEY)
                .allowMainThreadQueries().build();
        //TODO: check if there is going to be any discrepancy between database and
        //TODO: the static temporary list created on the MainActivityThread
        MainActivity.places.add(monitoredplace);
        db.monitoredLocationDao().insertAll(monitoredplace);
        finish();
    }
}
