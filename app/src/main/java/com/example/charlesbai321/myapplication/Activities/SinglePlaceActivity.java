package com.example.charlesbai321.myapplication.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.charlesbai321.myapplication.Data.MonitoredLocation;
import com.example.charlesbai321.myapplication.R;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;

public class SinglePlaceActivity extends AppCompatActivity {

    int position;
    TextView singlePlaceName;
    TextView singlePlaceDesc;
    MonitoredLocation singlePlace;
    EditText singlePlaceNickName;

    ArrayAdapter<String> categorySpinnerAdapter;
    Spinner categorySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_place);

        Toast.makeText(this, MainActivity.categories_string.toString(), Toast.LENGTH_SHORT).show();

        //extract the position of the recycler view that was clicked
        position = getIntent().getExtras().getInt(MainActivity.POSITION_KEY);
        singlePlaceName = findViewById(R.id.singleplaceName);

        //get position from the list, and set it to the title
        singlePlace = MainActivity.places.get(position);
        singlePlaceName.setText(singlePlace.name);

        singlePlaceNickName = findViewById(R.id.nickname);
        singlePlaceNickName.setText(singlePlace.nickName);

        singlePlaceDesc = findViewById(R.id.description);
//        Date d;
//        try{
//            d = MonitoredLocation.DATEFORMAT.parse(singlePlace.startTime);
//        }
//        catch(Exception e){} //shoudn't have an exception
        String startTime = singlePlace.startTime;
        String year = startTime.substring(0, 4);
        String month = (new DateFormatSymbols()).getMonths()
                [Integer.parseInt(startTime.substring(4, 6))-1];
        int day = Integer.parseInt(startTime.substring(6,8));

        singlePlaceDesc.setText("Starting from " + year + " " + month + " " + day + ", you have spent " +
                        singlePlace.time_spent / 60 + " hours and " + singlePlace.time_spent % 60 +
                        " minutes at " + singlePlace.name);

        //get reference to spinner (why is it even called a spinner?)
        categorySpinner = findViewById(R.id.spinner);
        //we're using the default spinner xml
        categorySpinnerAdapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, MainActivity.categories_string);
        categorySpinner.setAdapter(categorySpinnerAdapter);
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String s = MainActivity.categories_string.get(i);
                singlePlace.category = s;
                MainActivity.db.monitoredLocationDao().updatePlace(singlePlace);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //nothing happens, I shouldn't need to add anything
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        categorySpinnerAdapter.notifyDataSetChanged();
        categorySpinner.setSelection(MainActivity.categories_string.indexOf(singlePlace.category));
    }

    @Override
    protected void onPause(){
        super.onPause();
        String s = singlePlaceNickName.getText().toString();
        singlePlace.nickName = s;
        MainActivity.db.monitoredLocationDao().updatePlace(singlePlace);
    }

    public void deletePlace(View view) {
        if (singlePlace != null) {
            MainActivity.db.monitoredLocationDao().deleteMonitoredLocations(singlePlace);
            MainActivity.places.remove(singlePlace);
            Toast.makeText(this, "Deleted!", Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            Toast.makeText(this, "Error occurred, try again later", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void resetPlace(View view){
        if (singlePlace != null) {
            singlePlace.time_spent = 0;
            singlePlace.startTime = MonitoredLocation.DATEFORMAT.
                    format(Calendar.getInstance().getTime());
            MainActivity.db.monitoredLocationDao().updatePlace(singlePlace);
            Toast.makeText(this, "Reset!", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Error occurred, try again later", Toast.LENGTH_SHORT)
                    .show();
        }
    }


}
