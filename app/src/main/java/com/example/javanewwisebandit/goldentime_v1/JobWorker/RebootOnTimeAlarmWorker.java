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
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesDateTimeProcess;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;

import java.util.Calendar;

//import kr.ac.kaist.jypark.goldentime_v1.Receiver.AlarmReceiver;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesDateTimeProcess;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;

public class RebootOnTimeAlarmWorker extends Worker {

    public RebootOnTimeAlarmWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        int nextHour = UtilitiesDateTimeProcess.getCurrentTimeHour()+1;
        int endHour = nextHour+24;

        int hourOfTheMin = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(),"updateMin");
        int hourOfTheSec = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(),"updateSec");
        int ustatsHourOfTheMin = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(),"updateUsageStatsMin");
        int ustatsHourOfTheSec = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(),"updateUsageStatsSec");

        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        /** 폰 재부팅 후 정시 타이머 알람 체크 및 재등록 **/
        int tmpHour=0;
        for(int i=nextHour;i<endHour;i++) {
            if(i >=24)   tmpHour = i-24;
            else         tmpHour = i;

            String requestCodeStr = tmpHour+"0000";
            int requestCode = Integer.parseInt(requestCodeStr);
            setupAlarmZoneForOnTimeAlarm(intent, requestCode, tmpHour, "ALARM_ONTIMER");
            Log.w("AA", tmpHour+"시 정시 알람을 재등록 합니다.");
        }

        /** 폰 재부팅 후 UsageStats 업데이트 알람 체크 및 재등록 **/
        for(int i=nextHour;i<endHour;i++) {
            if(i >=24)   tmpHour = i-24;
            else         tmpHour = i;

            String requestCodeStr = tmpHour+""+ustatsHourOfTheMin+""+ustatsHourOfTheSec;
            int  requestCode = Integer.parseInt(requestCodeStr);
            setupAlarmZoneForDBUpdate(intent, requestCode, tmpHour, ustatsHourOfTheMin, ustatsHourOfTheSec, "ALARM_USAGESTATS_DBUPDATE");
            Log.w("AA", tmpHour+"시 UsageStats 알람을 재등록 합니다.");
        }

        /** 폰 재부팅 후 정시 DB업데이트 알람 체크 및 재등록 **/
        for(int i=nextHour;i<endHour;i++) {
            if(i >=24)   tmpHour = i-24;
            else         tmpHour = i;

            String requestCodeStr = tmpHour+""+hourOfTheMin+""+hourOfTheSec;
            int  requestCode = Integer.parseInt(requestCodeStr);
            setupAlarmZoneForDBUpdate(intent, requestCode, tmpHour, hourOfTheMin, hourOfTheSec, "ALARM_ONTIME_DBUPDATE");
            Log.w("AA", tmpHour+"시 DB 업데이트 알람을 재등록 합니다.");
        }

        /** 폰 재부팅 후 베이스라인 -> 중재 앱 전환 날짜 재등록 **/
        boolean isAppChanged = UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getApplicationContext(),"isAppChange");
        if(!isAppChanged)   setupAppChangeDateAlarm(intent, 1000, "ALARM_APPCHANGE");

        return Result.success();
    }

    private void setupAlarmZoneForOnTimeAlarm(Intent intent, int requestCode, int hourOfTheDay, String actionStr) {
        // 알람매니저 설정
        AlarmManager alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        intent.setAction(actionStr);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Initialize the calendar with today and the preferred time to run the job.
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, hourOfTheDay);
        alarmTime.set(Calendar.MINUTE, 0);
        alarmTime.set(Calendar.SECOND, 0);
        long timeInMillis = alarmTime.getTimeInMillis();
        if(timeInMillis - System.currentTimeMillis() < 0) timeInMillis += 86400000;

        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, alarmIntent);
    }

    private void setupAppChangeDateAlarm(Intent intent, int requestCode, String actionStr) {
        // 알람매니저 설정
        AlarmManager  alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        intent.setAction(actionStr);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        /**처음 앱 설치 시 설정되었던 중재 앱 전환 날짜(밀리초 단위) 가져오기 **/
        long timeInMillis = UtilitiesSharedPrefDataProcess.getLongDataToSharedPref(getApplicationContext(),"interventionDateTime");
        timeInMillis = checkAlarmExpired(timeInMillis);
        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, alarmIntent);
    }

    private void setupAlarmZoneForDBUpdate(Intent intent, int requestCode, int hourOfTheDay, int hourOfTheMin, int hourOfTheSec, String actionStr) {
        // 알람매니저 설정
        AlarmManager  alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        intent.setAction(actionStr);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getApplicationContext(), requestCode, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Initialize the calendar with today and the preferred time to run the job.
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, hourOfTheDay);
        alarmTime.set(Calendar.MINUTE, hourOfTheMin);
        alarmTime.set(Calendar.SECOND, hourOfTheSec);
        long timeInMillis = alarmTime.getTimeInMillis();
        if(timeInMillis - System.currentTimeMillis() < 0) timeInMillis += 86400000;

        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, alarmIntent);
    }

    /** 폰 껐다 켰는데 중재앱 변환시점을 지나가 버린 경우에 대한 예외처리:  1시간 이후로 재설정 **/
    private long checkAlarmExpired(long timeInMillis) {
        if(timeInMillis - System.currentTimeMillis() < 0) {
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.set(Calendar.HOUR_OF_DAY, UtilitiesDateTimeProcess.getCurrentTimeHour()+1);
            alarmTime.set(Calendar.MINUTE, 0);
            alarmTime.set(Calendar.SECOND, 0);
            timeInMillis = alarmTime.getTimeInMillis();
            return  timeInMillis;
        }
        else    return timeInMillis;
    }

}
