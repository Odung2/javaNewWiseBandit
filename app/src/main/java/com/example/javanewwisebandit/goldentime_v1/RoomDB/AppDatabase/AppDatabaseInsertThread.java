package com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase;

import android.content.Context;

public class AppDatabaseInsertThread extends Thread{
    private Context appContext;
    private int timeSlot;
    private int incentive;
    private boolean success;
    private String date;

    public AppDatabaseInsertThread(Context appContext, int timeSlot, int incentive, boolean success, String date){
        this.appContext = appContext;
        this.timeSlot = timeSlot;
        this.incentive = incentive;
        this.success = success;
        this.date = date;
    }

    public void run(){
        AppDatabase.getInstance(appContext)
                .usageTimeDailyDao()
                .insert(new UsageTimeDaily(timeSlot, incentive, success, date));
    }
}
