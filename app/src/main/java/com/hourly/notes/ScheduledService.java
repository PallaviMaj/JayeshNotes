package com.hourly.notes;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by pallavi on 15-05-2017.
 */

public class ScheduledService extends IntentService {
    private static final int PERIOD=60000*2;
    private static final String LOG_TAG ="ScheduledService";
    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;

    NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public ScheduledService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        sendNotification();
        // Release the wake lock provided by the BroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);
    }

    //    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        Toast.makeText(ScheduledService.this, "onBind", Toast.LENGTH_LONG).show();
//        Utils.generateNoteOnSD(ScheduledService.this,Utils.logFileName,Utils.getCurrentTime() + LOG_TAG + " onBind ");
//        Log.d(LOG_TAG,"onBind");
//        return null;
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        //TODO: every 1 hr start notification
//        Log.d(LOG_TAG,"onStartCommand");
//        Utils.generateNoteOnSD(ScheduledService.this,Utils.logFileName, Utils.getCurrentTime() + LOG_TAG + " onStartCommand ");
//        Toast.makeText(ScheduledService.this, "onStartCommand", Toast.LENGTH_LONG).show();
//        setNextAlarm();
//        return START_STICKY;
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.generateNoteOnSD(ScheduledService.this,Utils.logFileName, Utils.getCurrentTime() + LOG_TAG + " on destroy ");
        Intent broadcastIntent = new Intent("com.hourly.com.hourly.com.hourly.notes.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);
    }

    /**
     * Set the next alarm time. current hour + 1
     */
    public void setNextAlarm(){
        // Alarm 1hr each
        Date dat  = new Date();//initializes to now
        Calendar calendar = Calendar.getInstance();
        Calendar cal_now = Calendar.getInstance();
        cal_now.setTime(dat);
        calendar.setTime(dat);
        calendar.set(Calendar.HOUR_OF_DAY,10);//set the alarm time
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.SECOND,0);
        if(calendar.before(cal_now)){//if its in the past, set alarm for next hour
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
            Utils.generateNoteOnSD(ScheduledService.this,Utils.logFileName, Utils.getCurrentTime() + LOG_TAG + " current time is  " +
                    sdf.format(cal_now.getTime())+ " current calendar: " +sdf.format(calendar.getTime()));
            calendar = cal_now;
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND,0);
            calendar.add(Calendar.HOUR_OF_DAY,1);

            Utils.generateNoteOnSD(ScheduledService.this,Utils.logFileName, " alarm set to: "+sdf.format(calendar.getTime()));
        }
        Intent intent1 = new Intent(ScheduledService.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ScheduledService.this, 0,intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) ScheduledService.this.getSystemService(ScheduledService.this.ALARM_SERVICE);
        am.set(AlarmManager.RTC,calendar.getTimeInMillis(),pendingIntent);
    }


    // Post a notification indicating whether a doodle was found.
    private void sendNotification() {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, TakeNotesWindow.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.timesheet)
                        .setContentTitle(getString(R.string.timesheet_title))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(getString(R.string.fill_timesheet)))
                        .setContentText(getString(R.string.fill_timesheet));

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}
