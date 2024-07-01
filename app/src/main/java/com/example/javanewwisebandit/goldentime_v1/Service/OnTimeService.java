package com.example.javanewwisebandit.goldentime_v1.Service;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.javanewwisebandit.R;
import com.example.javanewwisebandit.goldentime_v1.Activity.MainActivity;
import com.example.javanewwisebandit.goldentime_v1.Config.Config;
import com.example.javanewwisebandit.goldentime_v1.Incentive;
import com.example.javanewwisebandit.goldentime_v1.Receiver.BackgroundReceiver;
import com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase.AppDatabaseExpectThread;
import com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase.AppDatabaseInsertThread;
import com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase.AppDatabaseMABThread;
import com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase.UpdateTuple;
import com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase.ViewTuple;
import com.example.javanewwisebandit.goldentime_v1.RoomDB.GoldenTimeDB.GoldenTimeDB;
import com.example.javanewwisebandit.goldentime_v1.RoomDB.GoldenTimeDB.UserInfo;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesDateTimeProcess;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesLocalDBProcess;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class OnTimeService extends Service {
    public static final String ACTION_NORMAL_START_SERVICE = "ACTION_NORMAL_START_FOREGROUND_SERVICE";
    public static final String ACTION_REBOOT_START_SERVICE = "ACTION_REBOOT_START_FOREGROUND_SERVICE";
    public static final String ACTION_SCREEN_UNLOCK_SERVICE = "ACTION_SCREEN_UNLOCK_FOREGROUND_SERVICE";
    public static final String ACTION_SCREEN_OFF_SERVICE = "ACTION_SCREENOFF_FOREGROUND_SERVICE";

    private BroadcastReceiver backgroundReceiver;
    NotificationCompat.Builder timerNotifyBuilder;
    NotificationManager timerNotificationManager;
    RemoteViews remoteViews;

    Timer screenOnTimerObj; //screen-on 타이머
    TimerTask screenOnTimerTaskObj;
    Timer watchDogObj;   //watchdog
    TimerTask watchDogTimerTaskObj;

    public int screenOnTimeCount;   // screen-on 타이머 카운트
    public int currentTimeSlot;
    public boolean isAppChanged;
    public boolean isUpdateAfterAppChange;

    public boolean isInterventionDone;

    public OnTimeService() { }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("AA", "OnTimeService() 시작");

        if(intent != null) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_NORMAL_START_SERVICE:
                    Log.d("AA", "OnTimeService case: ACTION_NORMAL_START_SERVICE - initNormalCaseOnTimer() is to be running ");
                    initNormalCaseOnTimer();
                    break;
                case ACTION_REBOOT_START_SERVICE:
                    Log.d("AA", "OnTimeService case: ACTION_REBOOT_START_SERVICE - initNotNormalCaseOnTimer() is to be running ");
                    initNotNormalCaseOnTimer();
                    break;
                case ACTION_SCREEN_UNLOCK_SERVICE:
                    Log.d("AA", "OnTimeService case: ACTION_SCREEN_UNLOCK_SERVICE - startScreenOnTimer() is to be running ");
                    startScreenOnTimer();
                    break;
                case ACTION_SCREEN_OFF_SERVICE:
                    Log.d("AA", "OnTimeService case: ACTION_SCREEN_OFF_SERVICE - startScreenOffForegroundService() is to be running ");
                    startScreenOffForegroundService();
                    break;
            }
        }

        return START_NOT_STICKY;
    }

    /** 예외적인 케이스로 정시 서비스가 실행되는 경우: 폰 껏다 키면서 foreground 서비스가 실행되는 경우 **/
    private void initNotNormalCaseOnTimer() {
        isUpdateAfterAppChange = UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getApplicationContext(),"isUpdateAfterAppChange");
        isAppChanged = UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getApplicationContext(),"isAppChange");
        currentTimeSlot = UtilitiesDateTimeProcess.getCurrentTimeHour(); //타임슬롯 설정
        isInterventionDone = UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getApplicationContext(), "isInterventionDone");
        int lastTimeSlot = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(), "lastTimeSlot");

        if(isAppChanged && isUpdateAfterAppChange && currentTimeSlot==9 && !isInterventionDone) {
            Log.d("AA", "showDailyNotification() 시작");
            showDailyNotification();
        }else{
            Log.d("AA", "isAppChanged 또는 isUpdateAfterAppchange 또는 currentTimeSlot 오류");

        }

        /* 폰 껏다 켰는데 타임슬롯이 변경된 경우: 앞 선 데이터 저장(이전시간 타임슬롯으로 저장) */
        if(currentTimeSlot != lastTimeSlot) {
            boolean noFirstTrial = UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getApplicationContext(),"noFirstTrial");
            if(noFirstTrial) {
                makeUpUsageTimeStats(lastTimeSlot, isAppChanged);
                UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(getApplicationContext(),"onTimeCount", 0);
                screenOnTimeCount =0;
            }
            else    UtilitiesSharedPrefDataProcess.setBooleanDataToSharedPref(getApplicationContext(),"noFirstTrial", true);
        }
        /* 폰 껏다 켰는데 같은 타임슬롯인 경우: 기존 데이터 로드 */
        else    screenOnTimeCount = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(), "onTimeCount");

        setupOnTimerResources();
    }

    /** 정시타이머 동작 관련된 모든 리소스(예. 타이머, 브로드캐스트 리시버 등)들을 초기화하는 함수 **/
    private void setupOnTimerResources() {
        registerScreenOnOffReceiver();  // 브로드캐스트 해제, 재시작
        isUpdateAfterAppChange = UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getApplicationContext(), "isUpdateAfterAppChange");

        if(isAppChanged && isUpdateAfterAppChange && UtilitiesDateTimeProcess.checkGoldenTime()){
            updateIncentiveMAB();
            chooseNewIncentive();
            showOnTimeNotification2();
        } else {
            showOnTimeNotification();
        }

        if(!checkDeviceLock())   startScreenOnTimer();
        startWatchDog();    //브로드캐스트 리시버 및 노티바 상태 모니터링
    }

    private void updateIncentiveMAB() {
        //실제 DB 데이터 타입은 UpdateTuple.java 참고
        String context = UtilitiesDateTimeProcess.getContextByTimeSlot(currentTimeSlot);
        double incentive = 200; //DB에서 가져와야함
        double success = 0; //DB에서 가져와야함

        AppDatabaseMABThread thread = new AppDatabaseMABThread(context, getApplicationContext());
        Log.d("AA", "Update incentive Thread state: "+thread.getState());
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            //TODO: handle exception
            return;
        }
        List<UpdateTuple> result = thread.getSelectResult();

        if (result != null && result.size() > 0) {
            for (UpdateTuple tuple : result) {
                success = tuple.success ? 1d : 0d;
                Incentive.getInstance().update(
                        tuple.incentive, success, context
                );
                Log.d("AA", "Room DB debugging, incentive MAB model updated");
            }
        }
    }

    // 인터페이스 정의: 서버 응답 처리를 위한 콜백
    interface IncentiveFrameListener {
        void onIncentiveFrameReceived(String incentiveFrame);
    }

    // 메서드 수정
    private void getUserIncentiveFrame(IncentiveFrameListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UserInfo userInfo = GoldenTimeDB.getInstance(getApplicationContext()).userInfoDao().getFirstUserInfo();
                if (userInfo != null) {
                    String url = "http://143.248.53.137:5000/goldentime/userincentive/" + userInfo.id;

                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject jsonResponse = new JSONObject(response);
                                        String incentiveFrame = jsonResponse.getString("incentiveFrame");
                                        listener.onIncentiveFrameReceived(incentiveFrame);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // 오류 처리
                        }
                    });

                    RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                    queue.add(stringRequest);
                }
            }
        }).start();
    }

    /** 새로운 incentive를 뽑아서 sharedpreference에 저장 **/
    private void chooseNewIncentive(){
//        int incentive = Incentive.getInstance().choose(UtilitiesDateTimeProcess.getContextByTimeSlot(currentTimeSlot)).intValue();
//        Log.d("AA", "Room DB Debugging, choose new incentive: " + incentive);
//        UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(this, "incentive", incentive);
        getUserIncentiveFrame(new IncentiveFrameListener() {
            @Override
            public void onIncentiveFrameReceived(String incentiveFrame) {
                int incentive;
                if (incentiveFrame.equals("Constant")) {
                    incentive = chooseConstantIncentive();
                } else if (incentiveFrame.equals("Random")) {
                    incentive = chooseRandomIncentive();
                } else if (incentiveFrame.equals("MAB")) {
                    incentive = Incentive.getInstance().choose(UtilitiesDateTimeProcess.getContextByTimeSlot(currentTimeSlot)).intValue();
                } else {
                    incentive = GoldenTimeDB.getInstance(getApplicationContext()).dailyStatDao().getLatestIncentive();
                    Log.d("AA", "Error occur: No Exist incentiveFrame Type - set incentive: " + incentive);
                }
                UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(getApplicationContext(), "incentive", incentive);
                Log.d("AA", "Room DB Debugging, choose new incentive: " + incentive);
            }
        });
    }




    private int chooseConstantIncentive() {
        return 500; // 상수 인센티브 값
    }

    private int chooseRandomIncentive() {
        int[] possibleIncentives = {200, 400, 600, 800};
        int randomIndex = new Random().nextInt(possibleIncentives.length);
        return possibleIncentives[randomIndex];
    }


    /** 폰 다시 켰을 때 이전 타임슬롯의 사용 시간 통계 make-up 해주는 함수 **/
    private boolean makeUpUsageTimeStats(int lastTimeSlot, boolean isAppChanged) {
        int lastTimeSlotUsageTime = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(), "onTimeCount");

        UtilitiesSharedPrefDataProcess.makeupUpdateTotalOnTimeUsageData(getApplicationContext(),lastTimeSlot, lastTimeSlotUsageTime, isAppChanged);
        lastTimeSlot++;
        int endTimeSlot;
        if(lastTimeSlot > currentTimeSlot)  endTimeSlot = currentTimeSlot+24;
        else                                endTimeSlot = currentTimeSlot;

        int tmpHour;
        for(int i=lastTimeSlot; i<endTimeSlot; i++) {
            if(i >=24)   tmpHour = i-24;
            else         tmpHour = i;
            UtilitiesSharedPrefDataProcess.makeupUpdateTotalOnTimeUsageData(getApplicationContext(),tmpHour, 0, isAppChanged);
        }
        return true;
    }

    /** 정상적으로 정시 서비스 실행하는 경우: 앱설치 후 등록된 알람에 의해 foreground 서비스가 실행되는 경우 **/
    private void initNormalCaseOnTimer() {
        isUpdateAfterAppChange = UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getApplicationContext(),"isUpdateAfterAppChange");
        isAppChanged = UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getApplicationContext(),"isAppChange");
        currentTimeSlot = UtilitiesDateTimeProcess.getCurrentTimeHour(); //타임슬롯 설정
        isInterventionDone = UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getApplicationContext(), "isInterventionDone");
        int lastTimeSlot = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(), "lastTimeSlot");

        /* 정시 알림이 좀 일찍 도착한 경우: 정시보다 이전 도착 */
        if(lastTimeSlot == currentTimeSlot || (lastTimeSlot-currentTimeSlot ==24)) currentTimeSlot++;
        UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(getApplicationContext(), "lastTimeSlot", currentTimeSlot);

        /* 중재앱 기간 중에 현재 타임슬롯이 9시이면: 일일 알림 노티 제공 */
        if(isAppChanged && isUpdateAfterAppChange && currentTimeSlot==9 && !isInterventionDone) {
            Log.d("AA", "showDailyNotification() 시작");
            showDailyNotification();
        }else{
            Log.d("AA", "isAppChanged 또는 isUpdateAfterAppchange 또는 currentTimeSlot 오류");
        }

        /* 이전 타임슬롯 사용통계 저장 */
        boolean noFirstTrial = UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getApplicationContext(),"noFirstTrial");
        if(noFirstTrial) {
            screenOnTimeCount = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(),"onTimeCount");
            UtilitiesSharedPrefDataProcess.updateTotalOnTimeUsageData(getApplicationContext(),currentTimeSlot,screenOnTimeCount, isAppChanged);
            screenOnTimeCount = 0;
            UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(getApplicationContext(),"onTimeCount", screenOnTimeCount);
        }
        else {
            UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(getApplicationContext(), "lastTimeSlot", currentTimeSlot);
            UtilitiesSharedPrefDataProcess.setBooleanDataToSharedPref(getApplicationContext(),"noFirstTrial", true);
        }

        setupOnTimerResources();
    }

    /** 일일 알림 노티 표시하는 함수 **/
    private void showDailyNotification() {
        NotificationManager dailyNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(Config.DAILY_ALARM_CHANNEL_ID, Config.DAILY_ALARM_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        channel.setShowBadge(false);
        dailyNotificationManager.createNotificationChannel(channel);

        RemoteViews dailyRemoteView = new RemoteViews(getPackageName(), R.layout.notification_daily_gain);
        /*어제까지 총 성공/실패 횟수 로드 */
        DecimalFormat decimalFormat = new DecimalFormat("###,###");

        int totalLossGold = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(this, "TotalIncentive");
//        dailyRemoteView.setTextViewText(R.id.dailynotiTotalLossGoldStatisticsVal, decimalFormat.format(totalLossGold) +"골드 ");
        dailyRemoteView.setTextViewText(R.id.dailynotiTotalGainGoldStatisticsVal, "+ "+decimalFormat.format(totalLossGold) +"골드 획득");

        //int yesterdayLossGold = getIncentiveSum(UtilitiesDateTimeProcess.getDateByDBDateFormat(UtilitiesDateTimeProcess.convertedDateStr(UtilitiesDateTimeProcess.getPreviousDateByDateFormat(UtilitiesDateTimeProcess.getTodayDateByDateFormat()))));
        int yesterdayLossGold = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(this, "TodayIncentive");
        dailyRemoteView.setTextViewText(R.id.dailynotiYesterdayGoldStatisticsVal, "+ "+decimalFormat.format(yesterdayLossGold)+"골드");
        //String totalRemainGold = decimalFormat.format(178500 - totalLossGold);
        //dailyRemoteView.setTextViewText(R.id.dailynotiTotalGoldStatisticsVal, totalRemainGold+"골드");

        /* 오늘 성공, 실패, 하루치 인센티브 리셋 */
        UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(getApplicationContext(),"dailySuccess",0);
        UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(getApplicationContext(),"dailyFail",0);
        UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(getApplicationContext(), "TodayIncentive", 0);

        NotificationCompat.Builder warningNotifyBuilder = new NotificationCompat.Builder(this, Config.DAILY_ALARM_CHANNEL_ID)
                .setContent(dailyRemoteView)
                .setSubText(Config.SUBTITLE_DAILY)
                .setColor(Color.parseColor("#AE905E"))
                .setShowWhen(true)
                .setSmallIcon(R.drawable.ic_monetization_on_24px);

        dailyNotificationManager.notify(Config.DAILY_NOTIFICATION_ID, warningNotifyBuilder.build());

    }

    /** 9분 알림 노티 표시하는 함수 **/
    private void showWarningNotification() {
        NotificationManager warningNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(Config.WARNING_ALARM_CHANNEL_ID, Config.WARNING_ALARM_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(false);
        channel.setVibrationPattern(new long[] {100,1000});
        warningNotificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder warningNotifyBuilder = new NotificationCompat.Builder(this, Config.WARNING_ALARM_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.golden_time))
//                .setContentTitle(Html.fromHtml("1분 후 <font color=#AE905E>" + UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(this, "incentive") + "골드</font> 차감", Html.FROM_HTML_MODE_COMPACT))
                .setContentTitle(Html.fromHtml("1분 내 폰 사용 중지 시 <font color=#AE905E>" + UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(this, "incentive") + "골드</font> 획득", Html.FROM_HTML_MODE_COMPACT))
                .setContentText(Config.LOSS_WARNING_MSG2)
                //.setSubText(Config.SUBTITLE_WARNING)
                .setColor(Color.parseColor("#AE905E"))
                .setShowWhen(true)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_monetization_on_24px);

        warningNotificationManager.notify(Config.WARNING_NOTIFICATION_ID, warningNotifyBuilder.build());
    }

    /** 10분 알림 노티 표시하는 함수 **/
    private void showTimeoutNotification() {
        NotificationManager timeoutNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(Config.TIMEOUT_ALARM_CHANNEL_ID, Config.TIMEOUT_ALARM_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(false);
        channel.setVibrationPattern(new long[] {100,1000});
        timeoutNotificationManager.createNotificationChannel(channel);


        NotificationCompat.Builder warningNotifyBuilder = new NotificationCompat.Builder(this, Config.TIMEOUT_ALARM_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.golden_time))
//                .setContentTitle(Html.fromHtml("<font color=#AE905E>" + UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(this, "incentive") + "골드</font> 차감", Html.FROM_HTML_MODE_COMPACT))
                .setContentTitle(Html.fromHtml("<font color=#AE905E>" + "0" + "골드</font> 획득", Html.FROM_HTML_MODE_COMPACT))

                .setContentText(Config.LOSS_TIMEOUT_MSG2)
                //.setSubText(Config.SUBTITLE_TIMEOUT)
                .setColor(Color.parseColor("#AE905E"))
                .setShowWhen(true)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_monetization_on_24px);

        timeoutNotificationManager.notify(Config.TIMEOUT_NOTIFICATION_ID, warningNotifyBuilder.build());
    }

    private void showOnTimeNotification2() {
        timerNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(Config.ONTIME_ALARM_CHANNEL_ID, Config.ONTIME_ALARM_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        channel.setShowBadge(false);
        timerNotificationManager.createNotificationChannel(channel);

        /*커스텀 노티바 화면 만들기*/
//        remoteViews = new RemoteViews(getPackageName(), R.layout.notification_foregrund_loss);
        remoteViews = new RemoteViews(getPackageName(), R.layout.notification_foreground_gain);

        remoteViews.setTextViewText(R.id.notiUsageTimeText, currentTimeSlot+"-"+(currentTimeSlot+1)+"시 사용시간: ");
        /*남은골드 리셋*/
        goldSetup(17, "#D29953");

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        timerNotifyBuilder = new NotificationCompat.Builder(this, Config.ONTIME_ALARM_CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setContent(remoteViews)
                .setShowWhen(false)
                .setSmallIcon(R.drawable.ic_monetization_on_24px);
//                .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteViews));

        startForeground(Config.ONTIME_NOTIFICATION_ID, timerNotifyBuilder.build());
    }

    private void goldSetup(int failNum, String colorStr) {
        int loopIdx = 17 - failNum;
        for(int i=17;i>loopIdx;i--) {
            String goldId = "gold"+i;
            int resId = getResources().getIdentifier(goldId,"id", getPackageName());
            remoteViews.setInt(resId, "setColorFilter", Color.parseColor(colorStr));
        }
    }


    private void startWatchDog() {
        if (watchDogObj == null) {
            watchDogObj = new Timer();
            watchDogTimerTaskObj = new TimerTask() {
                public void run() {
                    checkAndRegisterReceiver();
                    checkAndRegisterNotification();
                }
            };
            watchDogObj.schedule(watchDogTimerTaskObj, 0, 1000);
        }
    }

    private void checkAndRegisterNotification() {
        if(timerNotifyBuilder == null || timerNotificationManager == null) {
            if(isAppChanged && isUpdateAfterAppChange && UtilitiesDateTimeProcess.checkGoldenTime())    showOnTimeNotification2();
            else                                                                                        showOnTimeNotification();
        }
    }

    private void checkAndRegisterReceiver() {
        if(backgroundReceiver == null) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            filter.addAction(Intent.ACTION_SHUTDOWN);
            filter.addAction(Intent.ACTION_BOOT_COMPLETED);
            filter.addAction(Intent.ACTION_LOCKED_BOOT_COMPLETED);

            backgroundReceiver = new BackgroundReceiver();
            registerReceiver(backgroundReceiver, filter);
        }
    }

    private boolean checkDeviceLock(){
        KeyguardManager myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return myKM.inKeyguardRestrictedInputMode();
    }

    private void showOnTimeNotification() {
        timerNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(Config.ONTIME_ALARM_CHANNEL_ID, Config.ONTIME_ALARM_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        channel.setShowBadge(false);
        timerNotificationManager.createNotificationChannel(channel);

        String contentText;
        if(currentTimeSlot >=2 && currentTimeSlot<9)    contentText = currentTimeSlot+"시 GoldenTime 서비스 시간이 아닙니다.";
        else                                            contentText = currentTimeSlot+"시 GoldenTime 서비스가 동작 중입니다.";

        timerNotifyBuilder = new NotificationCompat.Builder(this, Config.ONTIME_ALARM_CHANNEL_ID)
                .setContentTitle("GoldenTime")
                .setContentText(contentText)
                .setShowWhen(false)
                .setSmallIcon(R.drawable.ic_monetization_on_24px);

        startForeground(Config.ONTIME_NOTIFICATION_ID, timerNotifyBuilder.build());
    }


    private void registerScreenOnOffReceiver() {
        if(backgroundReceiver!=null) {
            unregisterReceiver(backgroundReceiver);
            backgroundReceiver = null;
            checkAndRegisterReceiver();
        }
    }

    private void startScreenOnTimer() {
        if (screenOnTimerObj == null) {
            screenOnTimerObj = new Timer();
            screenOnTimerTaskObj = null;
            screenOnTimerTaskObj = new TimerTask() {
                public void run() {
                    screenOnTimeCount++;
                    UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(getApplicationContext(),"onTimeCount", screenOnTimeCount);
                    if(isAppChanged && isUpdateAfterAppChange && UtilitiesDateTimeProcess.checkGoldenTime()) {
                        updateTimer(screenOnTimeCount);
                        if(screenOnTimeCount<=15) startTimer(screenOnTimeCount);
                        if(screenOnTimeCount==540)  showWarningNotification();  // 9분 경고
                        if(screenOnTimeCount==600) {
                            showTimeoutNotification();  // 10분 경고
//                            saveFailDataInLocalDB(); // incentive 값만 저장
//                            saveSuccessDataInLocalDB(); // GainFrame
                            updateSuccessFailStatistics(); // 실패 횟수 증가
                            startTimer(screenOnTimeCount);
                        }
                    }
                }
            };
            screenOnTimerObj.schedule(screenOnTimerTaskObj, 0, 1000);
        }
    }

    private void updateSuccessFailStatistics() {
        /*오늘 실패 횟수 증가 */
        int todayFailNum = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(), "dailyFail");
        UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(getApplicationContext(),"dailyFail",++todayFailNum);

        /*누적 실패 횟수 증가 */
        int totalFailNum = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(), "totalFail");
        UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(getApplicationContext(),"totalFail",++totalFailNum);
    }

    // 노티바
    private void startTimer(int timeCount) {

        String usageTimeText = UtilitiesDateTimeProcess.getCurrentTimeHour() + "-" + (UtilitiesDateTimeProcess.getCurrentTimeHour() + 1) + "시 사용시간: ";
        String usageTime = (String) getNotificationTimerFormat(timeCount);
        String incentiveText = UtilitiesDateTimeProcess.getCurrentTimeHour() + "-" + (UtilitiesDateTimeProcess.getCurrentTimeHour() + 1) + "시 미션 성공 시: ";
        remoteViews.setTextViewText(R.id.notiUsageTimeText, UtilitiesDateTimeProcess.getCurrentTimeHour()+"-"+(UtilitiesDateTimeProcess.getCurrentTimeHour()+1)+"시 사용시간: ");
        remoteViews.setTextViewText(R.id.notiUsageTime, getNotificationTimerFormat(timeCount));
        remoteViews.setTextViewText(R.id.notiThisIncentive, UtilitiesDateTimeProcess.getCurrentTimeHour()+"-"+(UtilitiesDateTimeProcess.getCurrentTimeHour()+1)+"시 미션 성공 시: ");

        /*오늘 차감/누적 차감 데이터 로드 */
        int todayFailNum = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(), "dailyFail");
        int totalFail = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(), "totalFail");

        /*오늘 획득/누적 획득 데이터 로드 */
        String todayDateStr = UtilitiesDateTimeProcess.getTodayDateByDateFormat();
        int todayGainGoldValue = UtilitiesSharedPrefDataProcess.getIncentiveForDate(getApplicationContext(), todayDateStr);
        int totalGainGoldValue = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(), "TotalIncentive");
        DecimalFormat decimalFormat = new DecimalFormat("###,###");

        /*오늘획득*/
        String todayGainGold = decimalFormat.format(todayGainGoldValue);
        /*누적획득*/
        String totalGainGold = decimalFormat.format(totalGainGoldValue);

        /* remoteViews 업데이트*/
        remoteViews.setTextViewText(R.id.notiTotalGainGoldStatisticsVal, "+"+todayGainGold+"골드 (+"+totalGainGold+"골드)");

        /*현재 시간 인센티브 가져오고 세팅*/
        int incentive = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(this, "incentive");
        remoteViews.setTextViewText(R.id.notiThisIncentiveVal, this.getString(R.string.notiThisGainIncentiveString, incentive));
        String incentiveValue = this.getString(R.string.notiThisGainIncentiveString, incentive);

        /*성공률 계산*/
        String context = UtilitiesDateTimeProcess.getContextByTimeSlot(currentTimeSlot);
        AppDatabaseExpectThread ethread = new AppDatabaseExpectThread(context, this);
        ethread.start();
        try {
            ethread.join();
        } catch (InterruptedException e) {
            //TODO: handle exception
        }
        List<ViewTuple> result = ethread.getSelectResult();

        double expectedRate = 0;
        for (ViewTuple tuple: result){
            if (incentive == tuple.incentive) {
                expectedRate = (double) tuple.numSuccess / tuple.numTotalTry;
                break;
            }
        }
        expectedRate = expectedRate * 100;
        remoteViews.setTextViewText(R.id.notiThisSuccessRate, this.getString(R.string.notiThisSuccessRateString, (int) expectedRate));

        String successRate = this.getString(R.string.notiThisSuccessRateString, (int) expectedRate);

        String bigTextContent = usageTimeText + usageTime + "\n" +
                incentiveText + incentiveValue + "\n" +
                "오늘 획득/누적 획득: +" + todayGainGold + "골드 (+" + totalGainGold + "골드)" + "\n" +
                "성공률: " + successRate;

        timerNotifyBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(bigTextContent));
        timerNotificationManager.notify(Config.ONTIME_NOTIFICATION_ID, timerNotifyBuilder.build());
    }

    /** 골든타임 foreground 노티바 실시간 업데이트: 사용 중일 때만 **/
    private void updateTimer(int timeCount) {
        //remoteViews.setTextViewText(R.id.notiUsageTimeText, UtilitiesDateTimeProcess.getCurrentTimeHour()+"-"+(UtilitiesDateTimeProcess.getCurrentTimeHour()+1)+"시 사용시간: ");
        remoteViews.setTextViewText(R.id.notiUsageTime, getNotificationTimerFormat(timeCount));

        /*오늘 차감/누적 차감 데이터 로드 */
//        int todayFailNum = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(), "dailyFail");
//        int totalFail = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(), "totalFail");
//        DecimalFormat decimalFormat = new DecimalFormat("###,###");
//
//        /*누적차감*/
//        int totalLossGold = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(this, "TotalIncentive");
//        remoteViews.setTextViewText(R.id.notiTotalLossGoldStatisticsVal, "- "+totalLossGold+"골드");
//        String remainTotalGold = decimalFormat.format(178500 - totalLossGold);
//        remoteViews.setTextViewText(R.id.notiTotalRemainGoldStatisticsVal, "(총 "+remainTotalGold+"골드 남음)");
//
//        /*오늘차감*/
//        int todayLossGold = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(this, "TodayIncentive");
//        remoteViews.setTextViewText(R.id.notiTodayLossGoldStatisticsVal, "- "+todayLossGold+"골드,");
//
//        /*남은골드 세팅*/
//        goldSetup(todayFailNum, "#bebebe");

        timerNotificationManager.notify(Config.ONTIME_NOTIFICATION_ID, timerNotifyBuilder.build());
    }

    @SuppressLint("DefaultLocale")
    private CharSequence getNotificationTimerFormat(int durationSeconds) {
        String timeStr = String.format("%02d:%02d", (durationSeconds % 3600) / 60, (durationSeconds % 60));
        return timeStr;
    }

    private void startScreenOffForegroundService() {
        if(screenOnTimerObj!=null) {
            screenOnTimerObj.cancel();
            screenOnTimerObj.purge();
            screenOnTimerObj = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(backgroundReceiver!= null) {
            unregisterReceiver(backgroundReceiver);
            backgroundReceiver = null;
        }
    }
}

