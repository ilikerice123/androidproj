package com.example.charlesbai321.myapplication.Util;

import android.Manifest;
import android.app.Activity;
import android.app.IntentService;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.charlesbai321.myapplication.Activities.MainActivity;
import com.example.charlesbai321.myapplication.Data.MonitoredLocation;
import com.example.charlesbai321.myapplication.Data.MonitoredLocationDao;
import com.example.charlesbai321.myapplication.Data.MonitoredLocationsDatabase;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

/**
 * Created by charlesbai321 on 27/01/18.
 */

public class ParseLocationService extends IntentService {

    MonitoredLocationsDatabase db;

    public ParseLocationService(){
        super("ParseLocationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(MainActivity.LOG, "executed :D");
        //Do nothing if we don't have permission
        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            db = Room.databaseBuilder(this,
                    MonitoredLocationsDatabase.class, MonitoredLocation.DATABASE_KEY)
                    .allowMainThreadQueries().build();

            FusedLocationProviderClient client =
                    LocationServices.getFusedLocationProviderClient(getApplicationContext());
            client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                handleLocation(location);
                            }
                        }
                    });
        }
    }

    //this runs in O(n^2) time, but idk if I can make this any better
    private void handleLocation(Location l){
        List<MonitoredLocation> places = db.monitoredLocationDao().getListOfLocations();

        double shortestDistance = Double.MAX_VALUE;
        MonitoredLocation closestLocation = null;

        //by the end of this for loop, closest location will hold the monitoredlocation that
        //is closest to the current location, or null if no monitoredlocations are within
        // 75 meters of current location
        for(MonitoredLocation ml : places){
            //create location object for monitoredlocation
            Location placeLocation = new Location("");
            placeLocation.setLatitude(ml.latitude);
            placeLocation.setLongitude(ml.longitude);

            //checks distance from current location to the monitoredlocation
            double place_distance = placeLocation.distanceTo(l);

            //if it's less than 75 meters, then remember it as the shortest distance
            if(place_distance < 75) { //75 meters is quite a bit lol
                if(placeLocation.distanceTo(l) < shortestDistance){
                    closestLocation = ml;
                    shortestDistance = place_distance;
                }
            }
        }

        long sysTime = SystemClock.elapsedRealtime();

        //check if it was close enough to any place, and that this isn't the first initialization
        //of the log, and if the user has rebooted his phone, if so, disregard this log
        if(closestLocation != null && closestLocation.timeLastUpdated != 0 &&
                (sysTime > closestLocation.timeLastUpdated)){
            //if this was the last logged, then we're going to assume the user didn't move
            if(closestLocation.lastLogged) closestLocation.time_spent +=
                    (sysTime - closestLocation.timeLastUpdated)/(1000*60);
            //arbituary constant which would be the time the user took to get to his current
            //location
            else closestLocation.time_spent +=
                    (sysTime - closestLocation.timeLastUpdated)/(2*1000*60);
        }

        for(MonitoredLocation ml : places){
            //reset logged status
            ml.lastLogged = false;

            //seconds since last time updated
            ml.timeLastUpdated = SystemClock.elapsedRealtime() /*milliseconds*/;
            db.monitoredLocationDao().updatePlace(ml);
        }
        if(closestLocation != null) {
            closestLocation.lastLogged = true;
            db.monitoredLocationDao().updatePlace(closestLocation);
        }
    }

}
