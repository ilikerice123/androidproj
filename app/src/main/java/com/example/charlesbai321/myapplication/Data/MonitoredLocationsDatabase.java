package com.example.charlesbai321.myapplication.Data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by charlesbai321 on 18/01/18.
 */


@Database(entities = {MonitoredLocation.class}, version = 1) //versions keeps track of
// the updates that you make - must update version when altering database
public abstract class MonitoredLocationsDatabase extends RoomDatabase{
    public abstract MonitoredLocationDao monitoredLocationDao();
}
