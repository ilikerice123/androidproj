package com.example.charlesbai321.myapplication.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.charlesbai321.myapplication.Data.Category;
import com.example.charlesbai321.myapplication.Data.Displayable;
import com.example.charlesbai321.myapplication.Data.MonitoredLocationDao;
import com.example.charlesbai321.myapplication.Data.MonitoredLocationsDatabase;
import com.example.charlesbai321.myapplication.Util.AlarmReciever;
import com.example.charlesbai321.myapplication.Util.GPSService;
import com.example.charlesbai321.myapplication.Util.GPSTracker;
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

public class MainActivity extends AppCompatActivity {

    public static final String USE_GPS = "use_gps";
    public static final String POSITION_KEY = "position_key";
    public static final String LOG = "what_is_going_ONNNN";
    public static final String LOCSTARTSTOP = "are_we_using_gps???";
    public static List<MonitoredLocation> places;
    public static List<String> categories_string;
    public static List<Category> categories;
    TextView places_text;
    RecyclerView rv;
    RecyclerView.LayoutManager rvManager;
    PlaceAdapter dataAdapter;
    boolean viewPlace;
    //might as well just have a database here and try to get rid of it if it's taking up
    //too memory
    static MonitoredLocationsDatabase db;


    /**
     * creates a list from the database, initializes view, alarm manager, and intent for the
     * alarm manager to send
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //initializes the display
        viewPlace = true;
        places_text = findViewById(R.id.setofplaces);
        //initializes the list of places from database
        (new InitializeLocationsTask(this)).execute("dummy string");
        categories = new ArrayList<>();
        categories_string = new ArrayList<>();
        places = new ArrayList<>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return true;
    }

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
                dataAdapter.sortListTime();
                break;
            }
            case R.id.sort_alpha: {
                dataAdapter.sortListAlpha();
                break;
            }
            case R.id.clear_list: {
                DialogFragment confirmation = new ConfirmPopup();
                confirmation.show(getFragmentManager(), "confirmation");
                break;
            }
            case R.id.add_location: {
                Intent i = new Intent(this, Main2Activity.class);
                startActivity(i);
                break;
            }

            case R.id.add_category: {
                AddCategoryPopup categoryPopup = new AddCategoryPopup();
                categoryPopup.show(getFragmentManager(), "category");
                break;
            }
        }
        return true;
    }

    /**
     * refresh recyclerview if recyclerview isn't null
     * if list of places has been deleted, run task to fetch it again
     * check for gps permissions
     */
    @Override
    protected void onResume(){
        super.onResume();
        if(dataAdapter != null) {
            dataAdapter.refreshList(viewPlace);
        }

        //whenever the list of places gets deleted (which it shouldn't), recreate it
        if(places == null) {
            (new InitializeLocationsTask(this)).execute("dummy string");
        }
        else{
            for(MonitoredLocation ml : places){
                if(!categories_string.contains(ml.category)) categories_string.add(ml.category);
            }
        }

        //request permissions
        if(!hasPermission(this)){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.
                    ACCESS_FINE_LOCATION}, GPSService.USE_GPS);
        }
    }


    /**
     * callback for request permissions, this will check if location access has been
     * granted. If not, kill app (or at least this activity, which should lead to app kill
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case GPSTracker.REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    //TODO: Code here to initialize GPS service
                } else {
                    GPSTracker.noPermissionsAlert(this);
                    finish();
                }
            }
        }
    }

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
     * checks if we have permission to access gps
     * @param c
     * @return
     */
    public static boolean hasPermission(Context c){
        return ContextCompat.checkSelfPermission(c,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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

    //AsyncTask
    //this runs on another thread to create the lists of monitored locations from android's
    //Room
    //every time this finishes executing, it notifies the adapter of that
    //https://stackoverflow.com/questions/16920942/getting-context-in-asynctask
    //from here, because I need to use context to get the database, I'm obtaining a weak
    //reference to the database which will prevent memory leaks

    //AsyncTask also has the job of initializing all of the different lists that this
    //activity contains, since all of the lists depend on the dataset
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

            places.addAll(db);
            refreshCategories();
            setupRecyclerView();
            dataAdapter.refreshList(viewPlace);
            Toast.makeText(a, places.toString(), Toast.LENGTH_SHORT).show();
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
    public class ConfirmPopup extends DialogFragment {
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
                                dataAdapter.refreshList(viewPlace);
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

    //https://developer.android.com/guide/topics/ui/dialogs.html
    @SuppressLint("ValidFragment")
    public class AddCategoryPopup extends DialogFragment{
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            builder.setTitle("Enter a category");

            builder.setView(inflater.inflate(R.layout.addcategory_popup, null))
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            final EditText et = (EditText)
                                    AddCategoryPopup.this.getDialog().findViewById(R.id.categoryAdd);

                            String s = et.getText().toString();

                            if(!s.equals("") && !categories_string.contains(s)){
                                categories_string.add(s);
                                Category newCategory = new Category(s, 0);
                                categories.add(newCategory);
                            }
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            AddCategoryPopup.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }
    }

    private void refreshCategories(){
        Map<String, Integer> mapToCategory = new HashMap<>();
        Set<String> s = new HashSet<>();

        if(places == null) return;

        for(MonitoredLocation ml : places){
            s.add(ml.category);
            if(mapToCategory.keySet().contains(ml.category)) {
                mapToCategory.put(ml.category,
                        mapToCategory.get(ml.category) + ml.time_spent);
            }
            else {
                mapToCategory.put(ml.category, 0);
            }
        }

        for(String string : mapToCategory.keySet()){
            Category c = new Category(string, mapToCategory.get(string));
            categories.add(c);
        }

        categories_string = new ArrayList<>(s);
    }




}
