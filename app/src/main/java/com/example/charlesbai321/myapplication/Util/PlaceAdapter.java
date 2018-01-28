package com.example.charlesbai321.myapplication.Util;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.charlesbai321.myapplication.Activities.MainActivity;
import com.example.charlesbai321.myapplication.Data.MonitoredLocation;
import com.example.charlesbai321.myapplication.R;

/**
 * Created by charlesbai321 on 17/01/18.
 */

public class PlaceAdapter extends RecyclerView.Adapter {

    //might need more stuff later who knows
    public PlaceAdapter(){
    }

    //initializes view of a single item in recycler view and returns it as a ViewHolder
    @Override
    public placesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext()).inflate(
                R.layout.placeinfo, parent, false);

        return new placesHolder(cv);
    }

    //has a bunch of public members that are used to reference specific UI elements
    //in a single "item xml"
    public static class placesHolder extends RecyclerView.ViewHolder {
        protected TextView place_name;
        protected ProgressBar progressbar;
        protected TextView time_spent;

        public placesHolder(View itemView) {
            super(itemView);
            place_name = itemView.findViewById(R.id.placeName);
            progressbar = itemView.findViewById(R.id.placeProgress);
            time_spent = itemView.findViewById(R.id.time_spent);
        }

    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
        placesHolder holder = (placesHolder) h; //this cast shouldn't be necessary
        //but for some reason although placesHolder extended viewholder, it was giving
        //me an error :/
        MonitoredLocation place = MainActivity.places.get(position);
        holder.place_name.setText(place.name);
        holder.time_spent.setText(Integer.toString(place.time_spent));
    }

    @Override
    public int getItemCount() {
        return MainActivity.places.size();
    }
}
