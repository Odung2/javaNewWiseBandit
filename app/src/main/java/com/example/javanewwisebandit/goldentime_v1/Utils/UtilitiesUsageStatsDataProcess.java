package com.example.javanewwisebandit.goldentime_v1.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.javanewwisebandit.goldentime_v1.Config.Config;
import com.example.javanewwisebandit.goldentime_v1.Model.TimeSlotEachAppDataObject;
import com.example.javanewwisebandit.goldentime_v1.Model.TimeSlotTotalDataObject;
import com.example.javanewwisebandit.goldentime_v1.Model.UsageStatsIndexObject;
import com.example.javanewwisebandit.goldentime_v1.Model.UsageStatsObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

//import kr.ac.kaist.jypark.goldentime_v1.Config.Config;
//import kr.ac.kaist.jypark.goldentime_v1.Model.TimeSlotEachAppDataObject;
//import kr.ac.kaist.jypark.goldentime_v1.Model.TimeSlotTotalDataObject;
//import kr.ac.kaist.jypark.goldentime_v1.Model.UsageStatsIndexObject;
//import kr.ac.kaist.jypark.goldentime_v1.Model.UsageStatsObject;

public class UtilitiesUsageStatsDataProcess {
    public static boolean getUsageStatsData(Context context, int startHour) {
        int convertedEndTime = UtilitiesDateTimeProcess.getConvertedCurrentHourForUsageStats(startHour);
        String eachAppTimeSlotJSONStr = null;
        String totalJSONStr = null;

        if(startHour == convertedEndTime)   return false;

        /** 루프 돌면서 타임슬롯 별(마지막 업데이트했던 시간 ~ 현재시간) UsageStats 데이터 저장 **/
        for (int i = startHour; i < convertedEndTime; i++) {
            String[] timeResourceArry;  // 1: QueryStartTime(UsageStats 파라미터), 2: QueryEndTime(UsageStats 파라미터)
            if(convertedEndTime < 24)  timeResourceArry = getConvertedTimeResourcesNormalCase(i);
            else                       timeResourceArry = getConvertedTimeResourcesNotNormalCase(i);

            /* currentTimeSlotEachAppUsageStats: 각 타임슬롯 별 UsageStats 데이터 저장 객체(앱 패키지명, 이벤트 타입(String), 이벤트 발생시간(Long) ) */
            Map<Integer, UsageStatsObject> currentTimeSlotEachAppUsageStats = addEachTimeSlotUsageStats(context, timeResourceArry[0],  timeResourceArry[1]);

            /* 시간대 별 각 앱 사용 통계(패키지명, 시작시간, 끝시간, 사용시간)를 JSON에 넣는 함수 */
            if(!currentTimeSlotEachAppUsageStats.isEmpty()) {
                eachAppTimeSlotJSONStr = insertEachAppUsageStatsToJSONByDateFormat(eachAppTimeSlotJSONStr, currentTimeSlotEachAppUsageStats);
                Map<Integer, TimeSlotEachAppDataObject> timeSlotEachAppDataObjectMap = getAppUsageStatistics(timeResourceArry[0], currentTimeSlotEachAppUsageStats);
                if (timeSlotEachAppDataObjectMap != null) {
                    Map<Integer, TimeSlotTotalDataObject> timeSlotTotalDataObjectMap = getTimeSlotTotalUsageStatsObject(getAppUsageStatistics(timeResourceArry[0], currentTimeSlotEachAppUsageStats));
                    totalJSONStr = insertTotalUsageStatsToJSONByDateFormat(totalJSONStr, timeSlotTotalDataObjectMap, timeResourceArry[2], Integer.toString(i > 24 ? i - 24 : i));
                }
            }

            UtilitiesSharedPrefDataProcess.setStringDataToSharedPref(context, "UsageStatsRawData", totalJSONStr);
            //UtilitiesSharedPrefDataProcess.setStringDataToSharedPref(context,"UsageStatsRawData",eachAppTimeSlotJSONStr);
        }

        return true;
    }

    private static Map<Integer, UsageStatsObject> addEachTimeSlotUsageStats(Context context, String queryStartStr, String queryEndStr) {
        Map<Integer, UsageStatsObject> currentTimeSlotEachAppUsageStats;
        long quertStartTime = UtilitiesDateTimeProcess.getQueryTime(queryStartStr);
        long queryEndTime = UtilitiesDateTimeProcess.getQueryTime(queryEndStr);

        currentTimeSlotEachAppUsageStats = AppUsageStats.getTimeSlotEachAppUsageStatsObject(context, quertStartTime, queryEndTime);

        return currentTimeSlotEachAppUsageStats;
    }

    /** 시간상으로만 봤을 때 현재시간보다 마지막 업데이트 시간이 더 큰 경우(예. 23시~10시) **/
    private static String[] getConvertedTimeResourcesNotNormalCase(int hour) {
        String queryDateStr;
        String DBDateStr;
        String queryStart, queryEnd;
        String todayDateStr = UtilitiesDateTimeProcess.getTodayDateByDateFormat();
        String[] resultArry = new String[3];

        if(hour >= 24)          queryDateStr = UtilitiesDateTimeProcess.getTodayDateByDateFormat();
        else                    queryDateStr = UtilitiesDateTimeProcess.getPreviousDateByDateFormat(todayDateStr);

        queryDateStr = UtilitiesDateTimeProcess.getDateByDBDateFormat(queryDateStr);

        if(hour>=24) {
            queryStart = queryDateStr+" "+ (hour-24);
            queryEnd = queryDateStr+" "+(hour-23);
        }
        else {
            queryStart = queryDateStr+" "+ hour;
            if(hour==23)   queryEnd = todayDateStr+" "+ 0;
            else        queryEnd = queryDateStr+" "+(hour+1);
        }
        resultArry[0] = queryStart;
        resultArry[1] = queryEnd;
        resultArry[2] = queryDateStr;

        return resultArry;
    }

    /** 시간상으로만 봤을 때 현재시간이 마지막 업데이트 시간보다 더 큰 경우(예. 0시~10시) **/
    private static String[] getConvertedTimeResourcesNormalCase(int hour) {
        String queryDateStr;
        String queryStart, queryEnd;
        String[] resultArry = new String[3];
        String todayDateStr = UtilitiesDateTimeProcess.getTodayDateByDateFormat();

        queryDateStr = todayDateStr;
        queryDateStr = UtilitiesDateTimeProcess.getDateByDBDateFormat(queryDateStr);
        queryStart = queryDateStr+" "+hour;
        queryEnd = queryDateStr+" "+ (hour+1);

        resultArry[0] = queryStart;
        resultArry[1] = queryEnd;
        resultArry[2] = queryDateStr;

        return resultArry;
    }


    /** 각 타임슬롯 별 앱 사용기록 통계를 TimeSlotTotalDataObject(패키지명, 사용시간, 사용횟수)에 저장하는 함수 **/
    private static Map<Integer, TimeSlotTotalDataObject> getTimeSlotTotalUsageStatsObject(Map<Integer, TimeSlotEachAppDataObject> map) {
        ArrayList<String> uniquePackageList = getUniquePackageList(map);
        Map<Integer, TimeSlotTotalDataObject> caculatedTimeSlotTotalUsageStatsObject = new LinkedHashMap<>();
        int loopIdx = 0;
        for(String packageName : uniquePackageList) {
            long usageTime = 0L;
            int usageCount = 0;
            for (Map.Entry<Integer, TimeSlotEachAppDataObject> entry : map.entrySet()) {
                int key = entry.getKey();
                TimeSlotEachAppDataObject um = entry.getValue();
                if(packageName.equals(um.getPackageName())) {
                    long tmpUsageTime =map.get(key).getDuration();
                    if(tmpUsageTime > Config.GOLDENTIME_USAGESTATS_MIN_USAGETIME) {
                        usageTime += tmpUsageTime;
                        usageCount++;
                    }
                }
            }
            if(usageCount > 0 && usageTime > Config.GOLDENTIME_USAGESTATS_MIN_USAGETIME)  caculatedTimeSlotTotalUsageStatsObject.put(loopIdx++, new TimeSlotTotalDataObject(packageName,usageTime,usageCount));
        }
        return caculatedTimeSlotTotalUsageStatsObject;
    }

    /** UsageStats으로 받아온 사용 앱(패키지명) 리스트 내에 중복되는 값들을 제거하는 함수 (각 타임슬롯에서 사용한 앱 별 사용시간,사용횟수를 구하기 위한 전처리 작업 중의 하나) **/
    public static ArrayList<String> getUniquePackageList(Map<Integer, TimeSlotEachAppDataObject> map) {
        ArrayList<String> packageList = new ArrayList<>();
        for (Map.Entry<Integer, TimeSlotEachAppDataObject> entry : map.entrySet()) {
            TimeSlotEachAppDataObject um = entry.getValue();
            String currentPackageName = um.getPackageName();
            packageList.add(currentPackageName);
        }

        ArrayList<String> uniquePackageList = removeDuplicates(packageList);

        return uniquePackageList;
    }

    /** 앱 리스트의 중복 제거하는 함수 **/
    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {
        Set<T> set = new LinkedHashSet<>();
        set.addAll(list);
        list.clear();
        list.addAll(set);

        return list;
    }

    /** UsageStats으로부터 받은 RawData를 가공해서 각 앱 별 사용 통계를 구하는 함수 **/
    public static  Map<Integer, TimeSlotEachAppDataObject> getAppUsageStatistics(String timeStr, Map<Integer, UsageStatsObject> map) {
        if(map.isEmpty())   return null;
        else {
            Map<Integer, UsageStatsObject> updatedMap = removeFirstLauncherPackage(map);
            updatedMap = checkAndUpdateUsageStatsObj(updatedMap);
            //printUsageStatsObject(timeStr,updatedMap);

            Map<Integer, UsageStatsIndexObject> indexdUsageStatsObj = splitTimeSlotByEvent(updatedMap);
            indexdUsageStatsObj = setPackageNameToIndexObject(updatedMap,indexdUsageStatsObj);
            Map<Integer, TimeSlotEachAppDataObject> resultEachAppUsageStatsObj = getEachAppUsageStats(updatedMap,indexdUsageStatsObj);
            //printResultUsageStatsObject(timeStr,resultEachAppUsageStatsObj);

            return resultEachAppUsageStatsObj;
        }
    }

    /** 앱 사용통게 관련 IndexObject(ArrayList내에서 각 앱 사용 시작/종료 시점을 가르키는 인덱스)에 앱 패키지 명을 붙여주는 함수 (시작/종료되는 앱이 어떤 앱이지 표시하기 위함) **/
    public static Map<Integer, UsageStatsIndexObject> setPackageNameToIndexObject(Map<Integer, UsageStatsObject> map, Map<Integer, UsageStatsIndexObject> indexMap) {
        for (Map.Entry<Integer, UsageStatsIndexObject> entry : indexMap.entrySet()) {
            int key = entry.getKey();
            UsageStatsIndexObject um = entry.getValue();
            int startIdx = um.getStartTimeIdx();
            int endIdx = um.getEndTimeIdx();
            String tmpPackageName = setAppPackageName(startIdx,endIdx,map);
            indexMap.get(key).setPackageName(tmpPackageName);
        }
        return indexMap;
    }

    /** 앱 패키지명을 매칭해서 표시하는 함수 **/
    public static String setAppPackageName(int startIdx, int endIdx, Map<Integer, UsageStatsObject> map) {
        for(int i=startIdx; i<=endIdx; i++) {
            String packageName = map.get(i).getPackageName();
            if(checkAppPackageName(packageName)) return   packageName;
        }
        return "System App";
    }


    /** UsageStats RawData에 대한 전처리 작업 후, 각 앱 별 사용통계(사용시간, 횟수)를 구하는 함수 **/
    public static Map<Integer, TimeSlotEachAppDataObject> getEachAppUsageStats(Map<Integer, UsageStatsObject> originalMap, Map<Integer, UsageStatsIndexObject> indexMap) {
        Map<Integer, TimeSlotEachAppDataObject> caculatedEachAppUsageStatsObject = new LinkedHashMap<>();

        int loopIdx = 0;
        for (Map.Entry<Integer, UsageStatsIndexObject> entry : indexMap.entrySet()) {
            UsageStatsIndexObject um = entry.getValue();
            long tmpStartTime = originalMap.get(um.getStartTimeIdx()).getEventTime();
            long tmpEndTime = originalMap.get(um.getEndTimeIdx()).getEventTime();
            long tmpUsageTime = tmpEndTime - tmpStartTime;
            if(tmpUsageTime >= Config.GOLDENTIME_USAGESTATS_MIN_USAGETIME)
                caculatedEachAppUsageStatsObject.put(loopIdx++, new TimeSlotEachAppDataObject(um.getPackageName(),tmpStartTime,tmpEndTime,tmpUsageTime));
        }

        return caculatedEachAppUsageStatsObject;
    }


    /** 각 앱 별 사용시간,빈도값을 구하기 위해 앱 사용시작(resume)/사용종료(pause) 이벤트 로그시간을 기준으로 분리하는 함수 **/
    public static Map<Integer, UsageStatsIndexObject> splitTimeSlotByEvent(Map<Integer, UsageStatsObject> map) {
        ArrayList<String> eventPackageNameList = new ArrayList<>();
        ArrayList<Long> eventTimeList = new ArrayList<>();
        Map<Integer, UsageStatsIndexObject> eachPackageAppIndexInfo;

        /*인덱싱(앱 사용 시작/종료 위치)을 계산하기 위해 eventPackage와 eventTime 값만 따로 모음*/
        int loopIdx=0;
        for (Map.Entry<Integer, UsageStatsObject> entry : map.entrySet()) {
            UsageStatsObject um = entry.getValue();
            eventPackageNameList.add(loopIdx,um.getPackageName());
            eventTimeList.add(loopIdx++,um.getEventTime());
        }

        eachPackageAppIndexInfo = calculateTimeIndex(eventPackageNameList, eventTimeList);

        return eachPackageAppIndexInfo;
    }


    /** UsageStats 사용 통계를 앱 사용 단위로 나눠주는 알고리즘 함수  **/
    private static Map<Integer, UsageStatsIndexObject> calculateTimeIndex(ArrayList<String> packageNameList, ArrayList<Long> eventTimeList) {

        Map<Integer, UsageStatsIndexObject> eachPackageAppIndexInfo = new LinkedHashMap<>();
        int loopIdx=0;
        int startIdx = 0;
        int size = eventTimeList.size();
        if (eventTimeList.size() % 2 != 0)
            size = eventTimeList.size() - 1;
        for(int i=0;i<size;i+=2) {
            if(!packageNameList.get(i).contains("launcher")) {
                int compareIdx1 = i+1;
                int compareIdx2 = i+2;

                if(compareIdx2 != eventTimeList.size()) {
                    if((eventTimeList.get(compareIdx2) - eventTimeList.get(compareIdx1)) > Config.GOLDENTIME_USAGESTATS_MIN_INTERVAL) {
                        eachPackageAppIndexInfo.put(loopIdx++, new UsageStatsIndexObject("A", startIdx, compareIdx1));
                        startIdx = compareIdx2;
                    }
                    else {
                        if(packageNameList.get(compareIdx2).contains("launcher")) {
                            eachPackageAppIndexInfo.put(loopIdx++, new UsageStatsIndexObject("A", startIdx, compareIdx1));
                            startIdx = compareIdx2;
                        }
                    }
                }
                else {
                    eachPackageAppIndexInfo.put(loopIdx++, new UsageStatsIndexObject("A", startIdx, compareIdx1));
                    break;
                }
            }
            else    startIdx = i+2;
        }
        return eachPackageAppIndexInfo;
    }


    /** UsageStats 예외처리: 각 타임슬롯 끝에 Resume 등장 or 타임슬롯 시작에 Pause 등장 시 보정해주는 함수  **/
    private static Map<Integer, UsageStatsObject> checkAndUpdateUsageStatsObj(Map<Integer, UsageStatsObject> map) {
        int lastIdx = map.size()-1;
        Map<Integer, UsageStatsObject> updatedMap = new LinkedHashMap<>();
        if((!map.get(0).getEventType().equals("ACTIVITIY_PAUSED")) && (!map.get(lastIdx).getEventType().equals("ACTIVITIY_RESUMED"))) {
            for (Map.Entry<Integer, UsageStatsObject> entry : map.entrySet())
                updatedMap.put(entry.getKey(), entry.getValue());
        }
        else {
            if(map.get(0).getEventType().equals("ACTIVITIY_PAUSED")) {
                updatedMap.put(0, new UsageStatsObject(map.get(0).getPackageName(), "ACTIVITIY_RESUMED" ,map.get(0).getEventTime()));
                for (Map.Entry<Integer, UsageStatsObject> entry : map.entrySet())
                    updatedMap.put( entry.getKey()+1, entry.getValue());
            }

            if(map.get(lastIdx).getEventType().equals("ACTIVITIY_RESUMED")) {
                map.put(map.size(), new UsageStatsObject(map.get(lastIdx).getPackageName(), "ACTIVITIY_PAUSED" ,map.get(lastIdx).getEventTime()));
                for (Map.Entry<Integer, UsageStatsObject> entry : map.entrySet())
                    updatedMap.put(entry.getKey(), entry.getValue());
            }
        }

        return updatedMap;
    }

    /** 처음에 등장하는 launcher 기록들 삭제하는 함수 **/
    private static Map<Integer, UsageStatsObject> removeFirstLauncherPackage(Map<Integer, UsageStatsObject> map) {
        int firstUserAppIndex=0;
        Map<Integer, UsageStatsObject> removeFirstLauncherPkgObj = new LinkedHashMap<>();
        for (Map.Entry<Integer, UsageStatsObject> entry : map.entrySet()) {
            UsageStatsObject um = entry.getValue();
            if(checkAppPackageName(um.getPackageName())) {
                firstUserAppIndex = entry.getKey();
                break;
            }
        }

        int loopIdx=0;
        for (Map.Entry<Integer, UsageStatsObject> entry : map.entrySet()) {
            if(entry.getKey() >= firstUserAppIndex)
                removeFirstLauncherPkgObj.put(loopIdx++, entry.getValue());
        }

        return removeFirstLauncherPkgObj;
    }

    /** 앱 사용시간 계산할 때 배제할 앱 리스트 **/
    public static boolean checkAppPackageName(String packageName) {
        if(packageName.contains("systemui") || packageName.contains("settings") || packageName.contains("launcher") || packageName.contains("searchbox") || packageName.contains("packageinstaller"))    return false;
        else return true;
    }


    /** UsageStats_Stats 데이터(앱패키지명, 사용시간, 사용횟수)를 JSON 포멧으로 만드는 함수 **/
    private static String insertTotalUsageStatsToJSONByDateFormat(String jsonObjStr, Map<Integer, TimeSlotTotalDataObject> map, String dateStr, String timeSlot) {
        try {
            String resultStr ="";
            JSONArray totalUsageStatisticsArry;
            int arrayLoopIdx;
            if(jsonObjStr!= null) {
                totalUsageStatisticsArry = new JSONArray(jsonObjStr);
                arrayLoopIdx = totalUsageStatisticsArry.length();
            }
            else {
                totalUsageStatisticsArry = new JSONArray();
                arrayLoopIdx = 0;
            }

            for (Map.Entry<Integer, TimeSlotTotalDataObject> entry : map.entrySet()) {
                TimeSlotTotalDataObject um = entry.getValue();
                JSONObject statistics = new JSONObject();
                statistics.put("date", dateStr);
                statistics.put("slot", Integer.parseInt(timeSlot));
                statistics.put("name", um.getPackageName());
                statistics.put("time", UtilitiesDateTimeProcess.convertMillisToIntegerTime(um.getUsageTime()));
                statistics.put("count", um.getUsageCount());

                totalUsageStatisticsArry.put(arrayLoopIdx++, statistics);
            }
            resultStr = totalUsageStatisticsArry.toString();

            return resultStr;

        } catch (JSONException e) {return null;}
    }

//    /** UsageStats_RawData 데이터(앱패키지명, 시작시간, 끝시간, 사용시간)를 JSON 포멧으로 만드는 함수 **/
//    private static JSONObject insertEachAppUsageStatsToJSONByDateFormat(JSONObject jsonObj, Map<Integer, TimeSlotEachAppDataObject> map, String dateStr, String timeSlot) {
//        try {
//            JSONArray eachAppUsageStatisticsArry = new JSONArray();
//            int arrayLoopIdx =0;
//            for (Map.Entry<Integer, TimeSlotEachAppDataObject> entry : map.entrySet()) {
//                TimeSlotEachAppDataObject um = entry.getValue();
//                JSONObject statistics = new JSONObject();
//                statistics.put("date", dateStr);
//                statistics.put("slot", Integer.parseInt(timeSlot));
//                statistics.put("name", um.getPackageName());
//                statistics.put("start", UtilitiesDateTimeProcess.convertMillisToFormattedTime(um.getStartTime()));
//                statistics.put("end", UtilitiesDateTimeProcess.convertMillisToFormattedTime(um.getEndTime()));
//                statistics.put("duration", UtilitiesDateTimeProcess.convertDurationMillisToFormattedTime(um.getDuration()));
//
//                eachAppUsageStatisticsArry.put(arrayLoopIdx++, statistics);
//            }
//            jsonObj.put("Raw",eachAppUsageStatisticsArry);
//
//            return jsonObj;
//        } catch (JSONException e) {return null;}
//    }

    /** UsageStats_RawData 데이터(앱패키지명, 시작시간, 끝시간, 사용시간)를 JSON 포멧으로 만드는 함수 **/
    private static String insertEachAppUsageStatsToJSONByDateFormat(String jsonObjStr, Map<Integer, UsageStatsObject> map) {
        try {
            String resultStr ="";
            JSONArray eachAppUsageStatisticsArry;
            int arrayLoopIdx;
            if(jsonObjStr!= null) {
                eachAppUsageStatisticsArry = new JSONArray(jsonObjStr);
                arrayLoopIdx = eachAppUsageStatisticsArry.length();
            }
            else {
                eachAppUsageStatisticsArry = new JSONArray();
                arrayLoopIdx = 0;
            }

            for (Map.Entry<Integer, UsageStatsObject> entry : map.entrySet()) {
                UsageStatsObject um = entry.getValue();
                JSONObject statistics = new JSONObject();
                statistics.put("name", um.getPackageName());
                statistics.put("event", um.getEventType());
                statistics.put("time", UtilitiesDateTimeProcess.convertMillisToFormattedTime(um.getEventTime()));

                eachAppUsageStatisticsArry.put(arrayLoopIdx++, statistics);
            }
            resultStr = eachAppUsageStatisticsArry.toString();

            return resultStr;
        } catch (JSONException e) {return null;}
    }

    /** 데이터 사이즈를 줄이기 위해 앱 이름으로 저장.  앱 이름 없는 것은 패키지명을 저장 **/
    public static String convertAppPackageNameToAppName(Context context, String packageName) {
        String appName = getAppNameByPackageName(context,packageName);
        return (appName != null) ? appName : packageName;
    }

    /** 앱이름 --> 앱 피키지명 추출 함수 **/
    public static String getAppNameByPackageName(Context context, String packageName) {
        final PackageManager pm = context.getPackageManager();
        try {
            String tmpName = String.valueOf(pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)));
            return tmpName;
        } catch (PackageManager.NameNotFoundException e) {return null;}
    }










    /*******************************************************/
    private static String[] printNotNormalCase(int startHour, int endHour) {
        int timeSlot;
        String timeSlotDateStr, queryDateStr;
        String queryStart, queryEnd;
        String todayDateStr = UtilitiesDateTimeProcess.getTodayDateByDateFormat();
        String[] resultArry = new String[4];


        for (int i = startHour; i < endHour; i++) {
            if(i >= 24)         queryDateStr = UtilitiesDateTimeProcess.getTodayDateByDateFormat();
            else                queryDateStr = UtilitiesDateTimeProcess.getPreviousDateByDateFormat(todayDateStr);

            if(i <=25) {
                timeSlotDateStr = UtilitiesDateTimeProcess.getPreviousDateByDateFormat(todayDateStr);
                timeSlot = i;
            }
            else {
                timeSlotDateStr = queryDateStr;
                timeSlot = i-24;
            }

            if(i>=24) {
                queryStart = "2022-"+ queryDateStr+" "+ (i-24);
                queryEnd = "2022-" + queryDateStr+" "+(i-23);
            }
            else {
                queryStart = queryDateStr+" "+ i;
                if(i==23)   queryEnd = "2022-" + todayDateStr+" "+ 0;
                else        queryEnd = "2022-" + queryDateStr+" "+(i+1);
            }
            resultArry[0] = timeSlotDateStr;
            resultArry[1] = String.valueOf(timeSlot);
            resultArry[2] = queryStart;
            resultArry[3] = queryEnd;

            Log.w("AA","TimeSlotDate: "+timeSlotDateStr+",  Slot: "+timeSlot+"..................QueryTime: "+queryStart+" ~ "+queryEnd);
        }
        return resultArry;
    }


    private static String[] printNormalCase(int startHour, int endHour) {
        int timeSlot;
        String timeSlotDateStr, queryDateStr;
        String queryStart, queryEnd;
        String todayDateStr = UtilitiesDateTimeProcess.getTodayDateByDateFormat();
        String[] resultArry = new String[4];

        for (int i = startHour; i < endHour; i++) {
            queryDateStr = todayDateStr;
            queryStart = queryDateStr+" "+i;
            queryEnd = queryDateStr+" "+ (i+1);

            if(i < 2) {
                timeSlotDateStr = UtilitiesDateTimeProcess.getPreviousDateByDateFormat(queryDateStr);
                timeSlot = i+24;
            }
            else {
                timeSlotDateStr = queryDateStr;
                timeSlot = i;
            }

            resultArry[0] = timeSlotDateStr;
            resultArry[1] = String.valueOf(timeSlot);
            resultArry[2] = queryStart;
            resultArry[3] = queryEnd;

            Log.w("AA","TimeSlotDate: "+timeSlotDateStr+",  Slot: "+timeSlot+"..................QueryTime: "+queryStart+" ~ "+queryEnd);
        }
        return resultArry;
    }

    private static void printUsageStatsObject(String timeStr, Map<Integer, UsageStatsObject> map) {
        Set<Integer> set = map.keySet();
        Iterator<Integer> iter = set.iterator();
        Log.w("Utils", timeStr+"Updated ========================================================");
        while (iter.hasNext()) {
            int key = iter.next();
            UsageStatsObject um = map.get(key);
            Log.w("Activity","key : " + key + ", packageName : " + um.getPackageName() + ", eventType : "+ um.getEventType() + ", eventTime : " + UtilitiesDateTimeProcess.convertMillisToFormattedTime(um.getEventTime()));
        }
        Log.w("Utils", "========================================================================================================================");
    }

    private static void printResultUsageStatsObject(String timeStr, Map<Integer, TimeSlotEachAppDataObject> map) {
        Set<Integer> set = map.keySet();
        Iterator<Integer> iter = set.iterator();
        Log.w("Utils", timeStr+"Updated ========================================================");
        while (iter.hasNext()) {
            int key = iter.next();
            TimeSlotEachAppDataObject um = map.get(key);
            Log.w("Activity","packageName : " + um.getPackageName() +
                    ", startTime : "+ UtilitiesDateTimeProcess.convertMillisToFormattedTime(um.getStartTime()) +
                    ", endTime : "+ UtilitiesDateTimeProcess.convertMillisToFormattedTime(um.getEndTime()));
        }
        Log.w("Utils", "========================================================================================================================");
    }

    private static void printTimeSlotEachAppDataObject(Map<Integer, UsageStatsObject> map) {
        Set<Integer> set = map.keySet();
        Iterator<Integer> iter = set.iterator();
        Log.w("Utils", "Updated ========================================================");
        while (iter.hasNext()) {
            int key = iter.next();
            UsageStatsObject um = map.get(key);
            Log.w("AA","packageName : " + um.getPackageName() + ", eventType : "+ um.getEventType() +
                    ", eventTime : "+ UtilitiesDateTimeProcess.convertMillisToFormattedTime(um.getEventTime()));
        }
        Log.w("Utils", "========================================================================================================================");
    }

    private static void printTimeSlotTotalDataObject(Map<Integer, TimeSlotTotalDataObject> map) {
        Set<Integer> set = map.keySet();
        Iterator<Integer> iter = set.iterator();
        Log.w("Utils", "Updated ========================================================");
        while (iter.hasNext()) {
            int key = iter.next();
            TimeSlotTotalDataObject um = map.get(key);
            Log.w("Activity","packageName : " + um.getPackageName() +
                    ", UsageTime : "+ UtilitiesDateTimeProcess.convertMillisToFormattedTime(um.getUsageTime()) +
                    ", UsageCount : "+ um.getUsageCount());
        }
        Log.w("Utils", "========================================================================================================================");
    }

}
