package com.example.javanewwisebandit.goldentime_v1.RoomDB.GoldenTimeDB;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class UserInfo {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name="User")   //DB에 저장되는 필드명
    public String user;

    @ColumnInfo(name="Frame")
    public int frame;

    @ColumnInfo(name="Updated")
    public String updated;

}
