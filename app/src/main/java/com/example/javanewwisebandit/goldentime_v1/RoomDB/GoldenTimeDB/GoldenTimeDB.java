package com.example.javanewwisebandit.goldentime_v1.RoomDB.GoldenTimeDB;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {DailyStat.class, UsageStat.class, UserInfo.class}, version = 1,exportSchema = false)
public abstract class GoldenTimeDB extends RoomDatabase {

    private static GoldenTimeDB INSTANCE = null;
    public abstract DailyStatDao dailyStatDao();
    public abstract UsageStatDao usageStatDao();
    public abstract UserInfoDao userInfoDao();


    public static GoldenTimeDB getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    GoldenTimeDB.class, "goldentime.db").build();

        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

}
