package com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

public class TimeLineTuple {
    @ColumnInfo(name = "time_slot")
    @NonNull
    public int timeSlot;

    @ColumnInfo(name = "date")
    @NonNull
    public String date;

    @ColumnInfo(name = "incentive")
    @NonNull
    public int incentive;

    @ColumnInfo(name = "success")
    @NonNull
    public boolean success;
}
