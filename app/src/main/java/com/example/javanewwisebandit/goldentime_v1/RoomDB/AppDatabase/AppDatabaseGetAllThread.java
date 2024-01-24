package com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase;

import android.content.Context;

import java.util.List;

public class AppDatabaseGetAllThread extends Thread {
    private List<TimeLineTuple> timeLineModels;
    private Context appContext;

    public AppDatabaseGetAllThread(Context context){
        this.appContext = context;
    }

    public List<TimeLineTuple> getAllTimeLineModels(){ return timeLineModels; }

    public void setTimeLineModels(List<TimeLineTuple> tupleModels){
        this.timeLineModels = tupleModels;
    }

    public void run(){
        this.setTimeLineModels(AppDatabase.getInstance(appContext)
        .usageTimeDailyDao()
        .selectTimeLineModelData());
    }
}
