package com.example.charlesbai321.myapplication.Data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by charlesbai321 on 18/01/18.
 */

@Dao//data access object - this is how I access data in the database
public interface MonitoredLocationDao {
    //select from part selects it based on the name of your database, which u specified
    //on your entities list
    @Query("SELECT * FROM " + MonitoredLocation.DATABASE_KEY)
    List<MonitoredLocation> getListOfLocations();

    @Delete
    void deleteMonitoredLocations(MonitoredLocation... place);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(MonitoredLocation... place);

    @Update
    void updatePlace(MonitoredLocation... place);
}
