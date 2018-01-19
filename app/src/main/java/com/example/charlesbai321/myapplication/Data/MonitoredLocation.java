package com.example.charlesbai321.myapplication.Data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by charlesbai321 on 17/01/18.
 */

@Entity //<- this marks it as an entity for Room
public class MonitoredLocation implements Parcelable {

    //needs to be unique across different MonitoredLocation objects - name should suffice (?)
    @PrimaryKey //<- this value is used to differentiate and search items in your store
    public String name;
    public LatLng location;
    public int time_spent;

    public MonitoredLocation(String name, LatLng location){
        this.name = name;
        this.location = location;
        time_spent = 0;
    }

    public MonitoredLocation(String name, LatLng location, int time_spent){
        this.name = name;
        this.location = location;
        this.time_spent = time_spent;
    }

    void addMinutes(int minutes){
        time_spent += minutes;
    }

    @Override
    public String toString(){
        return name;
    }


    //-----------------------------------------------------------------------
    //stuff below this is just for implementing parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[] {name, Double.toString(location.latitude),
            Double.toString(location.longitude), Integer.toString(time_spent)});
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

    public MonitoredLocation(Parcel in){
        String[] data = new String[4];
        in.readStringArray(data);
        this.name = data[0];
        double lat = Double.parseDouble(data[1]);
        double lng = Double.parseDouble(data[2]);
        this.time_spent = Integer.parseInt(data[3]);
        this.location = new LatLng(lat, lng);
    }

}

