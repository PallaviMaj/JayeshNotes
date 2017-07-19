package com.hourly.notes;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by pallavi on 30-05-2017.
 */

public class AlarmReceiver extends WakefulBroadcastReceiver {
    private static int MID = 0;
    private static final String LOG_TAG ="AlarmReceiver";

    private static String RESTART_ACTION = "com.hourly.com.hourly.com.hourly.notes.ActivityRecognition.RestartSensor";
    // The app's AlarmManager, which provides access to the system alarm services.
    private AlarmManager alarmMgr;
    // The pending intent that is triggered when the alarm fires.
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context, "AlarmReceiver onReceive", Toast.LENGTH_LONG).show();

        Utils.generateNoteOnSD(context, Utils.logFileName, Calendar.getInstance().getTime() + LOG_TAG + " AlarmReceiver onReceive, action: " + intent.getAction());

        Intent service = new Intent(context, ScheduledService.class);

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, service);
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            setAlarm(context);
            //Need to start service now that the service was killed
//            Utils.generateNoteOnSD(context,Utils.logFileName, Utils.getCurrentTime() + LOG_TAG + " restart service ");
//            context.startService(new Intent(context, ScheduledService.class));
        }else {

            long when = System.currentTimeMillis();

            Calendar calendar = Calendar.getInstance();
//            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
//                Utils.generateNoteOnSD(context, Utils.logFileName, Calendar.getInstance().getTime() + " alarm on weekend " + calendar.get(Calendar.HOUR_OF_DAY));
//                return;
//            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");

            Calendar calendar_before8 = Calendar.getInstance();
            calendar_before8.set(Calendar.HOUR_OF_DAY,8);
            calendar_before8.set(Calendar.MINUTE,0);
            calendar_before8.set(Calendar.SECOND,0);

            Calendar calendar_after21 = Calendar.getInstance();
            calendar_after21.set(Calendar.HOUR_OF_DAY,21);
            calendar_after21.set(Calendar.MINUTE,0);
            calendar_after21.set(Calendar.SECOND,0);
            Utils.generateNoteOnSD(context, Utils.logFileName, Calendar.getInstance().getTime() + LOG_TAG + "  Current date: " + sdf.format(calendar.getTime())+" ; calendar b4 8: " +sdf.format(calendar_before8.getTime()) + " ; after 21: " + sdf.format(calendar_after21.getTime()) );
            if (calendar.before(calendar_before8) ){
                Utils.generateNoteOnSD(context, Utils.logFileName, Calendar.getInstance().getTime() + LOG_TAG + " missed b4 alarm hour: " + calendar.get(Calendar.HOUR_OF_DAY));
                return;
            }
            if(calendar.after(calendar_after21)) {
                Utils.generateNoteOnSD(context, Utils.logFileName, Calendar.getInstance().getTime() + LOG_TAG + " missed after alarm hour: " + calendar.get(Calendar.HOUR_OF_DAY));
                return;
            }
            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            Intent notificationIntent = new Intent(context, TakeNotesWindow.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(
                    context).setSmallIcon(R.drawable.timesheet)
                    .setContentTitle("Timesheet")
                    .setContentText("Fill up timesheets").setSound(alarmSound)
                    .setAutoCancel(true).setWhen(when)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
            notificationManager.notify(MID, mNotifyBuilder.build());
            MID++;
        }
    }


    // BEGIN_INCLUDE(set_alarm)
    /**
     * Sets a repeating alarm that runs once a day at approximately 8:30 a.m. When the
     * alarm fires, the app broadcasts an Intent to this WakefulBroadcastReceiver.
     * @param context
     */
    public void setAlarm(Context context) {
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // Set the alarm's trigger time to 8:30 a.m.
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.SECOND, 00);

        /*
         * If you don't have precise time requirements, use an inexact repeating alarm
         * the minimize the drain on the device battery.
         *
         * The call below specifies the alarm type, the trigger time, the interval at
         * which the alarm is fired, and the alarm's associated PendingIntent.
         * It uses the alarm type RTC_WAKEUP ("Real Time Clock" wake up), which wakes up
         * the device and triggers the alarm according to the time of the device's clock.
         *
         * Alternatively, you can use the alarm type ELAPSED_REALTIME_WAKEUP to trigger
         * an alarm based on how much time has elapsed since the device was booted. This
         * is the preferred choice if your alarm is based on elapsed time--for example, if
         * you simply want your alarm to fire every 60 minutes. You only need to use
         * RTC_WAKEUP if you want your alarm to fire at a particular date/time. Remember
         * that clock-based time may not translate well to other locales, and that your
         * app's behavior could be affected by the user changing the device's time setting.
         *
         * Here are some examples of ELAPSED_REALTIME_WAKEUP:
         *
         * // Wake up the device to fire a one-time alarm in one minute.
         * alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
         *         SystemClock.elapsedRealtime() +
         *         60*1000, alarmIntent);
         *
         * // Wake up the device to fire the alarm in 30 minutes, and every 30 minutes
         * // after that.
         * alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
         *         AlarmManager.INTERVAL_HALF_HOUR,
         *         AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);
         */

        // Set the alarm to fire at approximately 8:30 a.m., according to the device's
        // clock, and to repeat once a day.
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), 10*60*1000/*AlarmManager.INTERVAL_HOUR*/, alarmIntent);

        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, AlarmManager.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
}
