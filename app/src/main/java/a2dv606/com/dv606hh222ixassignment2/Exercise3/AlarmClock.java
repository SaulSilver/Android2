package a2dv606.com.dv606hh222ixassignment2.Exercise3;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import a2dv606.com.dv606hh222ixassignment2.R;

public class AlarmClock extends Activity implements AdapterView.OnItemSelectedListener{

    public static final String MyPREFERENCES = "MyPrefsFile";
    private TextView currentTimeTv;
    private TextView alarmTimeTv;
    private TimePicker alarmTimePicker;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private SharedPreferences settings;
    private static AlarmClock inst;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_clock);

        settings = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


        currentTimeTv = (TextView) findViewById(R.id.time_text);
        alarmTimeTv = (TextView) findViewById(R.id.alarm_time);

        settings.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                alarmTimeTv.setText(sharedPreferences.getString("alarm", "Alarm not set"));
            }
        });
        alarmTimeTv.setText(settings.getString("alarm", "Alarm not set"));

        final Intent INTENT = new Intent(getBaseContext(), AlarmReceiver.class);

        //Get the alarm manager service
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //Set the alarm to the time you picked
        final Calendar calendar = Calendar.getInstance();

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!interrupted()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currentTimeTv.setText("Current time: " + calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":"
                                        + calendar.getInstance().get(Calendar.MINUTE) + ":" + calendar.getInstance().get(Calendar.SECOND));
                            }
                        });
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException e) { e.printStackTrace();   }
            }
        };
        thread.start();

        alarmTimePicker = (TimePicker) findViewById(R.id.timePicker);

        final Button startAlarm = (Button) findViewById(R.id.alarm_on);
        startAlarm.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                int hour;
                int minute;

                int currentApiVersion = android.os.Build.VERSION.SDK_INT;
                if (currentApiVersion > android.os.Build.VERSION_CODES.LOLLIPOP_MR1){
                    hour = alarmTimePicker.getHour();
                    minute = alarmTimePicker.getMinute();
                } else {
                    hour = alarmTimePicker.getCurrentHour();
                    minute = alarmTimePicker.getCurrentMinute();
                }
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);

                String hourStr = String.valueOf(hour);
                String minuteStr = String.valueOf(minute);

                //For digits less than 10, they should be shown 0 + digit i.e. 09 or 07
                if(minute < 10)
                    minuteStr = "0" + minuteStr;

                if (hour > 12)
                    hourStr = String.valueOf(hour - 12);


                String alarmTime =  hourStr + ":" + minuteStr;
                INTENT.putExtra("extra", "yes");
                INTENT.putExtra("time", alarmTime);
                pendingIntent = PendingIntent.getBroadcast(AlarmClock.this, 0, INTENT, PendingIntent.FLAG_UPDATE_CURRENT);

                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                Log.e("Time now, "+ "Time is ", String.valueOf(calendar.getTime()));

                alarmTimeTv.setText("Alarm set to: " + alarmTime);

                SharedPreferences.Editor editor = settings.edit();
                editor.putString("alarm", "Alarm set to: " + alarmTime);
                editor.apply();

                Toast.makeText(getBaseContext(), "Alarm set to: " + alarmTime, Toast.LENGTH_LONG).show();
            }
        });

        final Button stopAlarm = (Button) findViewById(R.id.alarm_off);
        stopAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alarmManager.cancel(pendingIntent);
                alarmTimeTv.setText("Alarm is not set");

                Toast.makeText(getBaseContext(), "Alarm is canceled", Toast.LENGTH_LONG).show();

                SharedPreferences.Editor editor = settings.edit();
                editor.putString("alarm", "Alarm is not set");
                editor.apply();
            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    public void onResume() {
        super.onResume();
        String alarmMsg = settings.getString("alarm", "Alarm not set");
        alarmTimeTv.setText(alarmMsg);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
