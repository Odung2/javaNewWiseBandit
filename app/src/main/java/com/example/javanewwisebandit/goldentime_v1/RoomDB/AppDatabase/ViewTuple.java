package com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

public class ViewTuple {
    @ColumnInfo(name = "incentive")
    @NonNull
    public int incentive;

    public int numSuccess;

    public int numTotalTry;
}
