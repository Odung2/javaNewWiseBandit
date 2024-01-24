package com.example.javanewwisebandit.goldentime_v1.Utils;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.javanewwisebandit.goldentime_v1.Model.UsageStatsObject;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;



public class AppUsageStats {

    public static Map<Integer, UsageStatsObject> getTimeSlotEachAppUsageStatsObject(Context context, long startTime, long endTime) {
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        Map<Integer, UsageStatsObject> allEventStats = new LinkedHashMap<>();
        int loopIdx=0;

        UsageEvents uEvents = mUsageStatsManager.queryEvents(startTime,endTime);

        while (uEvents.hasNextEvent()){
            UsageEvents.Event event = new UsageEvents.Event();
            uEvents.getNextEvent(event);
            String eventType = getEventTypeStr(event.getEventType());

            //if((event.getEventType() == 1 || event.getEventType() == 2 ) && checkUserPackageName(context,packageName))
            allEventStats.put(loopIdx++, new UsageStatsObject(event.getPackageName(), eventType, event.getTimeStamp()));
        }
        return allEventStats;
        //return UtilitiesUsageStatsDataProcess.getAppUsageStatistics(timeStr, allEventStats);
    }

    private static String getEventTypeStr(int eventType) {
        if(eventType == 1)          return "ACTIVITIY_RESUMED";
        else if(eventType == 2)     return "ACTIVITIY_PAUSED";
        else if(eventType == 23)    return "ACTIVITIY_STOPPED";
        else if(eventType == 5)     return "CONFIGURATION_CHANGE";
        else if(eventType == 26)    return "DEVICE_SHUTDOWN";
        else if(eventType == 27)    return "DEVICE_STARTUP";
        else if(eventType == 19)    return "FOREGROUND_SERVICE_START";
        else if(eventType == 20)    return "FOREGROUND_SERVICE_STOP";
        else if(eventType == 18)    return "KEYGUARD_HIDDEN";
        else if(eventType == 17)    return "KEYGUARD_SHOWN";
        else if(eventType == 15)    return "SCREEN_INTERACTIVE";
        else if(eventType == 16)    return "SCREEN_NON_INTERACTIVE";
        else if(eventType == 8)     return "SHORTCUT_INVOCATION";
        else if(eventType == 11)    return "STANDBY_BUCKET_CHANGED";
        else if(eventType == 7)     return "USER_INTERACTION";
        else                        return "NONE";
    }

}
