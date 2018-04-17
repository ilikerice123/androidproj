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

/**
 * Comments:
 *
 * Because I wanted my PlaceAdapter to be able to display information for both different
 * categories as well as different different places, I needed an internal list structure that
 * can hold both of these. Originally, PlaceAdapter didn't even have its own internal list, and
 * instead just called the MainActivity's static list of places. But because I wanted it to
 * display categories as well, I created an interface called Displayable that both Category
 * and MonitoredLocation implement, and now I have an internal list of that type to display the
 * information.
 */
public class PlaceAdapter extends RecyclerView.Adapter {

    public static final String NEW_CATEGORY_OPTION = "New category...";

    //used as a reference for how to display progress
    private int maximum = 0;
    //used to create intents to launch other activities within this adapter
    private Context c;
    private List<Displayable> listtoDisplay;
    //this flag is so that when I'm displaying categories, I can't launch new activities by
    //clicking on the individual cardviews - you can only do that when you're viewing pictures
    private boolean clickable;

    public PlaceAdapter(Context c){
        this.c = c;
        listtoDisplay = new ArrayList<>();
        clickable = true;
        listtoDisplay.clear();
        listtoDisplay.addAll(MainActivity.places);
    }

    /**
     * these two functions are used to toggle between displaying categories, and displaying
     * individual places
     */
    public void displayCategory(){
        clickable = false;
        listtoDisplay.clear();
        listtoDisplay.addAll(MainActivity.categories);
        super.notifyDataSetChanged();
    }

    public void displayPlace(){
        clickable = true;
        listtoDisplay.clear();
        listtoDisplay.addAll(MainActivity.places);
        super.notifyDataSetChanged();
    }

    //initializes view of a single item in recycler view and returns it as a ViewHolder
    @Override
    public placesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext()).inflate(
                R.layout.placeinfo, parent, false);
        return new placesHolder(cv);
    }

    /**
     * wrapper class for a single recyclerview-cardview-holder type thing (honestly have
     * no idea what it's called)
     */
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

    /** Initialization of a new cardview. This sets the necessary text and also, if
     *  we are displaying locations as indicated by the flag, set an onClickListener
     *  for the different cards.
     * @param h
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, final int position) {
        placesHolder holder = (placesHolder) h; //this cast shouldn't be necessary
        //but for some reason although placesHolder extended viewholder, it was giving
        //me an error :/
        final int clickedposition = position;

        Displayable place = this.listtoDisplay.get(position);

        holder.place_name.setText(place.getTitleName());
        int time = place.getTimeSpent();
        if(time < 60) holder.time_spent.setText(time + "m");
        else holder.time_spent.setText(time/60 + "h " + time%60 + "m");

        holder.progressbar.setProgress(time * 100 / maximum);

        if(!clickable) return;

        h.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!clickable) return;


                MonitoredLocation ml =(MonitoredLocation) listtoDisplay.get(clickedposition);

                //TODO: because displayed list is not linked to main list, sorting the displayed
                //TODO: list messes up the retrieval of the monitoredlocation. this fixes it in
                //TODO: somewhat of an awkward fashion
                int index = 0;
                //find the id of the place clicked
                for(MonitoredLocation clicked : MainActivity.places){
                    if(clicked.getId() == ml.getId()) break;
                    index++;
                }
                Intent i = new Intent(c, SinglePlaceActivity.class);
                i.putExtra(MainActivity.POSITION_KEY, index);
                c.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listtoDisplay.size();
    }


    /**
     * Basically a wrapper around notifyDataSetChanged. I would've overriden
     * notifyDataSetChanged, but that function was a final function, so thus I just call
     * it at the end after I execute my logic. Basically, when the data is updated, there is
     * a chance that the new max for time_spent changes from what it was before, thereby
     * meaning the calculation I do for the progressbar element will be off, so I have to
     * recalculate the max.
     * @param viewPlace
     */
    public void refreshList(boolean viewPlace){
        if(listtoDisplay == null) return;

        int max = 1;
        for(Displayable d : listtoDisplay){
            if(d.getTimeSpent() > max) max = d.getTimeSpent();
        }

        maximum = max;

        super.notifyDataSetChanged();
    }

    /**
     * hey I can practice implementing different sorting algorithms here :D
     *
     * These next methods are called when the user selects to sort the data in a certain way
     * Using quicksort and 2 helper recursive functions to implement it
     */
    public void sortListTime(){
        //quicksort
        quickSortTime(0,listtoDisplay.size()-1);
        super.notifyDataSetChanged();
    }

    public void sortListAlpha(){
        quickSortAlpha(0,listtoDisplay.size()-1);
        super.notifyDataSetChanged();
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

}
