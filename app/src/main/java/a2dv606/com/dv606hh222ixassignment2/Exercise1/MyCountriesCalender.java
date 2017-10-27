package a2dv606.com.dv606hh222ixassignment2.Exercise1;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract.Events;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.provider.CalendarContract.Calendars;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import a2dv606.com.dv606hh222ixassignment2.R;

import static android.provider.CalendarContract.ACCOUNT_TYPE_LOCAL;

public class MyCountriesCalender extends AppCompatActivity implements CalendarProviderClient {

    private ListView countryListView;
    private MySimpleCursAdapter cursorAdapter;
    private SharedPreferences sharedPrefs;
    private String toUpdateEventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_countries_calender);

        getMyCountriesCalendarId();

        int[] to = new int[]{R.id.tvName, R.id.tvYear};
        cursorAdapter = new MySimpleCursAdapter(this, R.layout.countries_calendar_row, null, EVENTS_LIST_PROJECTION, to, 0);

        countryListView = (ListView) findViewById(R.id.countries_listView);
        countryListView.setAdapter(cursorAdapter);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        cursorAdapter.notifyDataSetChanged();

        initializeLoader();
    }

    /**
     * Setting up spinner in the menu
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ma_menu, menu);

        MenuItem item = menu.findItem(R.id.spinner);
        final Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_list_item_array,
                R.layout.my_spinner_item);
        adapter.setDropDownViewResource(R.layout.my_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setSelection(0, false);         //Disables the OnItemSelected method from firing up on instantiation
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                Bundle sortOrder = new Bundle();        //Sorting order bundle for the Loader Manager
                SharedPreferences.Editor editor = sharedPrefs.edit();

                switch (position) {
                    //case 0 is default sorting so it's not handled
                    case 1:             //Ascending by country
                        sortOrder.putString("SortOrder", (Events.TITLE + " ASC"));
                        editor.putString("sortSharedPref", (Events.TITLE + " ASC"));
                        break;
                    case 2:             //Descending by country
                        sortOrder.putString("SortOrder", (Events.TITLE + " DESC"));
                        editor.putString("sortSharedPref", (Events.TITLE + " DESC"));
                        break;
                    case 3:             //Ascending by year
                        sortOrder.putString("SortOrder", (Events.DTSTART + " ASC"));
                        editor.putString("sortSharedPref", (Events.DTSTART + " ASC"));
                        break;
                    case 4:             //descending by year
                        sortOrder.putString("SortOrder", (Events.DTSTART + " DESC"));
                        editor.putString("sortSharedPref", (Events.DTSTART + " DESC"));
                        break;
                }
                editor.apply();

                initializeLoader();        //Apply sorting order
                cursorAdapter.notifyDataSetChanged();

                if (position != 0)      //Not showing a toast for the default sort (useless)
                    Toast.makeText(parent.getContext(), "Sorted by " + parent.getItemAtPosition(position), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return true;
    }

    /**
     * When "add event" icon is pressed
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_add_event:
                Intent updateIntent = new Intent(this, NewEvent.class);
                startActivityForResult(updateIntent, 1);            //Requestcode = 1 for new event
                break;
            case R.id.item_preferences:
                Intent preferencesIntent = new Intent(this, MyPreferenceActivity.class);
                startActivityForResult(preferencesIntent, 3);            //Requestcode = 3 for settings
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void  onActivityResult(int requestCode, int resultCode, Intent result) {
        switch (requestCode) {
            case 1:                 //Add new event
                if (resultCode == RESULT_OK) {
                    String countryName = result.getStringExtra("country");
                    int visitYear = Integer.parseInt(result.getStringExtra("year"));

                    addNewEvent(visitYear, countryName);
                    Toast.makeText(MyCountriesCalender.this, "Event Created!", Toast.LENGTH_SHORT).show();
                }
                else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MyCountriesCalender.this, "Process cancelled", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:                 //Update an event
                if (resultCode == RESULT_OK) {
                    String countryName = result.getStringExtra("country");
                    int visitYear = Integer.parseInt(result.getStringExtra("year"));

                    updateEvent(getEventID(toUpdateEventName), visitYear, countryName);
                    Toast.makeText(MyCountriesCalender.this, "Event Updated!", Toast.LENGTH_SHORT).show();
                }
                else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(MyCountriesCalender.this, "Process cancelled", Toast.LENGTH_SHORT).show();
                }
                break;
            case 3:
                cursorAdapter.notifyDataSetChanged();
                initializeLoader();
                break;
        }
    }

    @Override
    public long getMyCountriesCalendarId() {

        long id;
        Cursor calCursor = getContentResolver().query(CALENDARS_LIST_URI, CALENDARS_LIST_PROJECTION, CALENDARS_LIST_SELECTION, CALENDARS_LIST_SELECTION_ARGS, null);

        if (!calCursor.moveToFirst()) {
            Uri uri = asSyncAdapter(CALENDARS_LIST_URI, ACCOUNT_TITLE, ACCOUNT_TYPE_LOCAL);

            ContentValues values = new ContentValues();
            values.put(Calendars.ACCOUNT_NAME, ACCOUNT_TITLE);
            values.put(Calendars.ACCOUNT_TYPE, ACCOUNT_TYPE_LOCAL);
            values.put(Calendars.NAME, CALENDAR_TITLE);
            values.put(Calendars.CALENDAR_DISPLAY_NAME, CALENDAR_TITLE);
            values.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
            values.put(Calendars.OWNER_ACCOUNT, ACCOUNT_TITLE);
            values.put(Calendars.VISIBLE, 1);
            values.put(Calendars.SYNC_EVENTS, 1);

            id = ContentUris.parseId(getContentResolver().insert(uri, values));
        } else
            id = calCursor.getLong(PROJ_CALENDARS_LIST_ID_INDEX);

        calCursor.close();
        return id;
    }

    private int getEventID(String eventTitle) {
        Uri eventUri = Uri.parse("content://com.android.calendar/events");

        int result = 0;
        String projection[] = { "_id", "title" };
        Cursor cursor = getContentResolver().query(eventUri, null, null, null, null);

        if (cursor.moveToFirst()) {
            String calName;
            String calID;

            int nameCol = cursor.getColumnIndex(projection[1]);
            int idCol = cursor.getColumnIndex(projection[0]);
            do {
                calName = cursor.getString(nameCol);
                calID = cursor.getString(idCol);

                if (calName != null && calName.contains(eventTitle))
                    result = Integer.parseInt(calID);

            } while (cursor.moveToNext());
            cursor.close();
        }
        return result;
    }

    @Override
    public void addNewEvent(int year, String country) {
        try {
            ContentValues entryValues = new ContentValues();
            entryValues.put(Events.CALENDAR_ID, getMyCountriesCalendarId());
            entryValues.put(Events.DTSTART, CalendarUtils.getEventStart(year));
            entryValues.put(Events.DTEND, CalendarUtils.getEventEnd(year));
            entryValues.put(Events.TITLE, (Character.toUpperCase(country.charAt(0)) + country.substring(1)));
            entryValues.put(Events.EVENT_TIMEZONE, CalendarUtils.getTimeZoneId());

            getContentResolver().insert(CalendarContract.Events.CONTENT_URI, entryValues);

            initializeLoader();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("exception", e.getMessage());
            Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void updateEvent(int eventId, int year, String country) {
        try {
            Uri eventUri = ContentUris.withAppendedId(EVENTS_LIST_URI, eventId);

            //adding the event properties
            ContentValues values = new ContentValues();
            values.put(Events.DTSTART, CalendarUtils.getEventStart(year));
            values.put(Events.DTEND, CalendarUtils.getEventEnd(year));
            values.put(Events.TITLE, country);

            getContentResolver().update(eventUri, values, null, null);

            initializeLoader();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("exception", e.getMessage());
            Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void deleteEvent(int eventId) {
        try{
            Uri eventUri = ContentUris.withAppendedId(EVENTS_LIST_URI, eventId);
            getContentResolver().delete(eventUri, null, null);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("exception", e.getMessage());
            Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        initializeLoader();
    }

    //--------------------------------- private helper methods --------------------

    /**
     * To apply the sorting method
     */
    private void initializeLoader() {
        Bundle bundle = new Bundle();
        bundle.putString("SortOrder", sharedPrefs.getString("sortSharedPref", null));

        if (getLoaderManager() != null)
            getLoaderManager().restartLoader(LOADER_MANAGER_ID, bundle, this);
        else
            getLoaderManager().initLoader(LOADER_MANAGER_ID, bundle, this);
    }

    private static Uri asSyncAdapter(Uri uri, String account, String accountType) {
        return uri.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,"true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
    }


    //-------------------- LoadManager methods -----------------------------
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle sortingBundle) {
        //If sort order is specified
        if(sortingBundle == null)
            //If sort order isn't specified, then the default order is ascending by title
            return new CursorLoader(this, EVENTS_LIST_URI, EVENTS_LIST_PROJECTION,
                    CalendarContract.Events.CALENDAR_ID + "=" + getMyCountriesCalendarId(),
                    null, Events.TITLE + " ASC");
        else {
            String str = sortingBundle.getString("SortOrder");
            CursorLoader crsLoader = new CursorLoader(this, EVENTS_LIST_URI, EVENTS_LIST_PROJECTION,
                    CalendarContract.Events.CALENDAR_ID + "=" + getMyCountriesCalendarId(),
                    null, sortingBundle.getString("SortOrder"));
            return crsLoader;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }


    //-------------------- SimpleCursorAdapter -------------------------------

    /**
     * A class solely for the custom-made SimpleCursorAdapter for bindView() due to the LoadManager
     * Created by hatem on 2017-07-04.
     */
    private class MySimpleCursAdapter extends SimpleCursorAdapter {

        private LayoutInflater cursorInflater;

        private MySimpleCursAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            cursorInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public void bindView(View view, final Context context, final Cursor cursor) {
            super.bindView(view, context, cursor);

            TextView tvName = (TextView) view.findViewById(R.id.tvName);
            TextView tvYear = (TextView) view.findViewById(R.id.tvYear);

            //Getting properties from cursor
            final String name = cursor.getString(cursor.getColumnIndexOrThrow(Events.TITLE));
            String year = String.valueOf(CalendarUtils.getEventYear(cursor.getLong(CalendarProviderClient.PROJ_EVENTS_LIST_DTSTART_INDEX)));

            //Adding the fields with properties
            tvName.setText(name);
            tvYear.setText(year);

            Button updateButton = (Button) view.findViewById(R.id.btUpdate);        //update event button
            Button deleteButton = (Button) view.findViewById(R.id.btDelete);        //delete event button

            //Getting settings properties from shared preferences
            boolean backgroundColor = sharedPrefs.getBoolean("backgroundColor", false);           //false is the default value
            boolean isBlackText = sharedPrefs.getBoolean("textColor", true);
            String textSize = sharedPrefs.getString("textSize", "Small");

            //For background color
            if (backgroundColor)
                countryListView.setBackgroundColor(Color.RED);
            else countryListView.setBackgroundColor(Color.WHITE);

            //Check if the text color is supposed to be black or blue from Shared Preferences
            if (isBlackText) {
                tvName.setTextColor(Color.BLACK);
                tvYear.setTextColor(Color.BLACK);
            } else  {
                tvName.setTextColor(Color.BLUE);
                tvYear.setTextColor(Color.BLUE);
            }

            //Apply the text size
            switch (textSize) {
                case "Small":
                    tvName.setTextSize(14);
                    tvYear.setTextSize(14);
                    break;
                case "Medium":
                    tvName.setTextSize(18);
                    tvYear.setTextSize(18);
                    break;
                case "Large":
                    tvName.setTextSize(24);
                    tvYear.setTextSize(24);
                    break;
            }

            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent updateIntent = new Intent(context, NewEvent.class);
                    toUpdateEventName = name;
                    startActivityForResult(updateIntent, 2);            //Requestcode = 2 for Update
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteEvent(getEventID(name));
                }
            });
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return cursorInflater.inflate(R.layout.countries_calendar_row, parent, false);
        }
    }


}
