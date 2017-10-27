package a2dv606.com.dv606hh222ixassignment2.Exercise1;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Several helper methods to use in the MyCountriesCalendar exercise
 *
 * Created by Kostiantyn Kucher on 09/09/2015.
 * Last modified by Kostiantyn Kucher on 10/09/2015.
 */
public class CalendarUtils {

    /**
     * Returns the event start/end value for calendar based on provided year value
     *
     * @param year country visit value
     * @param isStart flag specifying if the event start value should be returned
     * @return event start/end value as number of milliseconds since UTC epoch start
     */
    protected static long getEventValue(int year, boolean isStart) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, Calendar.JANUARY, 1, 12, (isStart ? 0 : 30));
        return c.getTimeInMillis();
    }

    /**
     * Returns the event start value for calendar based on provided year value
     *
     * @param year country visit value
     * @return event start value as number of milliseconds since UTC epoch start
     */
    public static long getEventStart(int year) {
        return getEventValue(year, true);
    }

    /**
     * Returns the event end value for calendar based on provided year value
     *
     * @param year country visit year value
     * @return event end value as number of milliseconds since UTC epoch start
     */
    public static long getEventEnd(int year) {
        return getEventValue(year, false);
    }

    /**
     * Returns the year value corresponding to the provided milliseconds value
     *
     * @param millis number of milliseconds since UTC epoch start
     * @return country visit year value
     */
    public static int getEventYear(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return c.get(Calendar.YEAR);
    }

    /**
     * Returns the default time zone ID for calendar
     *
     * @return default time zone ID
     */
    public static String getTimeZoneId() {
        return TimeZone.getDefault().getID();
    }

}
