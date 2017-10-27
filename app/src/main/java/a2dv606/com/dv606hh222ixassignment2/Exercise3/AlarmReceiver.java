package a2dv606.com.dv606hh222ixassignment2.Exercise3;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

/**
 *
 * Created by hatem on 2016-09-14.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String alarmTime = intent.getExtras().getString("time");

        String state = intent.getExtras().getString("extra");
        Log.e("MY ACTIVITY", "In the receiver with " + state);

        Intent i = new Intent(context, Alarm.class);
        i.putExtra("time", alarmTime);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

}
