package com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UsageTimeDailyDao {
    @Query("SELECT * FROM UsageTimeDaily")
    List<UsageTimeDaily> getAll();

    @Query("SELECT incentive, success FROM UsageTimeDaily WHERE context = :context")
    List<UpdateTuple> selectDataForMAB(String context);

    @Query("SELECT incentive, SUM(success) AS numSuccess, COUNT(*) AS numTotalTry FROM UsageTimeDaily WHERE context = :context GROUP BY incentive")
    List<ViewTuple> selectExpectedRateNumber(String context);

    @Query("SELECT time_slot, date, incentive, success FROM UsageTimeDaily")
    List<TimeLineTuple> selectTimeLineModelData();
    /**Loss Frame**/
    @Query("SELECT SUM(incentive) FROM UsageTimeDaily WHERE date = :date AND success = 0")
    int getFailSumIncentiveByDate(String date);
    /**Loss Frame**/
    @Query("SELECT SUM(incentive) FROM UsageTimeDaily WHERE success = 0")
    int getTotalFailSumIncentive();

    /**Gain Frame**/
    @Query("SELECT SUM(incentive) FROM UsageTimeDaily WHERE date = :date AND success = 1")
    int getSuccessSumIncentiveByDate(String date);
    /**Gain Frame**/
    @Query("SELECT SUM(incentive) FROM UsageTimeDaily WHERE success = 1")
    int getTotalSuccessSumIncentive();

    @Query("SELECT COUNT(*) FROM UsageTimeDaily WHERE context = :context")
    int selectCountNumber(String context);

    @Insert
    void insert(UsageTimeDaily data);

    @Delete
    void delete(UsageTimeDaily data);
}
