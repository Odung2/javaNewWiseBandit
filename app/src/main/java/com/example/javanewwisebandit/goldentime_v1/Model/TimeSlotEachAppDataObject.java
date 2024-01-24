package com.example.javanewwisebandit.goldentime_v1.Model;

public class TimeSlotEachAppDataObject {
    public String packageName;
    public long startTime;
    public long endTime;
    public long duration;

    public String startTimeStr;
    public String endTimeStr;
    public String durationStr;

    public TimeSlotEachAppDataObject(String packageName, String startTimeStr, String endTimeStr, String durationStr) {
        this.packageName = packageName;
        this.startTimeStr = startTimeStr;
        this.endTimeStr = endTimeStr;
        this.durationStr = durationStr;
    }

    public TimeSlotEachAppDataObject(String packageName, long startTime, long endTime, long duration) {
        this.packageName = packageName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getStartTimeStr() {
        return startTimeStr;
    }

    public void setStartTimeStr(String startTimeStr) {
        this.startTimeStr = startTimeStr;
    }

    public String getEndTimeStr() {
        return endTimeStr;
    }

    public void setEndTimeStr(String endTimeStr) {
        this.endTimeStr = endTimeStr;
    }

    public String getDurationStr() {
        return durationStr;
    }

    public void setDurationStr(String durationStr) {
        this.durationStr = durationStr;
    }
}
