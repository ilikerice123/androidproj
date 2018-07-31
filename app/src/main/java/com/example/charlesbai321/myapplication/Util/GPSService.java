package com.example.charlesbai321.myapplication.Util;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.charlesbai321.myapplication.Activities.MainActivity;
import com.example.charlesbai321.myapplication.Data.MonitoredLocation;
import com.example.charlesbai321.myapplication.Data.MonitoredLocationsDatabase;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import java.util.List;

/**
 * Created by charlesbai321 on 18/01/18.
 */

/**
 * this class is completely useless now. XDDD Read below for why
 *
 * This has been replaced with a service that starts AlarmManager, of which calls
 * ParseLocationService
 *
 * Originally, this was how my app was going to request locations every 15 or so minutes,
 * but because services don't run when the CPU is a asleep and obtaining a constant CPU
 * wakelock is not a valid solution, this class is now deprecated.
 */
//TODO: July 30, 2018 check what this class used to do...
public class GPSService extends Service {
    public static final int USE_GPS = 3;

    private MonitoredLocationsDatabase db;
    private LocationRequest lr;
    private FusedLocationProviderClient locationclient;

    private LocationCallback callback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult){
            //TODO: check if the location is close to anywhere!
            Location l = locationResult.getLastLocation();
            handleLocation(l);
            Toast.makeText(getApplicationContext(), "HELLO, IS THIS WORKING??? *TAP* *TAP*", Toast.LENGTH_SHORT).show();
        }
    };

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate(){
        initializeLocationRequest();
        db = Room.databaseBuilder(this,
                MonitoredLocationsDatabase.class, MonitoredLocation.DATABASE_KEY)
                .allowMainThreadQueries().build();
    }

    @Override
    public void onDestroy(){
        if(callback != null) {
            locationclient.removeLocationUpdates(callback);
        }
    }

    public boolean checkPermission(){
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    //TODO: check what this code does
    private void parseLocation(Location l){
        Log.d(MainActivity.LOG, l.toString());
        Toast.makeText(this, l.toString(), Toast.LENGTH_SHORT).show();
        Toast.makeText(this,
                db.monitoredLocationDao().getListOfLocations().toString(),
                Toast.LENGTH_SHORT).show();
    }

    //taken from ParseLocationService <- Can I not just use this instead of parseLocation??
    private void handleLocation(Location l){
        List<MonitoredLocation> places = db.monitoredLocationDao().getListOfLocations();

        double shortestDistance = Double.MAX_VALUE;
        MonitoredLocation closestLocation = null;

        // by the end of this for loop, closest location will hold the monitoredlocation that
        // is closest to the current location, or null if no monitoredlocations are within
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
                //arbitrary constant which would be the time the user took to get to his current
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

    private void initializeLocationRequest(){
        if(checkPermission()) {
            lr = new LocationRequest();
            lr.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            //TODO: might see if I need to tweak this
            lr.setFastestInterval(10000/*TEN_MINUTES*/);
            lr.setInterval(10000/*TEN_MINUTES*/);

            //initialize client
            locationclient = LocationServices.getFusedLocationProviderClient(this);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(lr);

            locationclient.requestLocationUpdates(lr, callback, null);
        }
        else{
            Toast.makeText(this, "No permissions for GPS", Toast.LENGTH_SHORT).show();
        }
    }


}
