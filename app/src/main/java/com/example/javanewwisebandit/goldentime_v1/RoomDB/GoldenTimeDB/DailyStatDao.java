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

    @Insert
    void insertAll(DailyStat... dailyStats);

}
