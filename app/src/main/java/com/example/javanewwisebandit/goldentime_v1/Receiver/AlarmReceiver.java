package com.example.javanewwisebandit.goldentime_v1.Receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.javanewwisebandit.goldentime_v1.Service.OnTimeService;
import com.example.javanewwisebandit.goldentime_v1.Service.RemoteDBUpdateService;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesLocalDBProcess;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;

//import kr.ac.kaist.jypark.goldentime_v1.Service.OnTimeService;
//import kr.ac.kaist.jypark.goldentime_v1.Service.RemoteDBUpdateService;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesDateTimeProcess;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("ALARM_ONTIMER")) {
            Intent serviceIntent2 = new Intent(context, OnTimeService.class);
            serviceIntent2.setAction(OnTimeService.ACTION_NORMAL_START_SERVICE);
            context.startForegroundService(serviceIntent2);
        }
        if (intent.getAction().equals("ALARM_APPCHANGE")) {
//            UtilitiesSharedPrefDataProcess.setBooleanDataToSharedPref(context, "isAppChange", true);
            /**baseline->intervention 이 아니라  intervention->baseline으로**/
            UtilitiesSharedPrefDataProcess.setBooleanDataToSharedPref(context, "isAppChange", false);
            UtilitiesSharedPrefDataProcess.setBooleanDataToSharedPref(context,"isUpdateAfterAppChange",false);
            UtilitiesSharedPrefDataProcess.setBooleanDataToSharedPref(context, "isInterventionDone", true);
            /** 중재 앱에서 사용하는 변수 초기화 **/
            /*UtilitiesSharedPrefDataProcess.setStringDataToSharedPref(context,"firstDate", UtilitiesDateTimeProcess.getTodayDateByDateFormat());
            UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(context, "totalSuccess", 0);
            UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(context, "totalFail", 0);
            UtilitiesSharedPrefDataProcess.setStringDataToSharedPref(context,"UsageTimeTotal", null);*/
            UtilitiesSharedPrefDataProcess.setBooleanDataToSharedPref(context, "isFollowup", true);
        }
        else if(intent.getAction().equals("ALARM_ONTIME_DBUPDATE")) {
            Intent serviceIntent = new Intent(context, RemoteDBUpdateService.class);
            serviceIntent.setAction(RemoteDBUpdateService.ACTION_ONTIME_STATS_UPDATE);
            context.startService(serviceIntent);
        }
        else if(intent.getAction().equals("ALARM_USAGESTATS_DBUPDATE")) {
            Intent serviceIntent = new Intent(context, RemoteDBUpdateService.class);
            serviceIntent.setAction(RemoteDBUpdateService.ACTION_APPUSAGE_STATS_UPDATE);
            context.startService(serviceIntent);
        }
    }

    public Boolean isLaunchingService(Context mContext){
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (OnTimeService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return  false;
    }
}

