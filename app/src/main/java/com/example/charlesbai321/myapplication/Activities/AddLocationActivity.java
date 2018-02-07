package com.example.charlesbai321.myapplication.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.charlesbai321.myapplication.Data.MonitoredLocation;
import com.example.charlesbai321.myapplication.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class AddLocationActivity extends AppCompatActivity implements OnMapReadyCallback,
        OnSuccessListener<Location>{

    Place thisplace;
    PlaceAutocompleteFragment placeSuggestion;
    GoogleMap gmap;
    SupportMapFragment mapFragment;
    TextView placetoAdd_text;

    /**
     * OnCreate() initializes all the different UI elements, including the place autocomplete
     * fragment and the corresponding onClick callback, the small CardView-wrapped TextView,
     * as well as initialization for the map object.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addlocation);

        placetoAdd_text = findViewById(R.id.placetoadd);
        placeSuggestion = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.places);

        //on place selected listener for the autocomplete widget
        placeSuggestion.setOnPlaceSelectedListener(new PlaceSelectionListener(){
            @Override
            public void onPlaceSelected(Place place) {
                thisplace = place;
                //add a marker and move the camera to the selected place
                gmap.addMarker(new MarkerOptions().position(place.getLatLng()).
                        title(place.getName().toString()));
                gmap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                placetoAdd_text.setText(thisplace.getName());
            }
            @Override
            public void onError(Status status) {
                Toast.makeText(getApplicationContext(), "Something wrong has " +
                        "happened! Restart the app", Toast.LENGTH_SHORT).show();
            }
        });

        //map initialization
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * This activity implements OnMapReady, so we implement this callback as a function
     * of the activity. In this function, we obtain a fusedlocationclient to return us a
     * location and once we get that location, we center the map around it. In addition,
     * setOnCameraIdleListener() makes it so when the map is moved around, the autocomplete
     * widget results reflect the map location
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gmap = googleMap;
        if(ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){

            FusedLocationProviderClient client =
                    LocationServices.getFusedLocationProviderClient(this);
            client.getLastLocation().addOnSuccessListener(this);
        }
        //updates autocomplete widget with only relevant suggestions based on map
        gmap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                LatLngBounds bounds = gmap.getProjection().getVisibleRegion().latLngBounds;
                placeSuggestion.setBoundsBias(bounds);
            }
        });
    }

    /**
     * Function for the addPlace button. When clicked, it checks if the user has selected a
     * location. If he has, then we create a MonitoredLocation object around that place,
     * feed it into our Room Database to generate a unique private ID, and reload the
     * dependent places (list) in the MainActivity. We don't have to refresh any of the other
     * data structures in MainActivity because that is done in MainActivity.onResume()
     *
     * If the user hasn't selected a place, then just return to main screen with finish()
     * @param view
     */
    public void addPlace(View view){
        if(thisplace != null) {
            MonitoredLocation monitoredplace = new MonitoredLocation(thisplace.getName().toString(),
                    thisplace.getName().toString(), thisplace.getLatLng());

            if(MainActivity.db != null) {
                MainActivity.db.monitoredLocationDao().insertAll(monitoredplace);

                MainActivity.places = MainActivity.db.monitoredLocationDao().getListOfLocations();
                Toast.makeText(this, "Add successful", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(this, "Error, restart app and try again",
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    /**
     * after successful obtaining of location, centers map around the location, at a bigger zoom
     * @param location
     */
    @Override
    public void onSuccess(Location location) {
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(14);
        gmap.moveCamera(zoom);
        gmap.moveCamera(CameraUpdateFactory.newLatLng(
                new LatLng(location.getLatitude(), location.getLongitude())));
    }
}
