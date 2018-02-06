package com.example.charlesbai321.myapplication.Util;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.charlesbai321.myapplication.Activities.MainActivity;
import com.example.charlesbai321.myapplication.Activities.SinglePlaceActivity;
import com.example.charlesbai321.myapplication.Data.Displayable;
import com.example.charlesbai321.myapplication.Data.MonitoredLocation;
import com.example.charlesbai321.myapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by charlesbai321 on 17/01/18.
 */

public class PlaceAdapter extends RecyclerView.Adapter {

    //used as a reference for how to display progress
    private int maximum = 0;
    //used to create intents to launch other activities within this adapter
    private Context c;
    private List<Displayable> listtoDisplay;
    private boolean clickable;

    //might need more stuff later who knows
    public PlaceAdapter(Context c){
        this.c = c;
        listtoDisplay = new ArrayList<>();
        clickable = true;
        listtoDisplay.clear();
        listtoDisplay.addAll(MainActivity.places);
    }

    public void displayCategory(){
        clickable = false;
        listtoDisplay.clear();
        listtoDisplay.addAll(MainActivity.categories);
    }

    public void displayPlace(){
        clickable = true;
        listtoDisplay.clear();
        listtoDisplay.addAll(MainActivity.places);
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
    public void onBindViewHolder(RecyclerView.ViewHolder h, final int position) {
        placesHolder holder = (placesHolder) h; //this cast shouldn't be necessary
        //but for some reason although placesHolder extended viewholder, it was giving
        //me an error :/
        final int clickedposition = position;

        h.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!clickable) return;

                Intent i = new Intent(c, SinglePlaceActivity.class);
                i.putExtra(MainActivity.POSITION_KEY, clickedposition);
                c.startActivity(i);
            }
        });

        Displayable place = this.listtoDisplay.get(position);

        holder.place_name.setText(place.getTitleName());
        int time = place.getTimeSpent();
        if(time < 60) holder.time_spent.setText(time + "m");
        else holder.time_spent.setText(time/60 + "h " + time%60 + "m");

        holder.progressbar.setProgress(time * 100 / maximum);
    }

    @Override
    public int getItemCount() {
        return listtoDisplay.size();
    }


    //refreshes the list, setting a new max progress bar value
    public void refreshList(boolean viewPlace){
        if(listtoDisplay == null) return;

        if(viewPlace) {
            listtoDisplay.clear();
            listtoDisplay.addAll(MainActivity.places);
        }
        else {
            listtoDisplay.clear();
            listtoDisplay.addAll(MainActivity.categories);
        }

        int max = 1;
        for(Displayable d : listtoDisplay){
            if(d.getTimeSpent() > max) max = d.getTimeSpent();
        }

        maximum = max;

        super.notifyDataSetChanged();
    }

    /**
     * hey I can practice implementing different sorting algorithms here :D
     */
    public void sortListTime(){
        //quicksort
        quickSortTime(0,listtoDisplay.size()-1);
        super.notifyDataSetChanged();
        Toast.makeText(c, "Sorted!", Toast.LENGTH_SHORT).show();
    }

    private void quickSortTime(int low, int high){
        if(low < high){
            int pivot = listtoDisplay.get(high).getTimeSpent();
            int wall = low;
            for(int i = low; i < high; i++){
                if(listtoDisplay.get(i).getTimeSpent() > pivot){
                    Displayable temp = listtoDisplay.get(wall);
                    listtoDisplay.set(wall, listtoDisplay.get(i));
                    listtoDisplay.set(i, temp);
                    wall++;
                }
            }

            Displayable temp = listtoDisplay.get(high);
            listtoDisplay.set
                    (high, listtoDisplay.get(wall));
            listtoDisplay.set(wall, temp);

            quickSortTime(low, wall-1);
            quickSortTime(wall+1, high);
        }
    }

    private void quickSortAlpha(int low, int high){
        if(low < high){
            String pivot = listtoDisplay.get(high).getTitleName();
            int wall = low;
            for(int i = low; i < high; i++){
                if(0 < pivot.compareTo(listtoDisplay.get(i).getTitleName())){
                    Displayable temp = listtoDisplay.get(wall);
                    listtoDisplay.set
                            (wall, listtoDisplay.get(i));
                    listtoDisplay.set(i, temp);
                    wall++;
                }
            }

            Displayable temp = listtoDisplay.get(high);
            listtoDisplay.
                    set(high, listtoDisplay.get(wall));
            listtoDisplay.set(wall, temp);

            quickSortAlpha(low, wall-1);
            quickSortAlpha(wall+1, high);
        }
    }

    public void sortListAlpha(){
        quickSortAlpha(0,listtoDisplay.size()-1);
        super.notifyDataSetChanged();
        Toast.makeText(c, "Sorted!", Toast.LENGTH_SHORT).show();
    }
}
