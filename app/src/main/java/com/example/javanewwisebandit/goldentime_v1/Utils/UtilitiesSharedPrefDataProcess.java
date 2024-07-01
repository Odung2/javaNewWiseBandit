package com.example.javanewwisebandit.goldentime_v1.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase.AppDatabaseInsertThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

//import kr.ac.kaist.jypark.goldentime_v1.RoomDB.AppDatabase.AppDatabaseInsertThread;

public class UtilitiesSharedPrefDataProcess {

    private static final String TAG = "UtilitiesSharedPrefDataProcess";

    /** 이메일 등록되었는 지 체크 **/
    public static boolean checkRegisterEmailStatus(Context context) {
        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        boolean emailRegisterStatus = pref.getBoolean("emailRegisterStatus", false);
        return emailRegisterStatus;
    }

    /** 앱 설치 후 첫 메인화면 실행 여부 체크(베이스라인 앱에서 사용 **/
    public static boolean checkFirstMainPageExecution(Context context) {
        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        boolean firstMainPageStatus = pref.getBoolean("firstMainPage", true);
        return firstMainPageStatus;
    }

    /** 앱 설치 후 첫 메인페이지 화면 등장 시 flag 값 변경(baseline앱에서 사용) **/
    public static void changeFirstMainPageFlagVal(Context context) {
        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = pref.edit();
        editor.putBoolean("firstMainPage", false);
        editor.apply();
    }

    /** boolean 타입의 sharedpref 변수 값 추출 **/
    public static boolean getBooleanSharedPrefData(Context context, String dataKey) {
        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        boolean data = pref.getBoolean(dataKey, false);
        return data;
    }

    /** key값을 입력으로 넣어서 해당 JSON 데이터가 존재하는 지 파악 **/
    public static String getStringSharedPrefData(Context context, String dataKey) {
        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        String data = pref.getString(dataKey, null);
        return data;
    }

    /** integer 타입의 sharedpref 변수 값 추출 **/
    public static int getIntegerSharedPrefData(Context context, String dataKey) {
        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        int data = pref.getInt(dataKey, 0);
        return data;
    }

    /** Long 타입의 sharedpref 변수 값 추출 **/
    public static long getLongDataToSharedPref(Context context, String dataKey) {
        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        long data = pref.getLong(dataKey, 0L);
        return data;
    }


    /** boolean 타입의 sharedpref 변수 값 저장 **/
    public static void setBooleanDataToSharedPref(Context context, String dataKey, boolean value) {
        SharedPreferences pref;
        SharedPreferences.Editor editor;
        pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor = pref.edit();
        editor.putBoolean(dataKey, value);
        editor.apply();
    }

    /** Integer 타입의 sharedpref 변수 값 저장 **/
    public static void setIntegerDataToSharedPref(Context context, String dataKey, int value) {
        SharedPreferences pref;
        SharedPreferences.Editor editor;
        pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor = pref.edit();
        editor.putInt(dataKey, value);
        editor.apply();
    }

    /** Long 타입의 sharedpref 변수 값 저장 **/
    public static void setLongDataToSharedPref(Context context, String dataKey, long value) {
        SharedPreferences pref;
        SharedPreferences.Editor editor;
        pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor = pref.edit();
        editor.putLong(dataKey, value);
        editor.apply();
    }


    /** JSON 데이터를 sharedpref 변수에 저장 **/
    public static void setStringDataToSharedPref(Context context, String dataKey, String dataStr) {
        SharedPreferences pref;
        SharedPreferences.Editor editor;
        pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor = pref.edit();
        editor.putString(dataKey, dataStr);
        editor.apply();
    }

    /** Screen Event 로그 데이터를 sharedpref 변수에 저장 **/
    public static void saveEventToSharedPref(Context context, String eventType) {
        String jsonStr = getStringSharedPrefData(context, "ScreenEventDaily");

        try {
            JSONObject allHistoryObj;
            JSONArray jsonArray;
            if(jsonStr!=null) {
                allHistoryObj = new JSONObject(jsonStr);

                if (allHistoryObj.isNull("ScreenEventDaily")) jsonArray = new JSONArray();
                else {
                    String dateObj = allHistoryObj.getString("ScreenEventDaily");
                    jsonArray = new JSONArray(dateObj);
                }
            }
            else {
                allHistoryObj = new JSONObject();
                jsonArray = new JSONArray();
            }

            JSONObject dayStatistics = new JSONObject();
            dayStatistics.put("type", eventType);
            dayStatistics.put("time",  UtilitiesDateTimeProcess.getCurrentTimeByFullDateFormat());
            jsonArray.put(dayStatistics);

            allHistoryObj.put("ScreenEventDaily", jsonArray);
            setStringDataToSharedPref(context, "ScreenEventDaily", allHistoryObj.toString());

        } catch (JSONException e) {}
    }


    /** 성공,실패 횟수 값 업데이트 **/
    public static void setSuccessFailCountDataToSharedPref(Context context, String dataKey) {
        SharedPreferences pref;
        SharedPreferences.Editor editor;
        pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        int resultData = pref.getInt(dataKey, 0);
        resultData++;
        editor = pref.edit();
        editor.putInt(dataKey, resultData);
        editor.apply();
    }

    /** 이전 타임슬롯 사용 통계 업데이트해주는 함수 **/
    public static String updatePreviousTimeSlotUsageData(Context context, int currentTimeSlot, int usageTime) {
        String totalOnTimeJSONStr = getStringSharedPrefData(context,"UsageTimeDaily");
        String dateStr;
        currentTimeSlot--;
        if(currentTimeSlot <= 2) {
            dateStr = UtilitiesDateTimeProcess.getPreviousDateByDateFormat(UtilitiesDateTimeProcess.getTodayDateByDateFormat());
            if(currentTimeSlot < 0) currentTimeSlot = 23;
        }
        else {
            dateStr = UtilitiesDateTimeProcess.getTodayDateByDateFormat();
        }

        try {
            JSONObject tmpObj;
            JSONArray jsonArray;
            boolean isSuccess = true;
            if(totalOnTimeJSONStr!=null) {
                tmpObj = new JSONObject(totalOnTimeJSONStr);

                if (tmpObj.isNull(dateStr)) jsonArray = new JSONArray();
                else {
                    String dateObj = tmpObj.getString(dateStr);
                    jsonArray = new JSONArray(dateObj);
                }
            }
            else {
                tmpObj = new JSONObject();
                jsonArray = new JSONArray();
            }

            if(!calculateUsageTimeSuccess(usageTime))     isSuccess = false;

            JSONObject statistics = new JSONObject();
            statistics.put("slot", currentTimeSlot);
            statistics.put("time", usageTime);
            statistics.put("success", isSuccess);
            statistics.put("incentive", UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(context, "incentive"));


            if(!(currentTimeSlot >= 2 && currentTimeSlot <= 8)){
                Log.d(TAG, "Room DB debugging, targeting incentive: " + UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(context, "incentive"));

                if(isSuccess){
                    AppDatabaseInsertThread thread = new AppDatabaseInsertThread(context, currentTimeSlot, UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(context, "incentive"), true, dateStr);
                    Log.d("AA", "Insert incentive Thread state: "+thread.getState());
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        //TODO: handle exception
                        Log.d("AA", e.toString());
                    }
                } else {
                    Log.d("AA", "Room DB Debugging, 이미 저장된 데이터(fail)");
                }

            }

            jsonArray.put(statistics);
            tmpObj.put(dateStr, jsonArray);

            setStringDataToSharedPref(context,"UsageTimeDaily",tmpObj.toString());
            return dateStr;
        } catch (JSONException e) {e.printStackTrace(); return null;}
    }


    /** 이전 타임슬롯까지의 총 사용 통계 업데이트해주는 함수 **/
    public static void updateTotalOnTimeUsageData(Context context, int currentTimeSlot, int currentTimeSlotUsageTime, boolean isAppChanged) {
        if(currentTimeSlotUsageTime >=3600) currentTimeSlotUsageTime = 3600;

        String todayDateStr = updatePreviousTimeSlotUsageData(context,currentTimeSlot,currentTimeSlotUsageTime);
        /** 중재기간 대시보드 시각화를 위해 별도의 sharedpref(key: "DashboardDaily") 변수 저장**/
        if(isAppChanged && UtilitiesDateTimeProcess.checkDashboardTime()) {
            boolean isUpdateAfterAppChange = getBooleanSharedPrefData(context, "isUpdateAfterAppChange");
            if(!isUpdateAfterAppChange) {
                setBooleanDataToSharedPref(context, "isUpdateAfterAppChange", true);
                return;
            }
            else    updateDashboardUsageStatistics(context, todayDateStr, currentTimeSlotUsageTime);
        }

        if(UtilitiesDateTimeProcess.checkDashboardTime()) {
            String totalOnTimeJSONStr = getStringSharedPrefData(context,"UsageTimeTotal");
            int timeSlot = UtilitiesDateTimeProcess.getConvertedPreviousTimeSlot(currentTimeSlot);

            try {
                JSONObject tmpObj;
                JSONArray jsonArray;
                int totalUsgeTime = 0;
                int successNum = 0;
                int failNum = 0;
                if(totalOnTimeJSONStr!=null) {
                    tmpObj = new JSONObject(totalOnTimeJSONStr);
                    if (tmpObj.isNull(todayDateStr)) jsonArray = new JSONArray();
                    else {
                        String dateObj = tmpObj.getString(todayDateStr);
                        jsonArray = new JSONArray(dateObj);
                        JSONObject subJsonObject = jsonArray.getJSONObject(0);
                        totalUsgeTime = subJsonObject.getInt("time");
                        successNum = subJsonObject.getInt("success");
                        failNum = subJsonObject.getInt("fail");
                    }
                }
                else {
                    tmpObj = new JSONObject();
                    jsonArray = new JSONArray();
                }


                if(calculateUsageTimeSuccess(currentTimeSlotUsageTime)) {
                    successNum = getIntegerSharedPrefData(context, "totalSuccess");
                    setIntegerDataToSharedPref(context,"totalSuccess", ++successNum);
                }
                else {
                    failNum = getIntegerSharedPrefData(context, "totalFail");
                    if(!isAppChanged) setIntegerDataToSharedPref(context,"totalFail", ++failNum);
                }



                JSONObject statistics = new JSONObject();
                statistics.put("slot", timeSlot);
                statistics.put("time", totalUsgeTime + currentTimeSlotUsageTime);
                statistics.put("success", successNum);
                statistics.put("fail", failNum);

                jsonArray.put(0, statistics);
                tmpObj.put(todayDateStr, jsonArray);
                setStringDataToSharedPref(context,"UsageTimeTotal",tmpObj.toString());

            } catch (JSONException e) {e.printStackTrace();}
        }

    }

    /** 폰 껐다 다시 킨 경우: 이전의 각 타임슬롯 시점까지의 총 사용 통계 업데이트해주는 함수 **/
    public static void makeupUpdateTotalOnTimeUsageData(Context context, int currentTimeSlot, int currentTimeSlotUsageTime, boolean isAppChanged) {
        if(currentTimeSlotUsageTime >=3600) currentTimeSlotUsageTime = 3600;

        /* 날짜 매칭 */
        String dateStr="";
        if(currentTimeSlot==0||currentTimeSlot==1) {
            dateStr = UtilitiesDateTimeProcess.getPreviousDateByDateFormat(UtilitiesDateTimeProcess.getTodayDateByDateFormat());
            //currentTimeSlot+=24;
        }
        else    dateStr = UtilitiesDateTimeProcess.getTodayDateByDateFormat();

        /** 중재기간 대시보드 시각화를 위해 별도의 sharedpref(key: "DashboardDaily") 변수 저장**/
        if(isAppChanged && UtilitiesDateTimeProcess.checkDashboardTime()) {
            boolean isUpdateAfterAppChange = getBooleanSharedPrefData(context, "isUpdateAfterAppChange");
            if(!isUpdateAfterAppChange) {
                setBooleanDataToSharedPref(context, "isUpdateAfterAppChange", true);
                return;
            }
            else    updateDashboardUsageStatistics(context, dateStr, currentTimeSlotUsageTime);
        }


        /* 현재 매개변수 타임슬롯 사용통계 저장*/
        makeupUpdateCurrentTimeUsageData(context,dateStr,currentTimeSlot,currentTimeSlotUsageTime);

        if(UtilitiesDateTimeProcess.checkDashboardTime()) {
            /* 현재 매개변수 타임슬롯시점까지의 사용통계 저장*/
            String totalOnTimeJSONStr = getStringSharedPrefData(context,"UsageTimeTotal");

            try {
                JSONObject tmpObj;
                JSONArray jsonArray;
                int totalUsgeTime = 0;
                int successNum = 0;
                int failNum = 0;
                if(totalOnTimeJSONStr!=null) {
                    tmpObj = new JSONObject(totalOnTimeJSONStr);
                    if (tmpObj.isNull(dateStr)) jsonArray = new JSONArray();
                    else {
                        String dateObj = tmpObj.getString(dateStr);
                        jsonArray = new JSONArray(dateObj);
                        JSONObject subJsonObject = jsonArray.getJSONObject(0);
                        totalUsgeTime = subJsonObject.getInt("time");
                        successNum = subJsonObject.getInt("success");
                        failNum = subJsonObject.getInt("fail");
                    }
                }
                else {
                    tmpObj = new JSONObject();
                    jsonArray = new JSONArray();
                }


                if(calculateUsageTimeSuccess(currentTimeSlotUsageTime)) {
                    successNum = getIntegerSharedPrefData(context, "totalSuccess");
                    setIntegerDataToSharedPref(context,"totalSuccess", ++successNum);
                }
                else {
                    failNum = getIntegerSharedPrefData(context, "totalFail");
                    if(!isAppChanged) setIntegerDataToSharedPref(context,"totalFail", ++failNum);
                }


                JSONObject statistics = new JSONObject();
                statistics.put("slot", currentTimeSlot);
                statistics.put("time", totalUsgeTime + currentTimeSlotUsageTime);
                statistics.put("success", successNum);
                statistics.put("fail", failNum);

                jsonArray.put(0, statistics);
                tmpObj.put(dateStr, jsonArray);
                setStringDataToSharedPref(context,"UsageTimeTotal",tmpObj.toString());

            } catch (JSONException e) {e.printStackTrace();}
        }
    }

    /** 폰 껐다 다시 킨 경우: 현재 매겨변수 시간타임슬롯에 대한 사용 통계 업데이트해주는 함수 **/
    private static void makeupUpdateCurrentTimeUsageData(Context context, String dateStr, int currentTimeSlot, int usageTime) {
        String totalOnTimeJSONStr = getStringSharedPrefData(context,"UsageTimeDaily");
        try {
            JSONObject tmpObj;
            JSONArray jsonArray;
            boolean isSuccess = true;
            if(totalOnTimeJSONStr!=null) {
                tmpObj = new JSONObject(totalOnTimeJSONStr);

                if (tmpObj.isNull(dateStr)) jsonArray = new JSONArray();
                else {
                    String dateObj = tmpObj.getString(dateStr);
                    jsonArray = new JSONArray(dateObj);
                }
            }
            else {
                tmpObj = new JSONObject();
                jsonArray = new JSONArray();
            }

            if(!calculateUsageTimeSuccess(usageTime))     isSuccess = false;

            JSONObject statistics = new JSONObject();
            statistics.put("slot", currentTimeSlot);
            statistics.put("time", usageTime);
            statistics.put("success", isSuccess);
            statistics.put("incentive", UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(context, "incentive"));
            jsonArray.put(statistics);
            tmpObj.put(dateStr, jsonArray);

            setStringDataToSharedPref(context,"UsageTimeDaily",tmpObj.toString());
        } catch (JSONException e) {e.printStackTrace();}
    }

    /** 중재기간 동안 사용되는 sharedpref변수 저장 **/
    private static void updateDashboardUsageStatistics(Context context, String dateStr, int usageTime) {
        String totalOnTimeJSONStr = getStringSharedPrefData(context,"DashboardDaily");
        try {
            JSONObject tmpObj;
            JSONArray jsonArray;
            int totalUsgeTime = 0;
            int successNum = 0;
            int failNum = 0;
            if(totalOnTimeJSONStr!=null) {
                tmpObj = new JSONObject(totalOnTimeJSONStr);
                if (tmpObj.isNull(dateStr)) jsonArray = new JSONArray();
                else {
                    String dateObj = tmpObj.getString(dateStr);
                    jsonArray = new JSONArray(dateObj);
                    JSONObject subJsonObject = jsonArray.getJSONObject(0);
                    totalUsgeTime = subJsonObject.getInt("time");
                    successNum = subJsonObject.getInt("success");
                    failNum = subJsonObject.getInt("fail");
                }
            }
            else {
                tmpObj = new JSONObject();
                jsonArray = new JSONArray();
            }

            boolean isSuccess = calculateUsageTimeSuccess(usageTime);
            if (isSuccess) {
                successNum++;
                saveSuccessDataInLocalDB(context, dateStr, usageTime);  // 성공 데이터베이스 저장
            } else {
                failNum++;
                saveFailDataInLocalDB(context, dateStr, usageTime);  // 실패 데이터베이스 저장
            }

            JSONObject statistics = new JSONObject();
            statistics.put("time", totalUsgeTime + usageTime);
            statistics.put("success", successNum);
            statistics.put("fail", failNum);

            jsonArray.put(0, statistics);
            tmpObj.put(dateStr, jsonArray);
            setStringDataToSharedPref(context,"DashboardDaily",tmpObj.toString());
            setIntegerDataToSharedPref(context,"dailySuccess", successNum);

        } catch (JSONException e) {e.printStackTrace();}
    }

    private static void saveSuccessDataInLocalDB(Context context, String dateStr, int usageTime) {
        Log.d("AA", "Room DB debugging, success, targeting incentive: " + getIntegerSharedPrefData(context, "incentive"));

        // AppDatabaseInsertThread를 사용하여 데이터베이스에 데이터 삽입
        AppDatabaseInsertThread thread = new AppDatabaseInsertThread(
                context,
                UtilitiesDateTimeProcess.getCurrentTimeHour(),
                getIntegerSharedPrefData(context, "incentive"),
                true,  // 성공으로 저장
                dateStr
        );

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            // 예외 처리
            e.printStackTrace();
        }

        // 총 인센티브와 오늘 인센티브 값을 SharedPreferences에 저장
        int todayIncentive = UtilitiesLocalDBProcess.getIncentiveSum(context, dateStr);
        setIntegerDataToSharedPref(context, "TotalIncentive", UtilitiesLocalDBProcess.getIncentiveSum(context, ""));
        setIncentiveForDate(context, dateStr, todayIncentive);
    }

    private static void saveFailDataInLocalDB(Context context, String dateStr, int usageTime) {
        Log.d("AA", "Room DB debugging, success, targeting incentive: " + getIntegerSharedPrefData(context, "incentive"));

        // AppDatabaseInsertThread를 사용하여 데이터베이스에 데이터 삽입
        AppDatabaseInsertThread thread = new AppDatabaseInsertThread(
                context,
                UtilitiesDateTimeProcess.getCurrentTimeHour(),
                getIntegerSharedPrefData(context, "incentive"),
                false,  // 성공으로 저장
                dateStr
        );

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            // 예외 처리
            e.printStackTrace();
        }

        // 총 인센티브와 오늘 인센티브 값을 SharedPreferences에 저장
        int todayIncentive = UtilitiesLocalDBProcess.getIncentiveSum(context, dateStr);
        setIntegerDataToSharedPref(context, "TotalIncentive", UtilitiesLocalDBProcess.getIncentiveSum(context, ""));
        setIncentiveForDate(context, dateStr, todayIncentive);
    }


    public static void setIncentiveForDate(Context context, String date, int value) {
        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("Incentive_" + date, value);
        editor.apply();
    }

    public static int getIncentiveForDate(Context context, String date) {
        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        return pref.getInt("Incentive_" + date, 0);
    }


    private static int getRandomNumber(int startNum, int endNum) {
        Random rand = new Random();
        int randNum = rand.nextInt(endNum - startNum)+startNum;
        return randNum;
    }


    /** 대시보드에서 이전 날짜 선택시 해당 날짜 데이터 가져오는 함수 **/
    public static String[] getDataDashboardUI(Context context, String dateStr) {
        String totalOnTimeJSONStr = getStringSharedPrefData(context,"DashboardDaily");
        String[] resultArry = {"0","0","0"};
        try {
            JSONObject tmpObj;
            JSONArray jsonArray;
            if(totalOnTimeJSONStr!=null) {
                tmpObj = new JSONObject(totalOnTimeJSONStr);
                if (tmpObj.isNull(dateStr)) return resultArry;
                else {
                    String dateObj = tmpObj.getString(dateStr);
                    jsonArray = new JSONArray(dateObj);
                    JSONObject subJsonObject = jsonArray.getJSONObject(0);
                    resultArry[0] = String.valueOf(subJsonObject.getInt("time"));
                    resultArry[1] = String.valueOf(subJsonObject.getInt("success"));
                    resultArry[2] = String.valueOf(subJsonObject.getInt("fail"));
                }
            }
            return resultArry;

        } catch (JSONException e) {e.printStackTrace(); return resultArry;}
    }


    public static void printArry(String[] dataArry) {
        if(dataArry == null) {
            Log.w("AA", "No data");
            return;
        }
        Log.w("aa","..................................................");
        Log.w("AA", "usageTime: "+ dataArry[0] + "  success: "+ dataArry[1] + "  fail: "+ dataArry[2]);
        Log.w("aa","..................................................");
    }



    public static void printJSON(Context context, String  dataKey) {
        String eachOnTimeUsageJSONStr = UtilitiesSharedPrefDataProcess.getStringSharedPrefData(context,dataKey);
        if(eachOnTimeUsageJSONStr == null)  eachOnTimeUsageJSONStr = "No data";
        Log.w("aa","..................................................");
        Log.w("aa",eachOnTimeUsageJSONStr);
        Log.w("aa","..................................................");
    }


    /** 사용시간에 대해 성공/실패 여부를 검사하는 함수 **/
    public static boolean calculateUsageTimeSuccess(int time) {
        return time <= 600 ? true : false;
    }

    /** 성공/실패를 boolean값으로 변형 **/
    public static int convertBooleanToInteger(boolean b) {
        return (b == true) ? 1 : 0;
    }

}
