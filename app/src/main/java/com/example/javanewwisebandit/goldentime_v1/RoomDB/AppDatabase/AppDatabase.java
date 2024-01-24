package com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {UsageTimeDaily.class}, version = 1,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UsageTimeDailyDao usageTimeDailyDao();

    private static AppDatabase instance;

    private static final Object sLock = new Object();

    public static AppDatabase getInstance(Context context) {
        synchronized (sLock) {
            if(instance ==null) {
                instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "UserStats.db").build();
            }
            return instance;
        }
    }
}