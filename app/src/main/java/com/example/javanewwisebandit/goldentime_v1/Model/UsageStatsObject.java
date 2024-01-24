package com.example.javanewwisebandit.goldentime_v1.Model;

public class UsageStatsObject {
    public String packageName;
    public String eventType;
    public long eventTime;

    public UsageStatsObject(String packageName, String eventType, long eventTime) {
        this.packageName = packageName;
        this.eventType = eventType;
        this.eventTime = eventTime;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }
}
