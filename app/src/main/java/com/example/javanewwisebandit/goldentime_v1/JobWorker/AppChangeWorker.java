package com.example.javanewwisebandit.goldentime_v1.JobWorker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.javanewwisebandit.goldentime_v1.Receiver.AlarmReceiver;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesDateTimeProcess;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;

import java.util.Calendar;

//import kr.ac.kaist.jypark.goldentime_v1.Receiver.AlarmReceiver;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesDateTimeProcess;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;

public class AppChangeWorker extends Worker {

    public AppChangeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        setupAlarmZone();

        return Result.success();
    }

    private void setupAlarmZone() {
        // 알람매니저 설정
        AlarmManager  alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.setAction("ALARM_APPCHANGE");
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), 1000, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Initialize the calendar with today and the preferred time to run the job.
        Calendar alarmTime = Calendar.getInstance();
        int hourOfTheDay = UtilitiesDateTimeProcess.getCurrentTimeHour()+1;
        alarmTime.set(Calendar.HOUR_OF_DAY, hourOfTheDay);
        alarmTime.set(Calendar.MINUTE, 3);
        alarmTime.set(Calendar.SECOND, 0);
        alarmTime.add(Calendar.DAY_OF_MONTH, 0);
        long timeInMillis = alarmTime.getTimeInMillis();
        //timeInMillis += 0;   //1주일 후: 604800000, 1일 후: 86400000
        /**SharedPref 변수에 저장**/
        UtilitiesSharedPrefDataProcess.setLongDataToSharedPref(getApplicationContext(),"interventionDateTime", timeInMillis);

        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, alarmIntent);
    }

}