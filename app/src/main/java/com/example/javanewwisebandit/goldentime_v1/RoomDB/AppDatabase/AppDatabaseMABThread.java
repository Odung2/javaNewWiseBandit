package com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase;

import java.util.List;

import android.content.Context;

public class AppDatabaseMABThread extends Thread {
    private List<UpdateTuple> selectResult;
    private String context;
    private Context appContext;

    public AppDatabaseMABThread(String context, Context appContext){
        this.context = context;
        this.appContext = appContext;
    }

    public List<UpdateTuple> getSelectResult() {
        return selectResult;
    }

    public void setSelectResult(List<UpdateTuple> selectResult) {
        this.selectResult = selectResult;
    }

    public void run(){
        this.setSelectResult(AppDatabase.getInstance(appContext)
                .usageTimeDailyDao()
                .selectDataForMAB(context));
    }
}
