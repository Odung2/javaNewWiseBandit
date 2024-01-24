package com.example.javanewwisebandit.goldentime_v1.JobWorker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.javanewwisebandit.goldentime_v1.Receiver.AlarmReceiver;

import java.util.Calendar;

//import kr.ac.kaist.jypark.goldentime_v1.Receiver.AlarmReceiver;

public class OnTimeAlarmWorker extends Worker {

    public OnTimeAlarmWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        int startIdx = 0;
        int endIdx = 24;
        for(int i=startIdx;i<endIdx;i++) {
            /* 이미 알람이 등록되어 있는지 체크 */
            Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
            String requestCodeStr = i+"0000";
            int  requestCode = Integer.parseInt(requestCodeStr);
            boolean alarmUp = (PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE) != null);
            if(!alarmUp)    setupAlarmZone(intent, requestCode, i);

            //if(!alarmUp && i == 0)    setupAlarmZone(intent, requestCode);
        }

        return Result.success();
    }

    private void setupAlarmZone(Intent intent, int requestCode, int hourOfTheDay) {
        // 알람매니저 설정
        AlarmManager  alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        intent.setAction("ALARM_ONTIMER");
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Initialize the calendar with today and the preferred time to run the job.
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, hourOfTheDay);
        alarmTime.set(Calendar.MINUTE, 0);
        alarmTime.set(Calendar.SECOND, 0);
        long timeInMillis = alarmTime.getTimeInMillis();
        if(timeInMillis - System.currentTimeMillis() < 0) timeInMillis += 86400000;

        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, alarmIntent);
        Log.w("AA", hourOfTheDay+"시 정시 알람을 등록합니다.");
    }

    //TODO: for debugging
    private void setupAlarmZone(Intent intent, int requestCode) {
        // 알람매니저 설정
        AlarmManager  alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        intent.setAction("ALARM_ONTIMER");
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Initialize the calendar with today and the preferred time to run the job.
        Calendar alarmTime = Calendar.getInstance();
        long timeInMillis = alarmTime.getTimeInMillis() + 10000;

        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, alarmIntent);
        Log.w("AA", "10초 후 알람을 등록합니다.");
    }
}
