package com.example.javanewwisebandit.goldentime_v1.RoomDB.GoldenTimeDB;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DailyStatDao {

    @Query("SELECT * FROM DailyStat")
    List<DailyStat> getAll();

    @Query("SELECT * FROM DailyStat WHERE id IN (:statIds)")
    List<DailyStat> loadAllByIds(int[] statIds);

    @Query("SELECT incentive FROM DailyStat ORDER BY Updated DESC LIMIT 1")
    int getLatestIncentive();

    @Insert
    void insertAll(DailyStat... dailyStats);

}
