package com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase;

import android.content.Context;

public class AppDatabaseViewThread extends Thread {
    private int selectResult;
    private String context;
    private Context appContext;

    public AppDatabaseViewThread(String context, Context appContext){
        this.context = context;
        this.appContext = appContext;
    }

    public int getSelectResult() {
        return selectResult;
    }

    public void setSelectResult(int selectResult) {
        this.selectResult = selectResult;
    }

    public void run(){
        this.setSelectResult(AppDatabase.getInstance(appContext)
                .usageTimeDailyDao()
                .selectCountNumber(context));
    }
}
