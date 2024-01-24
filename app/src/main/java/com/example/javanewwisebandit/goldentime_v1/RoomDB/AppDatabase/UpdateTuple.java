package com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;

public class UpdateTuple {
    @ColumnInfo(name = "incentive")
    @NonNull
    public int incentive;

    @ColumnInfo(name = "success")
    @NonNull
    public boolean success;
}
