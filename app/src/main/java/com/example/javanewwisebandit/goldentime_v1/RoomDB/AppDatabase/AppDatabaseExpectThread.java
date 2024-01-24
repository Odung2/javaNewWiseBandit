package com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase;

import android.content.Context;

import java.util.List;

public class AppDatabaseExpectThread extends Thread {
    private List<ViewTuple> selectResult;
    private String context;
    private Context appContext;

    public AppDatabaseExpectThread(String context, Context appContext){
        this.context = context;
        this.appContext = appContext;
    }

    public List<ViewTuple> getSelectResult() {
        return selectResult;
    }

    public void setSelectResult(List<ViewTuple> selectResult) {
        this.selectResult = selectResult;
    }

    public void run(){
        this.setSelectResult(AppDatabase.getInstance(appContext)
                .usageTimeDailyDao()
                .selectExpectedRateNumber(context));
    }
}
