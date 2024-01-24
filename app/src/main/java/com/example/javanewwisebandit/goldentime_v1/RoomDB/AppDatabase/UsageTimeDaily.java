package com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesDateTimeProcess;


//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesDateTimeProcess;

@Entity
public class UsageTimeDaily {
    @ColumnInfo(name = "time_slot")
    @NonNull
    public int timeSlot;

    @ColumnInfo(name = "context")
    @NonNull
    public String context;

    @ColumnInfo(name = "incentive")
    @NonNull
    public int incentive;

    @ColumnInfo(name = "success")
    @NonNull
    public boolean success;

    @ColumnInfo(name = "date")
    @NonNull
    public String date;

    @PrimaryKey(autoGenerate = true)
    public int keyID = 0;

    public UsageTimeDaily(int timeSlot, int incentive, boolean success, String date) {
        this.timeSlot = timeSlot;
        this.incentive = incentive;
        this.success = success;
        this.date = date;
        this.context = UtilitiesDateTimeProcess.getContextByTimeSlotAndSuccess(timeSlot, success);
        //this.date = UtilitiesDateTimeProcess.getDateByDBDateFormat(UtilitiesDateTimeProcess.convertedDateStr(UtilitiesDateTimeProcess.getTodayDateByDateFormat()));
    }
}

