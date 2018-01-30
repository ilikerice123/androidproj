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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String USE_GPS = "use_gps";
    public static final String PLACES_KEY = "places_key";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String LOG = "what_is_going_ONNNN";
    public static final String LOCSTARTSTOP = "are_we_using_gps???";
    public static List<MonitoredLocation> places;
    TextView places_text;
    RecyclerView rv;
    RecyclerView.LayoutManager rvManager;
    PlaceAdapter dataAdapter;
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

        places_text = findViewById(R.id.setofplaces);
        //initializes the list of places from database
        (new InitializeLocationsTask(this)).execute("dummy string");

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
            dataAdapter.refreshList();
        }
        //places_text.setText(places.toString());
        //whenever the list of places gets deleted (which it shouldn't), recreate it
        if(places == null) (new InitializeLocationsTask(this))
                .execute("dummy string");

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
        startService(i);
    }

    /**
     * method that executes when user presses add location button. launches new activity
     * @param view
     */
    public void addLocation(View view){
        Intent i = new Intent(this, Main2Activity.class);
        startActivity(i);
    }

    /**
     * clear the list of saved locations - deletes them not only from temporary list
     * created for recycler view, but also from the database
     * @param view
     */
    public void clearList(View view){
        if(db != null){
            for(MonitoredLocation ml : places) {
                db.monitoredLocationDao().deleteMonitoredLocations(ml);
            }
            places.clear();
            Toast.makeText(this, "Clear Success", Toast.LENGTH_SHORT).show();
            dataAdapter.refreshList();
        }
        else Toast.makeText(this,
                "Error occured, restart app and try again", Toast.LENGTH_SHORT).show();
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
        dataAdapter = new PlaceAdapter(); //data adapter
        rv.setAdapter(dataAdapter);
    }

    //AsyncTask
    //this runs on another thread to create the lists of monitored locations from android's
    //Room
    //every time this finishes executing, it notifies the adapter of that
    //https://stackoverflow.com/questions/16920942/getting-context-in-asynctask
    //from here, because I need to use context to get the database, I'm obtaining a weak
    //reference to the database which will prevent memory leaks
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
            places = db;
            setupRecyclerView();
            dataAdapter.refreshList();
        }

        public InitializeLocationsTask(Activity a){
            this.weakActivity = new WeakReference<>(a);
        }
    }


    //NOT NEEDED --------------------------------------------------------------------
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
    public class GotoSettingsPopup extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String message =
                    "It seems you are on a HuaWei or Xiaomi phone. Please add it to " +
                    "the list of allowed apps in the next screen in order for this app " +
                    "to work properly.";

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(message)
                    .setPositiveButton("Next", new DialogInterface.OnClickListener() {
                        //https://stackoverflow.com/questions/40660216/ontaskremoved-not-getting-called-in-huawei-and-xiomi-devices
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.miui.securitycenter",
                                    "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            return builder.create();
        }
    }
}
