package a2dv606.com.dv606hh222ixassignment2.Exercise1;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;

/**
 * Interface for Calendar Provider clients for MyCountries exercise
 * in 2DV606 Assignment 2.
 *
 * Information:
 * http://developer.android.com/guide/topics/providers/content-provider-basics.html
 * http://developer.android.com/guide/topics/providers/calendar-provider.html
 * http://developer.android.com/reference/android/content/ContentResolver.html
 * http://developer.android.com/reference/android/provider/CalendarContract.Calendars.html
 * http://developer.android.com/guide/components/loaders.html
 * http://developer.android.com/training/load-data-background/setup-loader.html
 * http://developer.android.com/reference/android/content/CursorLoader.html
 *
 * Created by Kostiantyn Kucher on 09/09/2015.
 * Last modified by Kostiantyn Kucher on 10/09/2015.
 */
public interface CalendarProviderClient extends LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Account title used for this specific calendar throughout the system.
     * You are expected to use this value for the Calendars.ACCOUNT_NAME and
     * Calendars.OWNER_ACCOUNT field when creating the calendar
     */
    String ACCOUNT_TITLE = "MyCountriesAccount";

    /**
     * Title used for this specific calendar throughout the system.
     * You are expected to use this value for the Calendars.NAME field when creating / accessing
     * the calendar (you may also use it for the Calendars.DISPLAY_NAME field)
     */
    String CALENDAR_TITLE = "MyCountries";

    /**
     * Content URI used to access the calendar(s)
     */
    Uri CALENDARS_LIST_URI = CalendarContract.Calendars.CONTENT_URI;

    /**
     * Selection used when querying for MyCountries calendar
     */
    String CALENDARS_LIST_SELECTION = "("
            + "(" + CalendarContract.Calendars.NAME + " = ? )" + " AND "
            + "(" + CalendarContract.Calendars.ACCOUNT_TYPE + " = ? )" + ")";

    /**
     * Selection args used when querying for MyCountries calendar
     */
    String[] CALENDARS_LIST_SELECTION_ARGS = new String[] {
            CALENDAR_TITLE,
            CalendarContract.ACCOUNT_TYPE_LOCAL
    };

    /**
     * List of columns to include when querying for MyCountries calendar.
     * We really need only the ID field, but you may want to
     * add additional columns for debug purposes.
     */
    String[] CALENDARS_LIST_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.NAME
    };

    /**
     * Column indices for the projection array above
     */
    int PROJ_CALENDARS_LIST_ID_INDEX = 0;
    int PROJ_CALENDARS_LIST_NAME_INDEX = 1;


    /**
     * Content URI used to access the calendar event(s)
     */
    Uri EVENTS_LIST_URI = CalendarContract.Events.CONTENT_URI;

    /**
     * List of columns to include when querying for MyCountries calendar events.
     * You should store the country name in TITLE field. The year of the visit should be converted
     * to DTSTART / DTEND values using the helper functions from the CalendarUtils class.
     * When querying for events, call the CalendarUtils.getEventYear() helper function
     * with the DTSTART field value.
     */
    String[] EVENTS_LIST_PROJECTION = new String[] {
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART
    };

    /**
     * Column indices for the projection array above
     */
    int PROJ_EVENTS_LIST_ID_INDEX = 0;
    int PROJ_EVENTS_LIST_TITLE_INDEX = 1;
    int PROJ_EVENTS_LIST_DTSTART_INDEX = 2;

    /**
     * An arbitrary ID to use for the Loader Manager
     */
    int LOADER_MANAGER_ID = 0;

    /**
     * Returns the ID of MyCountries calendar.
     * If the calendar is missing (e.g., during the first application launch), it must be created.
     * You should call this method already from your activity's onCreate() (it seems
     * that it is not possible for a user to remove the calendar manually, so calling this method
     * from onResume() seems to be an overkill).
     *
     * Remember to use the proper NAME field value and ACCOUNT_TYPE value of ACCOUNT_TYPE_LOCAL!
     * More information here: http://developer.android.com/reference/android/provider/CalendarContract.Calendars.html
     *
     * To create a calendar, you will need to issue a query as a Sync Adapter – you will need
     * to specify ACCOUNT_NAME and ACCOUNT_TYPE not only in provided values, but also in the URI:
     * http://developer.android.com/guide/topics/providers/calendar-provider.html#sync-adapter
     *
     * @return calendar ID
     */
    long getMyCountriesCalendarId();


    /**
     * Uses a Content Resolver to create a new calendar event.
     * You should call this method when handling a new data entry creation – use your code from
     * Assignment 1 to parse & validate user input, etc.
     *
     * The implementation of this method should call ContentResolver.insert() with
     * EVENTS_LIST_URI and values required by the Calendar Provider (use CalendarUtils methods
     * to get DTSTART, DTEND and EVENT_TIMEZONE values):
     * http://developer.android.com/guide/topics/providers/calendar-provider.html#add-event
     *
     * After you add an event, you should restart the Loader Manager from your activity to
     * get the updated data:
     * loaderManager.restartLoader(LOADER_MANAGER_ID, null, this)
     *
     * @param year country visit year
     * @param country visited country name
     */
    void addNewEvent(int year, String country);

    /**
     * Uses a Content Resolver to update a calendar event.
     * You should call this method when handling an entry update – use your code from
     * Assignment 1 to parse & validate user input, etc.
     *
     * The implementation of this method should call ContentResolver.update() with
     * an eligible URI and values required by the Calendar Provider (you will need to update only
     * the DTSTART, DTEND and TITLE fields):
     * http://developer.android.com/guide/topics/providers/calendar-provider.html#update-event
     *
     * The URI used for update should be constructed by calling
     * ContentUris.withAppendedId(EVENTS_LIST_URI, eventId)
     *
     * After you update an event, you should restart the Loader Manager from your activity to
     * get the updated data:
     * loaderManager.restartLoader(LOADER_MANAGER_ID, null, this)
     *
     * @param eventId event ID
     * @param year country visit year
     * @param country visited country name
     */
    void updateEvent(int eventId, int year, String country);

    /**
     * Uses a Content Resolver to delete a calendar event.
     * You should call this method when deleting an entry.
     *
     * The implementation of this method should call ContentResolver.delete() with
     * an eligible URI:
     * http://developer.android.com/guide/topics/providers/calendar-provider.html#delete-event
     *
     * The URI used for deletion should be constructed by calling
     * ContentUris.withAppendedId(EVENTS_LIST_URI, eventId)
     *
     * After you delete an event, you should restart the Loader Manager from your activity to
     * get the updated data:
     * loaderManager.restartLoader(LOADER_MANAGER_ID, null, this)
     *
     * @param eventId event ID
     */
    void deleteEvent(int eventId);


    /**
     * The Loader Manager callback that returns a CursorLoader object.
     * You must create an instance of CursorLoader that would query the Calendar Provider
     * here.
     *
     * @param id ID used to manage several loaders; can be ignored for this assignment
     * @param args additional arguments; can probably be ignored for this assignment
     * @return a CursorLoader that will be used to query and update the provider data
     */
    Loader<Cursor> onCreateLoader(int id, Bundle args);


    /**
     * The Loader Manager callback that signals the end of the load.
     * You must use the cursor available as the second argument here to access the data
     * (the simplest way is to call yourSimpleCursorAdapter.swapCursor(data) ).
     *
     * @param loader the actual loader object
     * @param data the data cursor
     */
    void onLoadFinished(Loader<Cursor> loader, Cursor data);


    /**
     * The Loader Manager callback that signals the reset of the loader.
     * You must remove the reference to the old data here
     * (the simplest way is to call yourSimpleCursorAdapter.swapCursor(null) ).
     *
     * @param loader the actual loader object
     */
    void onLoaderReset(Loader<Cursor> loader);

}
