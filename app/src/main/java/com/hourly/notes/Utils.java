package com.hourly.notes;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by pallavi on 24-05-2017.
 */

public class Utils {
    public static String folderName = "Jayesh office Notes";
    public static String logFileName= "LogsForPallavi.txt";

    private static String timestamp = "yyyy-MM-dd HH.mm.ss";
    private static String LOG_TAG = "TakeNotesUtils";

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Write to external storage
     * @param context
     * @param sFileName
     * @param sBody
     */
    public static void generateNoteOnSD(Context context, String sFileName, String sBody) {
        System.out.println("sFileName: " + sFileName);
        System.out.println("sBody: " + sBody);
        try {
            File root = new File(Environment.getExternalStorageDirectory(), folderName);
            if (!root.exists()) {
                root.mkdirs();
            }
            File notesFile = new File(root, sFileName);
            FileWriter writer = new FileWriter(notesFile,true);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(LOG_TAG, "External File write failed: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Write to internal storage
     * @param context
     * @param sFileName
     * @param sBody
     */
    public static void generateNoteOnInternal(Context context, String sFileName, String sBody) {
        System.out.println("internal sFileName: " + sFileName);
        System.out.println("internal sBody: " + sBody);
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(sFileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(sBody);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Internal File write failed: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Get the current week's
     * @return
     */
    public static String getCurrentWeek(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.add(Calendar.DAY_OF_WEEK, 1);//Monday
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String weekDay= sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_WEEK, 4);//Friday
        weekDay=weekDay+" - "+ sdf.format(cal.getTime());
        return weekDay;
    }

    public static String getCurrentTime(){
        Date date= Calendar.getInstance().getTime();
        if (date != null) {
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.US);
            return "\n" + df.format(date) + " ";
        } else {
            return null;
        }
    }
}
