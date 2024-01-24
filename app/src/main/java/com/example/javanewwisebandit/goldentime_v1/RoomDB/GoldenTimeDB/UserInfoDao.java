package com.example.javanewwisebandit.goldentime_v1.RoomDB.GoldenTimeDB;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserInfoDao {

    @Query("SELECT * FROM UserInfo")
    List<UserInfo> getAll();

    @Query("SELECT * FROM UserInfo WHERE id IN (:statIds)")
    List<UserInfo> loadAllByIds(int[] statIds);

    @Insert
    void insertAll(UserInfo... userInfos);

    @Delete
    void delete(UserInfo userInfos);

}
