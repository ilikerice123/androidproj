package com.example.charlesbai321.myapplication.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.charlesbai321.myapplication.Data.Category;
import com.example.charlesbai321.myapplication.Data.MonitoredLocation;
import com.example.charlesbai321.myapplication.R;
import com.example.charlesbai321.myapplication.Util.PlaceAdapter;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;

public class SinglePlaceActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {

    int position;
    TextView singlePlaceName;
    TextView singlePlaceDesc;
    MonitoredLocation singlePlace;
    EditText singlePlaceNickName;

    ArrayAdapter<String> categorySpinnerAdapter;
    Spinner categorySpinner;

    /**
     * sets up the view of the entire activity, as well as initializes the spinner. See
     * comments on setUpView() and within that function itself for more documentation
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_place);
        setUpView();
    }

    /**
     * Updates the spinner to show relevant results and sets the default spinner value to
     * the category that the location has
     */
    @Override
    protected void onResume(){
        super.onResume();
        categorySpinnerAdapter.notifyDataSetChanged();
        categorySpinner.setSelection(MainActivity.categories_string.indexOf(singlePlace.category));
    }

    /**
     * when the user gets out of the activity in any way possible, the database is updated
     * with the relevant Nickname information.
     */
    @Override
    protected void onPause(){
        super.onPause();
        String s = singlePlaceNickName.getText().toString();
        singlePlace.nickName = s;
        MainActivity.db.monitoredLocationDao().updatePlace(singlePlace);
    }

    /**
     * when this activity is stopped, the new string that was added to it that corresponded to
     * adding a new category is removed from the list
     */
    @Override
    protected void onStop(){
        super.onStop();
        MainActivity.categories_string.remove(PlaceAdapter.NEW_CATEGORY_OPTION);
    }

    /**
     * when the user clicks the delete button, the place is removed from the database and the
     * places Arraylist structure in the MainActivity. The activity finishes.
     * @param view
     */
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

    /**
     * when the user clicks the reset button, the date_started_logging and time_spent categories
     * of the selected location are set to null and updated within the database immediately.
     * @param view
     */
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

    //https://developer.android.com/guide/topics/ui/dialogs.html
    /**
     * this creates a dialog based on a simple xml layout file that contains an editText,
     * a Title, and buttons. After the user has entered a category and clicks Add Category,
     * if the category has not been added before and is not an empty string, then it is added
     * to the spinner, selected as the choice for the spinner, and updated within the database
     */
    @SuppressLint("ValidFragment")
    public class AddCategoryPopup extends DialogFragment {
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

                            if(!s.equals("") && !MainActivity.categories_string.contains(s)){
                                MainActivity.categories_string.add(s);
                                Category newCategory = new Category(s, 0);
                                MainActivity.categories.add(newCategory);
                                categorySpinner.setSelection(MainActivity.categories_string.size()-1);
                                singlePlace.category = s;
                                MainActivity.db.monitoredLocationDao().updatePlace(singlePlace);
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

    private void setUpView(){

        //extract the position of the recycler view that was clicked from the intent
        position = getIntent().getExtras().getInt(MainActivity.POSITION_KEY);
        singlePlaceName = findViewById(R.id.singleplaceName);

        //get place from position according to the list, and set it to the title
        singlePlace = MainActivity.places.get(position);
        singlePlaceName.setText(singlePlace.name);

        //set nickname
        singlePlaceNickName = findViewById(R.id.nickname);
        singlePlaceNickName.setText(singlePlace.nickName);

        //create a short textView that shows a bit more detailed information
        singlePlaceDesc = findViewById(R.id.description);
        String startTime = singlePlace.startTime;
        String year = startTime.substring(0, 4);
        String month = (new DateFormatSymbols()).getMonths()
                [Integer.parseInt(startTime.substring(4, 6))-1];
        int day = Integer.parseInt(startTime.substring(6,8));

        //this is so fricken ugly holy crap
        int time = singlePlace.time_spent;

        if(time / 60 == 1){
            if(time % 60 == 1){ //both singular
                singlePlaceDesc.setText("Starting from " + year + " " + month + " " + day + ", you have spent " +
                        singlePlace.time_spent / 60 + " hour and " + singlePlace.time_spent % 60 +
                        " minute at " + singlePlace.name);
            }
            else{  //hours singular
                singlePlaceDesc.setText("Starting from " + year + " " + month + " " + day + ", you have spent " +
                        singlePlace.time_spent / 60 + " hour and " + singlePlace.time_spent % 60 +
                        " minutes at " + singlePlace.name);
            }
        }
        else{
            if(time % 60 == 1){ //minutes singular
                singlePlaceDesc.setText("Starting from " + year + " " + month + " " + day + ", you have spent " +
                        singlePlace.time_spent / 60 + " hour and " + singlePlace.time_spent % 60 +
                        " minutes at " + singlePlace.name);
            }
            else{  //both plural
                singlePlaceDesc.setText("Starting from " + year + " " + month + " " + day + ", you have spent " +
                        singlePlace.time_spent / 60 + " hours and " + singlePlace.time_spent % 60 +
                        " minutes at " + singlePlace.name);
            }
        }

        //get reference to spinner (why is it even called a spinner?)
        categorySpinner = findViewById(R.id.spinner);
        //we're using the default spinner xml
        categorySpinnerAdapter = new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item, MainActivity.categories_string);
        categorySpinner.setAdapter(categorySpinnerAdapter);

        //godbless implementing activity functions. It makes code so much cleaner!!!
        categorySpinner.setOnItemSelectedListener(this);
    }

    /**
     * selected item callback. If it selects the first item on the list, then the first item
     * is to add your own item, so create a dialog for that. Otherwise, set the category to
     * the selected spinner item and update the database.
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String s;
        if(i == 0){
            DialogFragment addCategory = new AddCategoryPopup();
            addCategory.show(getFragmentManager(), "categoryPopup");
            s = MainActivity.categories_string.get(MainActivity.categories_string.size()-1);
        }
        else {
            s = MainActivity.categories_string.get(i);
        }
        singlePlace.category = s;
        MainActivity.db.monitoredLocationDao().updatePlace(singlePlace);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //nothing happens, I shouldn't need to add anything
    }
}
