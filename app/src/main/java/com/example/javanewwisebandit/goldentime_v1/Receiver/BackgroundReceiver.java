package com.example.javanewwisebandit.goldentime_v1.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.javanewwisebandit.goldentime_v1.JobWorker.RebootOnTimeAlarmWorker;
import com.example.javanewwisebandit.goldentime_v1.Service.OnTimeService;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesDateTimeProcess;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;
//
//import kr.ac.kaist.jypark.goldentime_v1.JobWorker.RebootOnTimeAlarmWorker;
//import kr.ac.kaist.jypark.goldentime_v1.Service.OnTimeService;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesDateTimeProcess;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;

public class BackgroundReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Log.i("ScreenONOFFReceiver", "Screen UnLock..................!!");
            UtilitiesSharedPrefDataProcess.saveEventToSharedPref(context,"Unlock");
            Intent serviceIntent = new Intent(context, OnTimeService.class);
            serviceIntent.setAction(OnTimeService.ACTION_SCREEN_UNLOCK_SERVICE);
            context.startForegroundService(serviceIntent);

        }
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.i("ScreenONOFFReceiver", "Screen ON..................!!");
            UtilitiesSharedPrefDataProcess.saveEventToSharedPref(context,"Screen On");
        }
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.i("ScreenONOFFReceiver", "Screen OFF..................!!");
            UtilitiesSharedPrefDataProcess.saveEventToSharedPref(context,"Screen Off");
            Intent serviceIntent = new Intent(context, OnTimeService.class);
            serviceIntent.setAction(OnTimeService.ACTION_SCREEN_OFF_SERVICE);
            context.startForegroundService(serviceIntent);
        }
        else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
            UtilitiesSharedPrefDataProcess.saveEventToSharedPref(context,"Power Off");
            UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(context, "lastTimeSlot", UtilitiesDateTimeProcess.getCurrentTimeHour());

        }
        else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) || intent.getAction().equals(Intent.ACTION_LOCKED_BOOT_COMPLETED)) {
            UtilitiesSharedPrefDataProcess.saveEventToSharedPref(context,"Power On");

            /* Job Worker에 정시타이머 알람을 새롭게 등록 */
            OneTimeWorkRequest oneTimeWorkRequest1    = new OneTimeWorkRequest.Builder(RebootOnTimeAlarmWorker.class).build();
            WorkManager.getInstance(context).enqueue(oneTimeWorkRequest1);

            /* 정시 타이머 서비스 실행 */
            Intent serviceIntent = new Intent(context, OnTimeService.class);
            serviceIntent.setAction(OnTimeService.ACTION_REBOOT_START_SERVICE);
            context.startForegroundService(serviceIntent);
        }
    }
}
