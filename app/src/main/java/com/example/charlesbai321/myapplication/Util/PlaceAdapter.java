package com.example.charlesbai321.myapplication.Util;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.charlesbai321.myapplication.Activities.MainActivity;
import com.example.charlesbai321.myapplication.Activities.SinglePlaceActivity;
import com.example.charlesbai321.myapplication.Data.MonitoredLocation;
import com.example.charlesbai321.myapplication.R;

/**
 * Created by charlesbai321 on 17/01/18.
 */

public class PlaceAdapter extends RecyclerView.Adapter {

    //used as a reference for how to display progress
    private int maximum = 0;
    //used to create intents to launch other activities within this adapter
    private Context c;

    //might need more stuff later who knows
    public PlaceAdapter(Context c){
        this.c = c;
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
        final int clickedposition = position;

        h.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(c, SinglePlaceActivity.class);
                i.putExtra(MainActivity.POSITION_KEY, clickedposition);
                c.startActivity(i);
            }
        });
        MonitoredLocation place = MainActivity.places.get(position);
        holder.place_name.setText(place.name);
        holder.time_spent.setText(place.time_spent/60 + "h " + place.time_spent%60 + "m");
        holder.progressbar.setProgress(place.time_spent * 100 / maximum);
    }

    @Override
    public int getItemCount() {
        return MainActivity.places.size();
    }


    //refreshes the list, setting a new max progress bar value
    public void refreshList(){
        if(MainActivity.places != null){
            //set to 1 so when I divide by max, I don't get errors
            int max = 1;
            for(MonitoredLocation ml : MainActivity.places){
                if(ml.time_spent > max) max = ml.time_spent;
            }
            maximum = max;
        }
        else maximum = Integer.MAX_VALUE; //set it to the maximum value, so that all of the
                                          //progress bars should display 0
        super.notifyDataSetChanged();
    }

    /**
     * hey I can practice implementing different sorting algorithms here :D
     */
    public void sortList(){

        super.notifyDataSetChanged();
    }
}
