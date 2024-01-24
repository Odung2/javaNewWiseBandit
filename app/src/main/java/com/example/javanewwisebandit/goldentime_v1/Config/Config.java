package com.example.javanewwisebandit.goldentime_v1.Config;

public class Config {
    public static final int GOLDENTIME_SERVICE_START_TIME = 9;
    public static final int GOLDENTIME_SERVICE_STOP_TIME = 2;
    public static final long GOLDENTIME_USAGESTATS_MIN_INTERVAL = 5000L;
    public static final long GOLDENTIME_USAGESTATS_MIN_USAGETIME = 1000L;


    /** Foreground 노티 정보 **/
    public static final int ONTIME_NOTIFICATION_ID = 5448;
    public static final String ONTIME_ALARM_CHANNEL_ID = "ICLAB_JYPARK_ONTIME_ALARM_CHANNEL_ID";
    public static final String ONTIME_ALARM_CHANNEL_NAME = "ICLAB_JYPARK_ONTIME_ALARM_CHANNEL_NAME";
    /** 일일 알림 노티 정보 **/
    public static final int DAILY_NOTIFICATION_ID = 3406;
    public static final String DAILY_ALARM_CHANNEL_ID = "ICLAB_JYPARK_DAILY_ALARM_CHANNEL_ID";
    public static final String DAILY_ALARM_CHANNEL_NAME = "ICLAB_JYPARK_DAILY_ALARM_CHANNEL_NAME";
    /** 9분 알림 노티 정보 **/
    public static final int WARNING_NOTIFICATION_ID = 3405;
    public static final String WARNING_ALARM_CHANNEL_ID = "ICLAB_JYPARK_WARNING_ALARM_CHANNEL_ID";
    public static final String WARNING_ALARM_CHANNEL_NAME = "ICLAB_JYPARK_WARNING_ALARM_CHANNEL_NAME";
    /** 10분 알림 노티 정보 **/
    public static final int TIMEOUT_NOTIFICATION_ID = 3404;
    public static final String TIMEOUT_ALARM_CHANNEL_ID = "ICLAB_JYPARK_TIMEOUT_ALARM_CHANNEL_ID";
    public static final String TIMEOUT_ALARM_CHANNEL_NAME = "ICLAB_JYPARK_TIMEOUT_ALARM_CHANNEL_NAME";

    /** 노티바 문구/워딩: Loss 그룹 **/
    public static final CharSequence SUBTITLE_DAILY = "일일 알림";
    public static final CharSequence SUBTITLE_WARNING = "사용제한 경고알림";
    public static final CharSequence SUBTITLE_TIMEOUT = "사용제한 초과알림";
    /*경고 알림 메시지 문구*/
    public static final CharSequence LOSS_WARNING_MSG2 = "사용제한 시간이 1분 남았습니다.";
    /*초과 알림 메시지 문구*/
    public static final CharSequence LOSS_TIMEOUT_MSG2 = "10분 사용하였습니다.";

}
