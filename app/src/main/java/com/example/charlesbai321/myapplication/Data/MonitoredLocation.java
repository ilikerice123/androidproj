package com.example.charlesbai321.myapplication.Data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by charlesbai321 on 17/01/18.
 */

@Entity(tableName = MonitoredLocation.DATABASE_KEY) //<- this marks it as an entity for Room
public class MonitoredLocation implements Parcelable {
    public static final String DATABASE_KEY = "monitoredlocation_database";

    //needs to be unique across different MonitoredLocation objects - name should suffice (?)
    //this value is used to differentiate and search items in your store
    @PrimaryKey(autoGenerate = true)
    private int id;

    //I originally had a LatLng object, but then I realized that I'd have to create a converter
    //so I just decided to separate it since LatLng is just a wrapper around the two doubles
    //anyway, and just return LatLng when I need it
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "nick_name")
    public String nickName;
    @ColumnInfo(name = "latitude_position")
    public double latitude;
    @ColumnInfo(name = "longitude_position")
    public double longitude;
    @ColumnInfo(name = "time_spent")
    //these are initialized to 0 by default, so it's fine
    public int time_spent;
    /**
     * this is the same throughout every object in the database -
     * it just marks when the location has last been updated.
     */
    @ColumnInfo(name = "time_last_updated")
    public long timeLastUpdated;
    @ColumnInfo(name = "last_logged")
    public boolean lastLogged;

    public MonitoredLocation(){
    }

    @Ignore
    public MonitoredLocation(String name, String nickName, LatLng location){
        this.name = name;
        this.nickName = nickName;
        this.latitude = location.latitude;
        this.longitude = location.longitude;
        time_spent = 0;
    }

    @Ignore
    public MonitoredLocation(String name, String nickName, LatLng location, int time_spent){
        this.name = name;
        this.nickName = nickName;
        this.latitude = location.latitude;
        this.longitude = location.longitude;
        this.time_spent = time_spent;
    }

    void addMinutes(int minutes){
        time_spent += minutes;
    }

    public int getId() {
        return id;
    }

    public LatLng getLocation(){
        return new LatLng(latitude, longitude);
    }

    public void setId(int id){
        this.id = id;
    }

    @Override
    public String toString(){
        return "[" + id + ": " + name + ", timelastUpdated: " + timeLastUpdated +
                ", time_spent: " + time_spent + ", lastLogged: " + lastLogged + "]";
    }


    //-----------------------------------------------------------------------
    //stuff below this is just for implementing parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[] {name, Double.toString(latitude),
            Double.toString(longitude), Integer.toString(time_spent)});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public MonitoredLocation createFromParcel(Parcel parcel) {
            return new MonitoredLocation(parcel);
        }

        @Override
        public MonitoredLocation[] newArray(int i) {
            return new MonitoredLocation[i];
        }
    };

    private MonitoredLocation(Parcel in){
        String[] data = new String[4];
        in.readStringArray(data);
        this.name = data[0];
        double lat = Double.parseDouble(data[1]);
        double lng = Double.parseDouble(data[2]);
        this.time_spent = Integer.parseInt(data[3]);
        this.latitude = lat;
        this.longitude = lng;
    }
}

