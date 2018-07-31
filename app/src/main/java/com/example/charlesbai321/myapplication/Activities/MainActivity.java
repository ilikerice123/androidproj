package com.example.charlesbai321.myapplication.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.charlesbai321.myapplication.Data.Category;
import com.example.charlesbai321.myapplication.Data.MonitoredLocationsDatabase;
import com.example.charlesbai321.myapplication.Util.DataTransfer;
import com.example.charlesbai321.myapplication.Util.GPSService;
import com.example.charlesbai321.myapplication.Data.MonitoredLocation;
import com.example.charlesbai321.myapplication.Util.PlaceAdapter;
import com.example.charlesbai321.myapplication.R;
import com.example.charlesbai321.myapplication.Util.StartGPSService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements DataTransfer {
    //what's the state of the recyclerview?

    public enum ViewState {
        DEFAULT, TIME_SORTED, ALPH_SORTED
    }
    ViewState state;

    //bunch of string constants
    public static final String USE_GPS = "use_gps";
    public static final String POSITION_KEY = "position_key";
    public static final String LOG = "what_is_going_ONNNN";
    public static final String LOCSTARTSTOP = "are_we_using_gps???";

    //rep-invariant this List should be a direct representation of the database - every time the
    //data base is updated, places should be updated along with it
    public static List<MonitoredLocation> places;
    //set of category names for the spinner
    public static List<String> categories_string;
    //categories that is constructed when places is updated
    public static List<Category> categories;

    RecyclerView rv;
    RecyclerView.LayoutManager rvManager;
    PlaceAdapter dataAdapter;
    //flag to indicate what we are viewing
    boolean viewPlace;
    //might as well just have a database here and try to get rid of it if it's taking up
    //too memory
    static MonitoredLocationsDatabase db;


    /**
     * THIS INFORMATION MOVED TO ONRESUME
     * creates a list from the database, initializes view, alarm manager, and intent for the
     * alarm manager to send
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //initializes the display
        state = ViewState.DEFAULT;
    }

    /**
     * to inflate the menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return true;
    }

    /**
     * callback for what happens when different menu options are selected. The menu items
     * are defined in res/menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(dataAdapter == null) return true;
        switch(item.getItemId()){
            case R.id.view_category: {
                viewPlace = false;
                dataAdapter.displayCategory();
                dataAdapter.refreshList(viewPlace);
                break;
            }
            case R.id.view_places: {
                viewPlace = true;
                dataAdapter.displayPlace();
                dataAdapter.refreshList(viewPlace);
                break;
            }
            case R.id.sort_time: {
                state = ViewState.TIME_SORTED;
                dataAdapter.sortListTime();
                break;
            }
            case R.id.sort_alpha: {
                state = ViewState.TIME_SORTED;
                dataAdapter.sortListAlpha();
                break;
            }
            case R.id.clear_list: {
                //add a dialog to confirm that this is what the user wants to do
                DialogFragment confirmation = new ConfirmPopup();
                confirmation.show(getFragmentManager(), "confirmation");
                break;
            }
            case R.id.add_location: {
                //start the addlocation activity
                Intent i = new Intent(this, AddLocationActivity.class);
                startActivity(i);
                break;
            }
        }
        return true;
    }

    /**
     * refresh recyclerview if recyclerview isn't null
     * if list of places has been deleted, run task to fetch it again
     * check for gps permissions
     * refreshes list of categories
     */
    @Override
    protected void onResume(){
        super.onResume();

        viewPlace = true;
        //create these on every iteration of onResume, since the user could have closed the
        //screen, and upon opening it again, the user would want to see updated results
        categories = new ArrayList<>();
        categories_string = new ArrayList<>();
        places = new ArrayList<>();

        //whenever the list of places gets deleted (which it shouldn't), recreate it
        (new InitializeLocationsTask(this)).execute("dummy string");

        //request permissions
        if(!hasPermission(this)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.
                    ACCESS_FINE_LOCATION}, GPSService.USE_GPS);
        }
    }


    /**
     * callback for request permissions, this will check if location access has been
     * granted. If not, kill app (or at least this activity, which should lead to app kill
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case GPSService.USE_GPS: {
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    //TODO: Code here to initialize GPS service
                }
                else {
                    finish();
                }
            }
        }
    }


//   COMMENT: The whole reason why we need to start the alarm in a service is so that
//   the alarm doesn't get destroyed or cancelled when our app is destroyed or cancelled.
    /**
     * starts the service that sets the alarm.
     * @param view
     */
    public void startGPSService(View view){
        Intent i = new Intent(this, StartGPSService.class);
        i.putExtra(MainActivity.USE_GPS, true);
        Toast.makeText(this, "Started tracking locations", Toast.LENGTH_SHORT).show();
        startService(i);
    }

    /**
     * start the same service as StartGPSService, but this time passing false in the
     * intent to signify we need to cancel the alarm
     * @param view
     */
    public void stopGPSService(View view){
        //we reset the time last updated to 0. this way, when the user starts it up again,
        //it doesn't assign all the time that the user wasn't tracking to the first closest
        //location that matches
        for(MonitoredLocation ml : places){
            ml.timeLastUpdated = 0;
            db.monitoredLocationDao().updatePlace(ml);
        }
        Intent i = new Intent(this, StartGPSService.class);
        i.putExtra(MainActivity.USE_GPS, false);
        Toast.makeText(this, "Stopped tracking locations", Toast.LENGTH_SHORT).show();
        startService(i);
    }

    /**
     * a few lines to initialize the recyclerView, the layout manager, and the data adapter
     */
    private void setupRecyclerView(){
        rv = findViewById(R.id.recyclerView); //obtains reference to rView
        rvManager = new LinearLayoutManager(this); //layout manager
        rv.setLayoutManager(rvManager);
        dataAdapter = new PlaceAdapter(this); //data adapter
        rv.setAdapter(dataAdapter);
    }
    /**
     *  AsyncTask
     *  this runs on another thread to create the lists of monitored locations from android's
     *  Room
     *  every time this finishes executing, it notifies the adapter of that

     *  https://stackoverflow.com/questions/16920942/getting-context-in-asynctask
     *  from here, because I need to use context to get the database, I'm obtaining a weak
     *  reference to the database which will prevent memory leaks

     *  AsyncTask also has the job of initializing all of the different lists that this
     *  activity contains, since all of the lists depend on the dataset
     */
    private class InitializeLocationsTask
            extends AsyncTask<String, Void, List<MonitoredLocation>>{
        private final WeakReference<Activity> weakActivity;

        protected List<MonitoredLocation> doInBackground(String... strings) {
            //the constructor is left as a string because I don't know how to deal with
            //void objects XD
            Activity a = weakActivity.get();
            if(a == null || a.isFinishing() || a.isDestroyed()){
                return null;
            }

            db = Room.databaseBuilder(a,
                    MonitoredLocationsDatabase.class, MonitoredLocation.DATABASE_KEY)
                    .allowMainThreadQueries().build();
            return db.monitoredLocationDao().getListOfLocations();
        }

        @Override
        protected void onPostExecute(List<MonitoredLocation> db){
            Activity a = weakActivity.get();
            if(a == null || a.isFinishing() || a.isDestroyed() || db == null){
                return;
            }

            //TODO: it might be better to use places = new ArrayList<>(db) in case we ever
            //TODO: run into a null pointer exception - the garbage collector should take care
            //TODO: of the unreferenced memory
            places.addAll(db);
            refreshCategories();
            setupRecyclerView();
            dataAdapter.refreshList(viewPlace);
            if(state == ViewState.TIME_SORTED) dataAdapter.sortListTime();
            if (state == ViewState.ALPH_SORTED) dataAdapter.sortListAlpha();
        }

        public InitializeLocationsTask(Activity a){
            this.weakActivity = new WeakReference<>(a);
        }
    }


    //PURPOSE CHANGED --------------------------------------------------------------------
    //I originally needed this because I had a START_STICKY service that would continuously
    //run when my app closed, but on xiaomi and huawei phones, there needed to be an extra step
    //in order to allow services to be restarted, which is what this dialog box prompts the user
    //to do when they click next. However, now I'm using alarm manager in conjunction with
    //Intent services in order to fire the necessary requests, so I don't need this dialog box
    //anymore
    //
    //
    //Called on oncreate by:
//            DialogFragment popUp = new GotoSettingsPopup();
//            popUp.show(getFragmentManager(), "xiaomi_popup");
    /**
     * creates an alertdialog object that can be placed somewhere.
     * Code mostly from android studio
     */
    @SuppressLint("ValidFragment")
    public static class ConfirmPopup extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String message =
                    "Are you sure you want to delete all of your data?";

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(message)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        //https://stackoverflow.com/questions/40660216/ontaskremoved-not-getting-called-in-huawei-and-xiomi-devices
                        public void onClick(DialogInterface dialog, int id) {
                            if(db != null){
                                for(MonitoredLocation ml : places) {
                                    db.monitoredLocationDao().deleteMonitoredLocations(ml);
                                }
                                places.clear();
                                DataTransfer dt = (DataTransfer) getActivity();
                                dt.updateList();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            return builder.create();
        }
    }

    /**
     * essentially iterates through the list of places, and creates a set of categories
     * with the time spent in each of them. It does it by first creating a map that maps
     * each category string to the time_spent. If it encounters a string element that
     * is already part of the map, the time_spent is simply added on.
     *
     */
    private void refreshCategories(){
        Map<String, Integer> mapToCategory = new HashMap<>();
        Set<String> s = new HashSet<>();

        if(places == null) return;

        for(MonitoredLocation ml : places){
            s.add(ml.category);
            if(mapToCategory.keySet().contains(ml.category)) {
                int temp = mapToCategory.remove(ml.category);
                mapToCategory.put(ml.category, temp + ml.time_spent);
            }
            else {
                mapToCategory.put(ml.category, ml.time_spent);
            }
        }

        categories.clear();
        for(String string : mapToCategory.keySet()){
            Category c = new Category(string, mapToCategory.get(string));
            categories.add(c);
        }

        categories_string = new ArrayList<>(s);
    }

    /**
     * Utility function: checks if we have permission to access gps
     */
    public static boolean hasPermission(Context c){
        return ContextCompat.checkSelfPermission(c,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     *
     */
    public void updateData(String s){
        //do nothing, updateData is for the other dialog fragment which saves a string, this one doesn't
    }

    public void updateList(){
        dataAdapter.refreshList(viewPlace);
    }



}
