package com.example.javanewwisebandit.goldentime_v1.RoomDB.GoldenTimeDB;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserInfoDao {

    @Query("SELECT * FROM UserInfo")
    List<UserInfo> getAll();

    @Query("SELECT * FROM UserInfo LIMIT 1")
    UserInfo getFirstUserInfo();

//    @Query("SELECT id FROM UserInfo ORDER BY Updated DESC LIMIT 1");
//    int getUserId();

    @Query("SELECT * FROM UserInfo WHERE id IN (:statIds)")
    List<UserInfo> loadAllByIds(int[] statIds);

    @Insert
    void insertAll(UserInfo... userInfos);

    @Delete
    void delete(UserInfo userInfos);

    @Update
    void update(UserInfo userInfo);

    // 사용자 이름으로 UserInfo 객체를 찾는 메서드 추가
    @Query("SELECT * FROM UserInfo WHERE User = :userName LIMIT 1")
    UserInfo findUserByName(String userName);
}
