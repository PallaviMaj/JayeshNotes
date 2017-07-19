package com.hourly.notes;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Show list of entries made till date
 */
public class ShowListActivity extends ListActivity {

    private static String LOG_TAG = "ShowListActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG,"onCreate ShowListActivity");
        setNextAlarm();
        try {
            showListOfNotes();
        }catch (IOException e){
            e.printStackTrace();
        }
//        if(!isServiceRunning("ShowListActivity")) {
//            //If service is not running, only then start it...
//            startService(new Intent(this, ShowListActivity.class));
//        }
    }
//    public boolean isServiceRunning(String serviceClassName){
//        ActivityManager activityManager = (ActivityManager)this.getSystemService(Activity.ACTIVITY_SERVICE);
//        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
//        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
//            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
//                return true;
//            }
//        }
//        return false;
//    }
    /**
     * Show list of com.hourly.com.hourly.notes available till date
     */
    public void showListOfNotes() throws IOException{
        File storageDir;
//        if (Utils.isExternalStorageWritable()) {
            storageDir= Environment.getExternalStorageDirectory();
//        }
//        else{
//            storageDir= new File(getFilesDir().getAbsolutePath());
//        }

        File directory = new File(storageDir, Utils.folderName);

        if (!directory.exists()) {
            return;
        }
        System.out.println("directory selected: "+ directory.getCanonicalPath());

        File[] files = directory.listFiles();
        if(files==null || files.length<1){
            Utils.generateNoteOnSD(ShowListActivity.this,Utils.logFileName, Utils.getCurrentTime() + LOG_TAG + " no files found ");
            TextView txt = new TextView(this);
            txt.setText("No files present");
            ListView.LayoutParams params=new ListView.LayoutParams
                    ((int) ViewGroup.LayoutParams.WRAP_CONTENT,(int) ViewGroup.LayoutParams.WRAP_CONTENT);
            txt.setLayoutParams(params);
            ListView lv = getListView();
            lv.setEmptyView(txt);
        } else {
            Log.d(LOG_TAG, "showListOfNotes: " + directory.getAbsolutePath());

            if(files!=null && files.length>0) {
                Arrays.sort(files);
            }
            List<String> fileNames= new ArrayList<String>();
            for (int i = 0; i < files.length; i++)
            {
                fileNames.add(files[i].getName());
            }
            setListAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, fileNames));
            ListView lv = getListView();
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position,
                                        long arg3) {
                    String value = (String) adapter.getItemAtPosition(position);
                    Log.d("Files", "Clicked Value: " + value);

                    File root = new File(Environment.getExternalStorageDirectory(), Utils.folderName);

                    if (!root.exists()) {
                        return;
                    }
                    String path = root.getAbsolutePath();
                    String strFilePath = path + File.separator + value;
                    File filePath = new File(strFilePath);
                    Uri uri = Uri.fromFile(filePath);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    // Text file
                    intent.setDataAndType(uri, "text/plain");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                }
            });
        }
    }


    /**
     * Set the next alarm time. current hour + 1
     */
    public void setNextAlarm(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        // Alarm 1hr each
        Date dat  = new Date();//initializes to now
        Calendar calendar = Calendar.getInstance();
        Calendar cal_now = Calendar.getInstance();
        cal_now.setTime(dat);
        calendar.setTime(dat);
        calendar.set(Calendar.HOUR_OF_DAY,10);//set the alarm time
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.SECOND,0);
        if(cal_now.after(calendar)){//if its in the past, set alarm for next hour
            Utils.generateNoteOnSD(ShowListActivity.this,Utils.logFileName, Utils.getCurrentTime() + LOG_TAG + " current time is  " +
                    sdf.format(cal_now.getTime())+ " current calendar: " +sdf.format(calendar.getTime()));
            calendar = cal_now;
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND,0);
            calendar.add(Calendar.HOUR_OF_DAY,1);

            Utils.generateNoteOnSD(ShowListActivity.this,Utils.logFileName, " alarm set to: "+sdf.format(calendar.getTime()));
        }
        Toast.makeText(ShowListActivity.this,"alarm set to " + sdf.format(calendar.getTime()),Toast.LENGTH_LONG).show();
//        Intent intent1 = new Intent(ShowListActivity.this, AlarmReceiver.class);
        Intent intent1 = new Intent("com.hourly.com.hourly.com.hourly.notes.ActivityRecognition.RestartSensor");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ShowListActivity.this, 0,intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) ShowListActivity.this.getSystemService(ShowListActivity.this.ALARM_SERVICE);
        am.set(AlarmManager.RTC,calendar.getTimeInMillis(),pendingIntent);
    }
}
