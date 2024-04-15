package com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase;

import android.content.Context;

public class AppDatabaseIncentiveThread extends Thread{
    private int sum;
    private Context appContext;
    private String date;

    public AppDatabaseIncentiveThread(Context appContext, String date){
        this.appContext = appContext;
        this.sum = 0;
        this.date = date;
    }

    public int getSum() {
        return sum;
    }

    public void run(){
        if(this.date.equals("")){
            this.sum = AppDatabase.getInstance(appContext)
                    .usageTimeDailyDao()
//                    .getTotalFailSumIncentive(); // LossFrame
                    .getTotalSuccessSumIncentive(); // GainFrame
        }
        else {
            this.sum = AppDatabase.getInstance(appContext)
                    .usageTimeDailyDao()
//                    .getFailSumIncentiveByDate(date); // LossFrame
                    .getSuccessSumIncentiveByDate(date); // GainFrame
        }
    }

}
