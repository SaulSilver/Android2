package a2dv606.com.dv606hh222ixassignment2.Exercise1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import a2dv606.com.dv606hh222ixassignment2.R;

/**
 * An activity for creating/updating a new event in the calendar
 *
 * Created by hatem on 2017-07-05.
 */

public class NewEvent extends AppCompatActivity {

    private EditText countryNameField;
    private EditText visitYearField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_event_calendar);

        countryNameField = (EditText) findViewById(R.id.et_name);
        visitYearField = (EditText) findViewById(R.id.et_year);

        Button saveButton = (Button) findViewById(R.id.btSave);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String countryName = countryNameField.getText().toString();
                String yearText = visitYearField.getText().toString();

                if(countryName.isEmpty() || yearText.isEmpty() || Integer.parseInt(yearText) < 1900 || Integer.parseInt(yearText) > 2018 ) {    //2018 is max in case the app is tested in 2018
                    Toast error = Toast.makeText(getApplicationContext(), "Both fields have to be filled correctly", Toast.LENGTH_SHORT);
                    error.show();

                } else {
                    Intent resultIntent = new Intent(getBaseContext(), MyCountriesCalender.class);
                    resultIntent.putExtra("country", countryName);
                    resultIntent.putExtra("year", yearText);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
            }
        });
    }
}
