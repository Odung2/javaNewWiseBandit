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
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;

import java.util.Calendar;

//import kr.ac.kaist.jypark.goldentime_v1.Receiver.AlarmReceiver;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;

public class OnTimeDBUpdateWorker extends Worker {

    public OnTimeDBUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        int hourOfTheMin = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(),"updateMin");
        int hourOfTheSec = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(),"updateSec");

        for(int i=0;i<24;i++) {
            /* 이미 알람이 등록되어 있는지 체크 */
            Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
            String requestCodeStr = i+""+hourOfTheMin+""+hourOfTheSec;
            int  requestCode = Integer.parseInt(requestCodeStr);
            boolean alarmUp = (PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE) != null);
            if(!alarmUp)    setupAlarmZone(intent, requestCode, i, hourOfTheMin, hourOfTheSec);
        }

        return Result.success();
    }

    private void setupAlarmZone(Intent intent, int requestCode, int hourOfTheDay, int hourOfTheMin, int hourOfTheSec) {
        // 알람매니저 설정
        AlarmManager  alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        intent.setAction("ALARM_ONTIME_DBUPDATE");
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Initialize the calendar with today and the preferred time to run the job.
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, hourOfTheDay);
        alarmTime.set(Calendar.MINUTE, hourOfTheMin);
        alarmTime.set(Calendar.SECOND, hourOfTheSec);
        long timeInMillis = alarmTime.getTimeInMillis();
        if(timeInMillis - System.currentTimeMillis() < 0) timeInMillis += 86400000;

        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, alarmIntent);
        Log.w("AA", hourOfTheDay+"시 DB 업데이트 알람을 등록합니다.");
    }
}
