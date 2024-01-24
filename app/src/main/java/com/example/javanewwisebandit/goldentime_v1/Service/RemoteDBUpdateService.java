package com.example.javanewwisebandit.goldentime_v1.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.javanewwisebandit.goldentime_v1.RoomDB.GoldenTimeDB.DailyStat;
import com.example.javanewwisebandit.goldentime_v1.RoomDB.GoldenTimeDB.GoldenTimeDB;
import com.example.javanewwisebandit.goldentime_v1.RoomDB.GoldenTimeDB.UsageStat;
import com.example.javanewwisebandit.goldentime_v1.RoomDB.GoldenTimeDB.UserInfo;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesDateTimeProcess;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesUsageStatsDataProcess;
//import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

//import kr.ac.kaist.jypark.goldentime_v1.RoomDB.GoldenTimeDB.DailyStat;
//import kr.ac.kaist.jypark.goldentime_v1.RoomDB.GoldenTimeDB.GoldenTimeDB;
//import kr.ac.kaist.jypark.goldentime_v1.RoomDB.GoldenTimeDB.UsageStat;
//import kr.ac.kaist.jypark.goldentime_v1.RoomDB.GoldenTimeDB.UserInfo;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesDateTimeProcess;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesUsageStatsDataProcess;

public class RemoteDBUpdateService extends Service {
    public static final String ACTION_INITIAL_DATA_UPDATE = "ACTION_INITIAL_DATA_UPDATE_SERVICE";
    public static final String ACTION_LOG_DATA_INSERT = "ACTION_LOG_DATA_UPDATE_SERVICE";
    public static final String ACTION_ONTIME_STATS_UPDATE = "ACTION_ONTIME_STATS_UPDATE_SERVICE";
    public static final String ACTION_APPUSAGE_STATS_UPDATE = "ACTION_APPUSAGE_STATS_UPDATE_SERVICE";
    public static final String ACTION_GET_INCENTIVE = "ACTION_GET_INCENTIVE";
    /** base server usrl **/
    public static final String BASE_SERVER_URL = "http://143.248.53.137:5000";
    /** 새로운 데이터 저장 서버 url **/
    public static final String EXPERIMENT_ADDRESS = BASE_SERVER_URL+"/usage_data";
    public static final String USER_INFO_ADDRESS=BASE_SERVER_URL;
    public static final String DAILY_STAT_ADDRESS=BASE_SERVER_URL;
    public static final String USAGE_STAT_ADDRESS=BASE_SERVER_URL;

    /** 공통 데이터 저장 서버 Url **/
    public static final String UPDATETIME_INSERT_SERVER_ADDRESS = BASE_SERVER_URL+"/goldentime/updatetime/insert";
    public static final String LOG_INSERT_SERVER_ADDRESS = BASE_SERVER_URL+"/goldentime/logdata/insert";

    /** Baseline 기간 데이터 저장할 서버 Url **/
    public static final String BASELINE_ADDRESS = BASE_SERVER_URL+"/baseline";

    /** Intervention 기간 데이터 저장할 서버 Url **/
    public static final String INTERVENTION_ADDRESS = BASE_SERVER_URL+"/intervention";

    public static RequestQueue requestQueue;

    public static final SimpleDateFormat sdf_all  = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    //Debugging log용
    private static final String TAG = "RemoteDBUpdateService";

    public RemoteDBUpdateService() { }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            Log.d(TAG, "onStartCommand()");
            String action = intent.getAction();
            boolean isAppChanged = UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getApplicationContext(),"isAppChange");
            boolean isUpdateAfterAppChange = UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getApplicationContext(),"isUpdateAfterAppChange");
            switch (action) {
                case ACTION_INITIAL_DATA_UPDATE:
                    String updateTimeStr = intent.getExtras().getString("updateTime");
                    String ustatsUpdateTimeStr = intent.getExtras().getString("ustatsUpdateTime");
                    saveOnTimeUpdateTimeMinSec(updateTimeStr, ustatsUpdateTimeStr,"InsertUpdateTime", UPDATETIME_INSERT_SERVER_ADDRESS);
                    /** userinfo에 등록 (Local & Remote) **/
                    saveUserInfo("UserInfo", EXPERIMENT_ADDRESS);

                    break;
                case ACTION_LOG_DATA_INSERT:
                    /*String logStr = intent.getExtras().getString("logData");
                    saveLogData(logStr, "LogData", LOG_INSERT_SERVER_ADDRESS);*/
                    break;
                case ACTION_ONTIME_STATS_UPDATE:
                    /** dailystats에 등록 (Local & Remote) **/
                    updateDailyUsageTimeLogData("UsageTimeDaily", EXPERIMENT_ADDRESS, isAppChanged, isUpdateAfterAppChange);
                    /*
                    if(isAppChanged && isUpdateAfterAppChange) {
                        //Invervention
                        updateDailyUsageTimeLogData("UsageTimeDaily", INTERVENTION_ADDRESS, isAppChanged, isUpdateAfterAppChange);
                        updateScreenEventLogData("ScreenEventDaily", INTERVENTION_ADDRESS);
                    }
                    else {
                        //Baseline
                        updateDailyUsageTimeLogData("UsageTimeDaily", BASELINE_ADDRESS, isAppChanged, isUpdateAfterAppChange);
                        updateScreenEventLogData("ScreenEventDaily", BASELINE_ADDRESS);
                    }*/
                    break;
                case ACTION_APPUSAGE_STATS_UPDATE:
                    /** sharedpref에 저장된 맨마지막 업데이트 시점(시간)으로부터 현재시간까지 루프 **/
                    /** usagestats에 등록 (Local & Remote) **/
                    int lastUpdateHour = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(), "lastAppUsageUpdateTime");
                    if(UtilitiesUsageStatsDataProcess.getUsageStatsData(getApplicationContext(), lastUpdateHour)) {
                        Log.d(TAG, "usagestat start");
                        updateUsageStatsRawData("UsageStatsRawData", EXPERIMENT_ADDRESS);
                        /*
                        if(isAppChanged && isUpdateAfterAppChange)    updateUsageStatsRawData("UsageStatsRawData", INTERVENTION_ADDRESS); //intervention
                        else                updateUsageStatsRawData("UsageStatsRawData", BASELINE_ADDRESS); //baseline */
                    }
                    break;
            }
        }

        return START_NOT_STICKY;
    }

    private void saveUserInfo(String sendObjStr, String serverUrl){
        /** RemoteDB 업데이트 하는 곳 **/
        String userName = UtilitiesSharedPrefDataProcess.getStringSharedPrefData(getApplicationContext(), "userName");
        try {
            JSONObject totalObject = new JSONObject();
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            jsonObject.put("UserName", userName);
            jsonObject.put("Frame", 1);  //TODO: 그룹별 숫자 확인
            jsonArray.put(jsonObject);

            totalObject.put(sendObjStr, jsonArray);
            saveDataToRemoteDB(totalObject, sendObjStr, serverUrl);
        } catch (JSONException e) { e.printStackTrace();}

        /** LocalDB 업데이트 하는 곳 **/
        class InsertRunnable implements Runnable {

            @Override
            public void run() {
                UserInfo userInfo = new UserInfo();
                userInfo.user = userName;
                userInfo.frame = 1;
                userInfo.updated = sdf_all.format(new Date());

                GoldenTimeDB.getInstance(getApplicationContext()).userInfoDao().insertAll(userInfo);
                Log.d(TAG, "userInfo insert");
            }
        }
        InsertRunnable insertRunnable = new InsertRunnable();
        Thread addThread = new Thread(insertRunnable);
        addThread.start();
    }

    /*
    private void saveLogData(String logStr, String sendObjStr, String serverURL) {
        try {
            JSONObject totalObj = new JSONObject();
            JSONArray totalOnTimePointStatsArry;
            totalOnTimePointStatsArry = createLogDataObject(logStr);
            if(totalOnTimePointStatsArry!=null) {
                totalObj.put(sendObjStr, totalOnTimePointStatsArry);
                saveDataToRemoteDB(totalObj,sendObjStr,serverURL);
            }

        } catch (JSONException e) { e.printStackTrace(); }
    }

    private JSONArray createLogDataObject(String logStr) {
        String userName = UtilitiesSharedPrefDataProcess.getStringSharedPrefData(getApplicationContext(), "userName");
        if(logStr != null) {
            try {
                JSONArray resultJSONArry = new JSONArray();
                JSONObject logJSONObject = new JSONObject();

                logJSONObject.put("UserName",userName);
                logJSONObject.put("Log", logStr);
                logJSONObject.put("Frame", 3);  //나중에 각 그룹별로 apk배포시 Type 숫자 넣어주기(1: loss, 2: gain, 3: control)
                resultJSONArry.put(logJSONObject);

                return resultJSONArry;

            } catch (JSONException e) { e.printStackTrace(); return null;}
        }
        return null;
    }*/

    /** UsageStats RawData 업데이트하는 함수 **/
    private void updateUsageStatsRawData(String sendObjStr, String serverURL) {

        class InsertRunnable implements Runnable {
            final JSONObject jsonObject;

            public InsertRunnable(JSONObject jsonObject){
                this.jsonObject = jsonObject;
            }

            @Override
            public void run() {
                try {
                    UsageStat usageStat = new UsageStat();
                    usageStat.user = jsonObject.getString("UserName");
                    usageStat.date = jsonObject.getString("Date");
                    usageStat.timeSlot = jsonObject.getInt("TimeSlot");
                    usageStat.usageTime = jsonObject.getInt("UsageTime");
                    usageStat.appPackage = jsonObject.getString("AppPackage");
                    usageStat.frame = jsonObject.getInt("Frame");
                    usageStat.period = jsonObject.getString("Period");
                    usageStat.updated = sdf_all.format(new Date());

                    GoldenTimeDB.getInstance(getApplicationContext()).usageStatDao().insertAll(usageStat);
                    Log.d(TAG, "usageStat insert");
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }

        try {
            JSONObject totalObj = new JSONObject();
            JSONArray totalOnTimePointStatsArry;
            totalOnTimePointStatsArry = createUsageStatsObject(sendObjStr);
            Log.d(TAG, "object not null? :" + (totalOnTimePointStatsArry == null));
            if(totalOnTimePointStatsArry!=null) {
                totalObj.put(sendObjStr, totalOnTimePointStatsArry);
                saveDataToRemoteDB(totalObj,sendObjStr,serverURL);

                for (int i = 0; i < totalOnTimePointStatsArry.length(); ++i) {
                    JSONObject rec = totalOnTimePointStatsArry.getJSONObject(i);
                    InsertRunnable insertRunnable = new InsertRunnable(rec);
                    Thread addThread = new Thread(insertRunnable);
                    addThread.start();
                    addThread.join();
                }
            }

        } catch (JSONException | InterruptedException e) { e.printStackTrace(); }

    }

    /** 원격 DB에 저정할 UsageStats 오브젝트를 만드는 함수 **/
    private JSONArray createUsageStatsObject(String sendObjStr) {
        String eachOnTimeJSONStr = UtilitiesSharedPrefDataProcess.getStringSharedPrefData(getApplicationContext(),sendObjStr);
        String userName = UtilitiesSharedPrefDataProcess.getStringSharedPrefData(getApplicationContext(), "userName");
        Log.d(TAG, "sharedPreference null? : "+ (eachOnTimeJSONStr == null));
        if(eachOnTimeJSONStr != null) {
            try {
                JSONArray jsonArray = new JSONArray(eachOnTimeJSONStr);
                JSONArray resultJSONArry = new JSONArray();

                String period = "intervention";
                if (UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getApplicationContext(), "isFollowup"))
                    period = "followup";

                for(int i=0;i<jsonArray.length();i++) {
                    JSONObject subJsonObject = jsonArray.getJSONObject(i);
                    JSONObject eachSlotJSONObject = new JSONObject();
                    String appPackage = subJsonObject.getString("name");

                    eachSlotJSONObject.put("UserName",userName);
                    eachSlotJSONObject.put("Date", subJsonObject.getString("date"));
                    eachSlotJSONObject.put("TimeSlot", subJsonObject.getInt("slot"));
                    eachSlotJSONObject.put("AppPackage", appPackage);
                    eachSlotJSONObject.put("UsageTime", subJsonObject.getInt("time"));
                    eachSlotJSONObject.put("Frame", 1);  //TODO: 그룹별 숫자 확인
                    eachSlotJSONObject.put("Period", period);

                    resultJSONArry.put(eachSlotJSONObject);
                }
                return resultJSONArry;

            } catch (JSONException e) { e.printStackTrace(); return null;}
        }
        return null;

    }

    /** 원격 DB에 저정할 UsageStats RawData 오브젝트를 만드는 함수 **/
    /*private JSONArray createUsageStatsRawDataObject(String sendObjStr) {
        String eachOnTimeJSONStr = UtilitiesSharedPrefDataProcess.getStringSharedPrefData(getApplicationContext(),sendObjStr);
        String userName = UtilitiesSharedPrefDataProcess.getStringSharedPrefData(getApplicationContext(), "userName");
        if(eachOnTimeJSONStr != null) {
            try {
                JSONArray jsonArray = new JSONArray(eachOnTimeJSONStr);
                JSONArray resultJSONArry = new JSONArray();

                for(int i=0;i<jsonArray.length();i++) {
                    JSONObject subJsonObject = jsonArray.getJSONObject(i);
                    JSONObject eachSlotJSONObject = new JSONObject();
                    String appPackage = subJsonObject.getString("name");
                    String appName = UtilitiesUsageStatsDataProcess.convertAppPackageNameToAppName(getApplicationContext(),appPackage);

                    eachSlotJSONObject.put("UserName",userName);
                    eachSlotJSONObject.put("AppName", appName);
                    eachSlotJSONObject.put("AppPackage", appPackage);
                    eachSlotJSONObject.put("EventType", subJsonObject.getString("event"));
                    eachSlotJSONObject.put("EventTime", subJsonObject.getString("time"));
                    eachSlotJSONObject.put("Frame", 1);
                    eachSlotJSONObject.put("Period", "intervention");

                    resultJSONArry.put(eachSlotJSONObject);
                }
                return resultJSONArry;

            } catch (JSONException e) { e.printStackTrace(); return null;}
        }
        return null;

    }*/

    /** 업데이트 되는 시간(분,초)을 저장하는 함수 **/
    private void saveOnTimeUpdateTimeMinSec(String updateTimeStr,String ustatsUpdateTimeStr, String sendObjStr, String serverURL) {
        try {
            JSONObject totalObj = new JSONObject();
            JSONArray updateTimeArry;
            updateTimeArry = createUpdateTimeObject(updateTimeStr,ustatsUpdateTimeStr);
            if(updateTimeArry!=null) {
                totalObj.put(sendObjStr, updateTimeArry);
                saveDataToRemoteDB(totalObj,sendObjStr,serverURL);
            }

        } catch (JSONException e) { e.printStackTrace(); }
    }

    /** 원격 DB에 저장할 업데이트 되는 시간(분,초) 오브젝트를 만드는 함수 **/
    private JSONArray createUpdateTimeObject(String updateTimeStr, String ustatsUpdateTimeStr) {
        String userName = UtilitiesSharedPrefDataProcess.getStringSharedPrefData(getApplicationContext(), "userName");

        try {
            JSONObject updateTimeObj = new JSONObject();
            JSONArray resultJSONArry = new JSONArray();

            updateTimeObj.put("UserName",userName);
            updateTimeObj.put("UpdateTime", updateTimeStr);
            updateTimeObj.put("UsageStatsUpdateTime", ustatsUpdateTimeStr);
            updateTimeObj.put("Frame", 1);  //TODO: 그룹별 숫자 확인
            resultJSONArry.put(updateTimeObj);

            return resultJSONArry;
        } catch (JSONException e) { e.printStackTrace(); return null;}
    }


    /** 일일 전체 사용시간 통계 업데이트 함수 **/
    /*private void updateTotalUsageTimeLogData(String sendObjStr, String dateStr, String serverURL) {
        try {
            JSONObject totalObj = new JSONObject();
            JSONArray totalOnTimePointStatsArry = createTotalOnTimePointStatsObject(dateStr);
            if(totalOnTimePointStatsArry!=null) {
                totalObj.put(sendObjStr, totalOnTimePointStatsArry);
                saveDataToRemoteDB(totalObj,sendObjStr,serverURL);
            }

        } catch (JSONException e) { e.printStackTrace(); }
    }*/

    /** 원격 DB 업데이트를 위해 일일 전체 사용시간 오브젝트 만드는 함수 **/
    /*private JSONArray createTotalOnTimePointStatsObject(String dateStr) {
        String totalOnTimeJSONStr = UtilitiesSharedPrefDataProcess.getStringSharedPrefData(getApplicationContext(),"UsageTimeTotal");
        String userName = UtilitiesSharedPrefDataProcess.getStringSharedPrefData(getApplicationContext(), "userName");
        int totalSuccess = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(),"totalSuccess");
        int totalFail = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(),"totalFail");

        if(totalOnTimeJSONStr != null) {
            try {
                JSONObject tmpObj = new JSONObject(totalOnTimeJSONStr);
                if(tmpObj.isNull(dateStr))  return null;
                String dateObj = tmpObj.getString(dateStr);
                JSONArray jsonArray = new JSONArray(dateObj);
                JSONArray resultJSONArry = new JSONArray();

                for(int i=0;i<jsonArray.length();i++) {
                    JSONObject subJsonObject = jsonArray.getJSONObject(i);
                    JSONObject eachSlotJSONObject = new JSONObject();

                    eachSlotJSONObject.put("UserName",userName);
                    eachSlotJSONObject.put("Date", UtilitiesDateTimeProcess.getDateByDBDateFormat(dateStr));
                    eachSlotJSONObject.put("TimeSlot", subJsonObject.getInt("slot"));
                    eachSlotJSONObject.put("UsageTime", subJsonObject.getInt("time"));
                    eachSlotJSONObject.put("Success", subJsonObject.getInt("success"));
                    eachSlotJSONObject.put("Fail", subJsonObject.getInt("fail"));
                    eachSlotJSONObject.put("TotalSuccess", totalSuccess);
                    eachSlotJSONObject.put("TotalFail", totalFail);
                    eachSlotJSONObject.put("Frame", 3);  //나중에 각 그룹별로 apk배포시 Type 숫자 넣어주기(1: loss, 2: gain, 3: control)

                    resultJSONArry.put(eachSlotJSONObject);
                }
                return resultJSONArry;

            } catch (JSONException e) { e.printStackTrace(); return null;}
        }
        return null;
    }*/

    /** 각 타임슬롯 별 앱 사용시간 통계 업데이트 함수(매 시간 호출) **/
    private void updateDailyUsageTimeLogData(String sendObjStr, String serverURL, boolean isAppChanged, boolean isUpdateAfterAppChange) {
        //sendObjStr == "UsageTimeDaily"
        Log.d(TAG, "UsageTimeDaily");
        class InsertRunnable implements Runnable {
            final JSONObject jsonObject;

            public InsertRunnable(JSONObject jsonObject){
                this.jsonObject = jsonObject;
            }

            @Override
            public void run() {
                try {
                    DailyStat dailyStat = new DailyStat();
                    dailyStat.user = jsonObject.getString("UserName");
                    dailyStat.date = jsonObject.getString("Date");
                    dailyStat.timeSlot = jsonObject.getInt("TimeSlot");
                    dailyStat.usageTime = jsonObject.getInt("UsageTime");
                    dailyStat.success = jsonObject.getInt("Success");
                    dailyStat.incentive = jsonObject.getInt("Incentive");
                    dailyStat.frame = jsonObject.getInt("Frame");
                    dailyStat.period = jsonObject.getString("Period");
                    dailyStat.updated = sdf_all.format(new Date());

                    GoldenTimeDB.getInstance(getApplicationContext()).dailyStatDao().insertAll(dailyStat);
                    Log.d(TAG, "dailyStat insert");
                } catch (JSONException e) { e.printStackTrace(); }
            }
        }

        try {
            JSONObject totalObj = new JSONObject();
            JSONArray eachOnTimePointStatsArry;
            String todayDateStr;
            int currentHour = UtilitiesDateTimeProcess.getCurrentTimeHour();

            //현재 시간을 기준으로 DB에 날짜를 어떻게 셋업할 지 판단(0-2시: 어제날짜, 9-23시: 오늘날짜)
            if(currentHour <= 2) {
                todayDateStr = UtilitiesDateTimeProcess.getPreviousDateByDateFormat(UtilitiesDateTimeProcess.getTodayDateByDateFormat());
                /** 새벽 2시타임(2~3시 사이)에는 하루 총 사용시간 통계를 업데이트함 **/
                /*if(currentHour == 2) {
                    if(isAppChanged && isUpdateAfterAppChange)    updateTotalUsageTimeLogData("UsageTimeTotal", todayDateStr, INTERVENTION_ADDRESS); //intervention
                    else                updateTotalUsageTimeLogData("UsageTimeTotal", todayDateStr, BASELINE_ADDRESS); //baseline
                }*/
            }
            else {
                todayDateStr = UtilitiesDateTimeProcess.getTodayDateByDateFormat();
                /** 이전에 DB 업데이트가 안되서 남아있는 로그가 있다면 먼저 업데이트함(하루 전까지만 커버) **/
                String yesterdayDateStr = UtilitiesDateTimeProcess.getPreviousDateByDateFormat(todayDateStr);
                eachOnTimePointStatsArry = createUsageTimeLogStatsObject(yesterdayDateStr, sendObjStr);
                if(eachOnTimePointStatsArry!=null) {
                    totalObj.put(sendObjStr, eachOnTimePointStatsArry);
                    saveDataToRemoteDB(totalObj, sendObjStr, serverURL);
                    for (int i = 0; i < eachOnTimePointStatsArry.length(); ++i) {
                        JSONObject rec = eachOnTimePointStatsArry.getJSONObject(i);
                        InsertRunnable insertRunnable = new InsertRunnable(rec);
                        Thread addThread = new Thread(insertRunnable);
                        addThread.start();
                        addThread.join();
                    }
                }
            }

            Log.d(TAG, "daily update start?");
            eachOnTimePointStatsArry = createUsageTimeLogStatsObject(todayDateStr, sendObjStr);
            Log.d(TAG, "null? : " + (eachOnTimePointStatsArry == null));
            if(eachOnTimePointStatsArry!=null) {
                Log.d(TAG, "array: " + eachOnTimePointStatsArry.toString());
                totalObj.put(sendObjStr, eachOnTimePointStatsArry);
                saveDataToRemoteDB(totalObj, sendObjStr, serverURL);
                for (int i = 0; i < eachOnTimePointStatsArry.length(); ++i) {
                    JSONObject rec = eachOnTimePointStatsArry.getJSONObject(i);
                    InsertRunnable insertRunnable = new InsertRunnable(rec);
                    Thread addThread = new Thread(insertRunnable);
                    addThread.start();
                    addThread.join();
                }
            }

        } catch (JSONException | InterruptedException e) { e.printStackTrace(); }
    }

    /** 원격 DB 업데이트를 위해 각 타임슬롯의 앱 사용시간 오브젝트 만드는 함수 **/
    private JSONArray createUsageTimeLogStatsObject(String dateStr, String sendObjStr) {
        String eachOnTimeJSONStr = UtilitiesSharedPrefDataProcess.getStringSharedPrefData(getApplicationContext(),sendObjStr);
        String userName = UtilitiesSharedPrefDataProcess.getStringSharedPrefData(getApplicationContext(), "userName");

        if(eachOnTimeJSONStr != null) {
            try {
                JSONObject tmpObj = new JSONObject(eachOnTimeJSONStr);
                if(tmpObj.isNull(dateStr))  return null;    //해당 날짜(키) 데이터가 없으면 리턴
                String dateObj = tmpObj.getString(dateStr);
                JSONArray jsonArray = new JSONArray(dateObj);
                JSONArray resultJSONArry = new JSONArray();

                String period = "intervention";
                if (UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getApplicationContext(), "isFollowup"))
                    period = "followup";

                for(int i=0;i<jsonArray.length();i++) {
                    JSONObject subJsonObject = jsonArray.getJSONObject(i);
                    JSONObject eachSlotJSONObject = new JSONObject();

                    eachSlotJSONObject.put("UserName",userName);
                    eachSlotJSONObject.put("Date", UtilitiesDateTimeProcess.getDateByDBDateFormat(dateStr));
                    eachSlotJSONObject.put("TimeSlot", subJsonObject.getInt("slot"));
                    eachSlotJSONObject.put("UsageTime", subJsonObject.getInt("time"));
                    eachSlotJSONObject.put("Success", UtilitiesSharedPrefDataProcess.convertBooleanToInteger(subJsonObject.getBoolean("success")));
                    eachSlotJSONObject.put("Frame", 1);  //TODO: 그룹별 숫자 확인
                    eachSlotJSONObject.put("Incentive", subJsonObject.getInt("incentive"));
                    eachSlotJSONObject.put("Period", period);

                    resultJSONArry.put(eachSlotJSONObject);
                }

                return resultJSONArry;

            } catch (JSONException e) { e.printStackTrace(); return null;}
        }
        return null;
    }

    /** 원격 DB 업데이트를 위해 각 타임슬롯의 앱 사용시간 오브젝트 만드는 함수 **/
    /*private void updateScreenEventLogData(String sendObjStr, String serverURL) {
        try {
            JSONObject totalObj = new JSONObject();
            JSONArray eachOnTimePointStatsArry;

            eachOnTimePointStatsArry = createEventStatsObject(sendObjStr);
            if(eachOnTimePointStatsArry!=null) {
                totalObj.put(sendObjStr, eachOnTimePointStatsArry);
                saveDataToRemoteDB(totalObj, sendObjStr, serverURL);
            }

        } catch (JSONException e) { e.printStackTrace(); }
    }*/

    /*private JSONArray createEventStatsObject(String sendObjStr) {
        String totalOnTimeJSONStr = UtilitiesSharedPrefDataProcess.getStringSharedPrefData(getApplicationContext(),sendObjStr);
        String userName = UtilitiesSharedPrefDataProcess.getStringSharedPrefData(getApplicationContext(), "userName");

        if(totalOnTimeJSONStr != null) {
            try {
                JSONObject tmpObj = new JSONObject(totalOnTimeJSONStr);
                String dateObj = tmpObj.getString(sendObjStr);
                JSONArray jsonArray = new JSONArray(dateObj);
                JSONArray resultJSONArry = new JSONArray();

                for(int i=0;i<jsonArray.length();i++) {
                    JSONObject subJsonObject = jsonArray.getJSONObject(i);
                    JSONObject eachSlotJSONObject = new JSONObject();

                    eachSlotJSONObject.put("UserName",userName);
                    eachSlotJSONObject.put("EventType", subJsonObject.getString("type"));
                    eachSlotJSONObject.put("EventTime", subJsonObject.getString("time"));
                    eachSlotJSONObject.put("Frame", 3);  //나중에 각 그룹별로 apk배포시 Type 숫자 넣어주기(1: loss, 2: gain, 3: control)

                    resultJSONArry.put(eachSlotJSONObject);
                }
                return resultJSONArry;

            } catch (JSONException e) { e.printStackTrace(); return null;}
        }
        return null;
    }*/


    /** 원격 DB 연결 및 데이터 전송 함수 **/
    private boolean saveDataToRemoteDB(JSONObject dataObj, final String sendObjStr ,final String serverUrl) {

        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        Log.d(TAG, "dataObj: " + dataObj);

        try {
            final String requestBody = dataObj.toString();
            if (dataObj == null || sendObjStr.isEmpty() || serverUrl.isEmpty()) return false;

            StringRequest request = new StringRequest(
                    Request.Method.POST,
                    serverUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "응답-> " + response);

                            if(response.equals("200")){
                                if(!sendObjStr.equals("UsageTimeTotal"))
                                    UtilitiesSharedPrefDataProcess.setStringDataToSharedPref(getApplicationContext(), sendObjStr, null);
                                if(sendObjStr.equals("UsageStatsRawData"))
                                    UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(getApplicationContext(),"lastAppUsageUpdateTime", UtilitiesDateTimeProcess.getCurrentTimeHour());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                Log.e(TAG, "에러 상태 코드: " + error.networkResponse.statusCode);
                                Log.e(TAG, "에러 본문: " + new String(error.networkResponse.data, StandardCharsets.UTF_8));
                            } else {
                                Log.e(TAG, "네트워크 응답 없음, 에러: " + error.toString());
                            }
                            // 오류 스택 트레이스 로깅
                            error.printStackTrace();
                            // FirebaseCrashlytics.getInstance().recordException(error);
                        }

                    }
            ) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            request.setShouldCache(false);
            requestQueue.add(request);

            request.setRetryPolicy(new DefaultRetryPolicy(
                    5000, // 타임아웃 시간 (밀리초)
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // 최대 재시도 횟수
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(request);
        } catch(Exception e){
            e.printStackTrace();
//            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return true;
    }
}