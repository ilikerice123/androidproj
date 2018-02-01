package com.example.charlesbai321.myapplication.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.charlesbai321.myapplication.Data.MonitoredLocation;
import com.example.charlesbai321.myapplication.R;

public class SinglePlaceActivity extends AppCompatActivity {

    int position;
    TextView singlePlaceName;
    MonitoredLocation singlePlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_place);
        position = getIntent().getExtras().getInt(MainActivity.POSITION_KEY);
        singlePlaceName = findViewById(R.id.singleplaceName);
        //get position from the list
        singlePlace = MainActivity.places.get(position);
        singlePlaceName.setText(singlePlace.name);
    }

    public void deletePlace(View view){
        MainActivity.db.monitoredLocationDao().deleteMonitoredLocations(singlePlace);

    }


}
