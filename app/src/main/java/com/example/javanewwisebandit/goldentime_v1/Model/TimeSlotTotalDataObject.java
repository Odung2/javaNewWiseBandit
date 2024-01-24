package com.example.javanewwisebandit.goldentime_v1.Model;

public class TimeSlotTotalDataObject {
    public String packageName;
    public long usageTime;
    public int usageCount;

    public TimeSlotTotalDataObject(String packageName, long usageTime, int usageCount) {
        this.packageName = packageName;
        this.usageTime = usageTime;
        this.usageCount = usageCount;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public long getUsageTime() {
        return usageTime;
    }

    public void setUsageTime(long usageTime) {
        this.usageTime = usageTime;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }
}
