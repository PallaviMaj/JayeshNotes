package com.hourly.notes;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.hourly.notes.R;
import com.hourly.notes.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by pallavi on 15-05-2017.
 */
public class TakeNotesWindow extends Activity{
    private PopupWindow popUpWindow;
    private Button btnClickSave;
    private EditText edtMsg;
    private static String LOG_TAG = "TakeNotes";

    private static final int START_HOUR = 7;//7am
    private static final int END_HOUR = 17;//5pm

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_note);

        setNextAlarm();
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < START_HOUR && hour > END_HOUR){
            Utils.generateNoteOnSD(this,Utils.logFileName,Utils.getCurrentTime() + LOG_TAG + " onCreate ");
            finish(); //we do not need to do anything in this case.
        }

        btnClickSave = (Button)findViewById(R.id.btnSave);
        btnClickSave.setOnClickListener(new MyListener());

        edtMsg = (EditText) findViewById(R.id.edtNote);
    }

    /**
     * Add listener
     */
    public class MyListener implements View.OnClickListener {
        public MyListener() {
            super();
        }

        @Override
        public void onClick (View v) {
            switch (v.getId()){
                case R.id.btnSave:
                    {
                        //TODO: write to file and save in list.
                        String fileName = Utils.getCurrentWeek()+".txt";

                        String notes= edtMsg.getText().toString();
                        if(notes.trim().length()<1){
                            //Note is blank
                            Log.d(LOG_TAG, "no data to save");
                            Utils.generateNoteOnSD(TakeNotesWindow.this,Utils.logFileName,Utils.getCurrentWeek() + LOG_TAG + " no data to save ");
                        }else {
                            Utils.generateNoteOnSD(TakeNotesWindow.this,Utils.logFileName,Utils.getCurrentWeek() + LOG_TAG + " data saved ");
                            String note =  "\n------\n" + Utils.getCurrentTime() + "\n" + notes;
                            if (Utils.isExternalStorageWritable()) {
                                //External storage has write access.
                                Utils.generateNoteOnSD(v.getContext(), fileName, note);
                            } else {
                                //If no write permission, write on internal storage
                                Utils.generateNoteOnInternal(v.getContext(), fileName, note);
                            }
                        }
                    }
                    finish();
                    break;
            }
        }
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
            Utils.generateNoteOnSD(TakeNotesWindow.this,Utils.logFileName, Utils.getCurrentTime() + LOG_TAG + " current time is  " +
                    sdf.format(cal_now.getTime())+ " current calendar: " +sdf.format(calendar.getTime()));
            calendar = cal_now;
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND,0);
            calendar.add(Calendar.HOUR_OF_DAY,1);

            Utils.generateNoteOnSD(TakeNotesWindow.this,Utils.logFileName, " alarm set to: "+sdf.format(calendar.getTime()));
        }
//        Intent intent1 = new Intent(TakeNotesWindow.this, AlarmReceiver.class);
        Intent intent1 = new Intent("com.hourly.com.hourly.com.hourly.notes.ActivityRecognition.RestartSensor");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(TakeNotesWindow.this, 0,intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) TakeNotesWindow.this.getSystemService(TakeNotesWindow.this.ALARM_SERVICE);
        am.set(AlarmManager.RTC,calendar.getTimeInMillis(),pendingIntent);
    }
}
