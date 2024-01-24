package com.example.javanewwisebandit.goldentime_v1.Model;

public class UsageStatsIndexObject {
    public String packageName;
    public int startTimeIdx;
    public int endTimeIdx;

    public UsageStatsIndexObject(String packageName, int startTimeIdx, int endTimeIdx) {
        this.packageName = packageName;
        this.startTimeIdx = startTimeIdx;
        this.endTimeIdx = endTimeIdx;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getStartTimeIdx() {
        return startTimeIdx;
    }

    public void setStartTimeIdx(int startTimeIdx) {
        this.startTimeIdx = startTimeIdx;
    }

    public int getEndTimeIdx() {
        return endTimeIdx;
    }

    public void setEndTimeIdx(int endTimeIdx) {
        this.endTimeIdx = endTimeIdx;
    }
}
