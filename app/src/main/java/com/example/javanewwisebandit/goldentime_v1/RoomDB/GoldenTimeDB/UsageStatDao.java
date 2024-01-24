package com.example.javanewwisebandit.goldentime_v1.RoomDB.GoldenTimeDB;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UsageStatDao {

    @Query("SELECT * FROM UsageStat")
    List<UsageStat> getAll();

    @Query("SELECT * FROM UsageStat WHERE id IN (:statIds)")
    List<UsageStat> loadAllByIds(int[] statIds);

    @Insert
    void insertAll(UsageStat... usageStat);

    @Delete
    void delete(UsageStat usageStat);

}
