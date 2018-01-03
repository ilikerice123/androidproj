package com.example.charlesbai321.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by charlesbai321 on 23/12/17.
 * A Service is an application component representing either an application's desire to
 * perform a longer-running operation while not interacting with the user or to supply
 * functionality for other applications to use. Each service class must have a corresponding
 * <service> declaration in its package's AndroidManifest.xml. Services can be started with Context.startService() and Context.bindService().
 */

public class GPSTracker {
    private Context mContext;
    private Activity mActivity;

    public static final int REQUEST_LOCATION = 3; //I have no idea why this is this
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback mLCallback;
    private Location location;
    private LocationRequest lr;
    private double latitude;
    private double longitude;

    public GPSTracker(Activity a, Context c){
        mContext = c;
        mActivity = a;
        if(!hasPermission(c)){
            //noPermissionsAlert(mContext);
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity);
        initializeLocationRequest();

        //create a call back that sets the location parameter whenever a new one is found
        mLCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                for(Location l : locationResult.getLocations()){
                    //TODO: Do something with the list of locations returned
                    //CHECK if the new location is better than the current one
                    location = l;
                }
            }
        };

        //we have initialized a fused location client and now it's time to see if the location
        //requests are satisfied

        //add the location request that was initialized in the constructor
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(lr);
        //create a client and get the response
        SettingsClient client = LocationServices.getSettingsClient(mActivity);

        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        //this is what happens when all the requirements are met.
        task.addOnSuccessListener(mActivity, new OnSuccessListener<LocationSettingsResponse>(){
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse){
                try {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(mActivity,
                            new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location l) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (l != null) {
                                        // Logic to handle location object
                                        location = l;
                                    }
                                }
                            });
                }
                catch(SecurityException e){
                    //noPermissionsAlert(mContext);
                }
            }
        });

        //this is what happens when locaiton requests cannot be met
        task.addOnFailureListener(mActivity, new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e){
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(mActivity,
                                REQUEST_LOCATION);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });

    }

    private void initializeLocationRequest(){
        lr = new LocationRequest();
        lr.setInterval(10000);
        lr.setFastestInterval(5000);
        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startLocationUpdates(){
        if(hasPermission(mContext) && fusedLocationClient != null){
            try {
                fusedLocationClient.requestLocationUpdates(lr, mLCallback, null);
            }
            catch(SecurityException e){
                //this should never happen because i'm checking for permissions right before
            }
            return;
        }
    }

    public void pauseLocationUpdates(){
        if(fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(mLCallback);
        }
    }

    public Location getLocation(){
        if(hasPermission(mContext)){
            return location;
        }
        else {
            //noPermissionsAlert(mContext);
        }
        return null;
    }

    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public static void noPermissionsAlert(Context c){
        Toast.makeText(c, "No Location Permissions", Toast.LENGTH_SHORT).show();
    }


    public static boolean hasPermission(Context c){
        return ContextCompat.checkSelfPermission(c,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


}
