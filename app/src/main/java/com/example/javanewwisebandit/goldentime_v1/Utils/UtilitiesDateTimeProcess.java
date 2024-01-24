package com.example.javanewwisebandit.goldentime_v1.Utils;

import android.content.Context;
import android.util.Log;

import com.example.javanewwisebandit.goldentime_v1.Config.Config;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//import kr.ac.kaist.jypark.goldentime_v1.Config.Config;

public class UtilitiesDateTimeProcess {
    /** 오늘 날짜를 SimpleDateFormat형태로 가져옴 **/
    public static String getTodayDateByDateFormat() {
        DateFormat dateFormat = new SimpleDateFormat("MM-dd");
        Date todayDate = new Date();
        return dateFormat.format(todayDate.getTime());
    }

    /** 오늘 날짜를 Dashboard 포멧으로 가져옴 **/
    public static String getDateForUIComponent(String dateStr) {
        String formattedDateStr = dateStr.split("-")[0]+"월 "+ dateStr.split("-")[1]+"일";
        return formattedDateStr;
    }


    /** 현재 시간(hour)을 가져옴  **/
    public static int getCurrentTimeHour() {
        DateFormat dateFormat = new SimpleDateFormat("HH");
        Date todayDate = new Date();
        return Integer.parseInt(dateFormat.format(todayDate.getTime()));
    }

    /** 현재 분(min)을 가져옴  **/
    public static int getCurrentTimeMin() {
        DateFormat dateFormat = new SimpleDateFormat("mm");
        Date todayDate = new Date();
        return Integer.parseInt(dateFormat.format(todayDate.getTime()));
    }

    /** 현재 초(sec)을 가져옴  **/
    public static int getCurrentTimeSec() {
        DateFormat dateFormat = new SimpleDateFormat("ss");
        Date todayDate = new Date();
        return Integer.parseInt(dateFormat.format(todayDate.getTime()));
    }

    /** 어제날짜를 dateformat(mm-dd)형태로 반환하는 함수 **/
    public static String getPreviousDateByDateFormat(String currentDateStr) {
        return setDate(currentDateStr,-1);
    }

    /** 이전날짜를 Dashoboard UI 날짜포멧 형태로 반환하는 함수 **/
    public static String getPreviousDateByUIComponentDateFormat(String currentDateUIFormattedStr) {
        String currentFormattedDate = convertUIComponentDateToFormattedDate(currentDateUIFormattedStr);
        return setDate(currentFormattedDate,-1);
    }

    /** 이후날짜를 Dashoboard UI 날짜포멧 형태로 반환하는 함수 **/
    public static String getNextDateByDateFormat(String currentDateUIFormattedStr) {
        String currentFormattedDate = convertUIComponentDateToFormattedDate(currentDateUIFormattedStr);
        return setDate(currentFormattedDate,1);
    }


    public static int compareSelectedUIComponentDateWithFirstDate(Context context, String selectedDateStr) {
        String firstDateStr = UtilitiesSharedPrefDataProcess.getStringSharedPrefData(context, "firstDate");

        return compareTwoDate(selectedDateStr,firstDateStr);
    }

    public static int compareSelectedUIComponentDateWithTodayDate(String selectedDateStr) {
        String todayDateStr = getTodayDateByDateFormat();

        return compareTwoDate(selectedDateStr,todayDateStr);
    }

    /** 두 날짜의 양적인 크기를 비교하는 함수 **/
    private static int compareTwoDate(String selectedDateStr, String firstDateStr) {
        if(firstDateStr == null) return -1;
        int monthDiff = Integer.parseInt(selectedDateStr.split("-")[0]) - Integer.parseInt(firstDateStr.split("-")[0]);

        if (monthDiff == 0) {
            int dayDiff = Integer.parseInt(selectedDateStr.split("-")[1]) - Integer.parseInt(firstDateStr.split("-")[1]);

            if (dayDiff > 0) return 1;
            else if (dayDiff == 0) return 0;
            else return -1;
        } else if (monthDiff < 0) return -1;
        else return 1;
    }


    /** 지정한 날짜(dayIdx)만큼 날짜를 옮겨서 dateformat으로 변경해주는 함수 **/
    public static String setDate(String currentDayStr, int dayIdx) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("MM-dd");
            Date selectedDate = dateFormat.parse(currentDayStr);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectedDate);
            calendar.add(Calendar.DATE, dayIdx);

            return dateFormat.format(calendar.getTime());

        } catch (ParseException e) { return null; }
    }

    /** 원격 DB의 datetime 포멧에 맞게 변형 **/
    public static String getDateByDBDateFormat(String dateStr) {
        return Calendar.getInstance().get(Calendar.YEAR) + "-" + dateStr;
    }

    /** Screen Event 발생 시간을 원격 DB에 저장하기 위해 datetime 포멧을 만들어 주는 함수 **/
    public static String getCurrentTimeByFullDateFormat() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date todayDate = new Date();
        return dateFormat.format(todayDate.getTime());
    }

    /** Dashboard UI 포멧 날짜값을 mm-dd 형태 날짜로 변환하는 함수 **/
    public static String convertUIComponentDateToFormattedDate(String dateStr) {
        String changeDateStr = dateStr.replace(" ","-");
        changeDateStr = changeDateStr.replace("월", "");
        changeDateStr = changeDateStr.replace("일","");

        return changeDateStr;
    }

    /**문자열 시간값을 Dashboard UI 포멧 시간으로 변환하는 함수 **/
    public static String convertStringTimeToUIComponetFormat(String timeStr) {
        int tmpTime = Integer.parseInt(timeStr);
        String tmpStr = String.format("%02d:%02d:%02d", (tmpTime / 3600), (tmpTime % 3600) / 60, (tmpTime % 60));
        int tmpHour = Integer.parseInt(tmpStr.split(":")[0]);
        return tmpHour+"시간 "+tmpStr.split(":")[1]+"분 "+tmpStr.split(":")[2]+"초";
    }


    private static String convertFormtedDateToUIComponentDate(String formattedTimeStr) {
        return formattedTimeStr.split("-")[0]+"월 "+formattedTimeStr.split("-")[1]+"일";
    }

    /** SimpleDateFormat 형태의 문자열 시간을 long 타입의 TimeStamp 시간으로 변환하는 함수 **/
    private static long convertFormattedTimeToMillis(String formattedTimeStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long convertedMillsTime = dateFormat.parse(formattedTimeStr).getTime();
            return convertedMillsTime;

        } catch (ParseException e) { return 0L; }
    }

    /** long 타입의 TimeStamp 시간을 SimpleDateFormat 형태의 문자열 시간으로 변환하는 함수 **/
    public static String convertMillisToFormattedTime(long milliSeconds) {
        // Create a calendar object that will convert the date and time value in milliseconds to date.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return dateFormat.format(calendar.getTime());
    }

    /** long 타입의 TimeStamp 시간을 SimpleDateFormat 형태의 문자열 시간으로 변환하는 함수 **/
    public static long convertFormattedDateToMills(String firstDateStr) {
        String tmpDateStr = Calendar.getInstance().get(Calendar.YEAR) + "-" +firstDateStr;
        return getMilliFromDate(tmpDateStr);
    }

    /** long 타입의 TimeStamp 시간을 정수형 시간으로 변환하는 함수 **/
    public static int convertMillisToIntegerTime(long usageTime) {
        float floatTime = usageTime / 1000;
        return Math.round(floatTime);
    }

    /** Milliseconds를 날짜로 변환해주는 함수 **/
    public static long getMilliFromDate(String dateFormat) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            date = formatter.parse(dateFormat);
        } catch (ParseException e) { }

        return date.getTime();
    }


    /** UsageStats 호출 시 사용할 시작/끝시간을 적절하게 변형시켜주는 함수 **/
    public static int getConvertedCurrentHourForUsageStats(int startHour) {
        int currentTimeSlotHour = getCurrentTimeHour();
        if(currentTimeSlotHour < startHour)  currentTimeSlotHour +=24;

        return currentTimeSlotHour;
    }

    /** UsageStats 파라미터에 넣을 쿼리 시간(Long타입)을 구하는 함수 **/
    public static long getQueryTime(String queryStr) {
        String dateStr, timeStr;
        dateStr = queryStr.split(" ")[0];
        int hour = Integer.parseInt(queryStr.split(" ")[1]);
        timeStr = String.format("%02d:%02d:%02d", hour, 0, 0);

        return convertFormattedTimeToMillis(dateStr+" "+timeStr);
    }
    /** 매개변수 시간의 이전 시간을 구해주는 함수 **/
    public static int getConvertedPreviousTimeSlot(int timeSlot) {
        if(timeSlot <= Config.GOLDENTIME_SERVICE_STOP_TIME) {
            timeSlot +=23;
            timeSlot = timeSlot % 24;
        }
        else timeSlot--;

        return timeSlot;
    }

    /** 캘린더 뷰를 위한 날짜 함수 모음 **/
    public static String createSelectedDateForCalendar(int month, int day) {
        return String.format("%02d-%02d", month, day);
    }

    /** 대시보드 일일통계에 표기할 문구 선택하는 함수 **/
    public static String getDailyStatisticsWording(String dateStr) {
        String resultStr="";
        String todayDateStr = getTodayDateByDateFormat();
        String yesterdayDateStr = setDate(todayDateStr,-1);

        /* 선택한 날짜 = 오늘 */
        if(dateStr.equals(todayDateStr)) {
            if(checkGoldenTime())   resultStr = convertFormtedDateToUIComponentDate(dateStr)+" "+getCurrentTimeHour()+"시 기준";
            else                    resultStr = convertFormtedDateToUIComponentDate(dateStr)+"(골든타임 시간이 아닙니다.)";
        }
        /* 선택한 날짜 != 오늘 */
        else {
            /* 선택한 날짜 = 어제 */
            if(dateStr.equals(yesterdayDateStr) && checkGoldenTime())
                resultStr = convertFormtedDateToUIComponentDate(dateStr)+" 사용통계(현재 골든타임 진행중)";
            else    resultStr = convertFormtedDateToUIComponentDate(dateStr)+" 사용통계";
        }

        return resultStr;
    }

    /** 대시보드 전체통계에 표기할 문구 세팅 함수 **/
    public static String getTotalStatisticsWording() {
        return getDateForUIComponent(getTodayDateByDateFormat())+" "+getCurrentTimeHour()+"시 기준";
    }

    public static boolean checkGoldenTime() {
        int currentHour = getCurrentTimeHour();
        return currentHour < Config.GOLDENTIME_SERVICE_STOP_TIME || currentHour >= Config.GOLDENTIME_SERVICE_START_TIME;
    }

    public static boolean checkGoldenTimeMidNight() {
        int currentHour = getCurrentTimeHour();
        return currentHour >= 0 && (currentHour > 24 || currentHour <= Config.GOLDENTIME_SERVICE_STOP_TIME);
    }

    public static String convertedDateStr(String dateStr) {
        if(dateStr.equals(getTodayDateByDateFormat()) && checkGoldenTimeMidNight())
            dateStr = UtilitiesDateTimeProcess.setDate(dateStr, -1);

        return dateStr;
    }

    public static boolean checkDashboardTime() {
        int currentHour = getCurrentTimeHour();
        return currentHour <= Config.GOLDENTIME_SERVICE_STOP_TIME || currentHour > Config.GOLDENTIME_SERVICE_START_TIME;
    }

    /*public static String getContextByTimeSlot(int slot) {
        //TODO: 시간대(timeslot)에 따라 context 분류 -> 슬라이드에 있음
        if(slot > 8 && slot < 12) return "morning";
        else if(slot > 11 && slot < 16) return "afternoon";
        else if(slot > 15 && slot < 21) return "evening";
        else return "night";
    }

    public static String [] getAllContext(){
        return new String [] {"morning", "afternoon", "evening", "night"};
    }

    public static int [] getTimeSlotByContext (String context){
        if (context.equals("morning")) return new int [] {9, 12};
        else if (context.equals("afternoon")) return new int [] {12, 16};
        else if (context.equals("evening")) return new int [] {16, 21};
        else if (context.equals("night")) return new int [] {21, 2};
        else return new int [] {0, 0};
    }*/

    public static String getContextByTimeSlot(int slot) {
        Calendar today = Calendar.getInstance();
        if (slot <= 2 || slot > 23) today.add(Calendar.DAY_OF_WEEK, -1);
        if (today.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            return "weekend";
        }
        if(slot > 8 && slot < 18) return "work";
        else return "non-work";
    }

    public static String getContextByTimeSlotAndSuccess(int slot, boolean success) {
        Calendar today = Calendar.getInstance();
        if (success) {
            if (slot <= 2 || slot >= 23) today.add(Calendar.DAY_OF_WEEK, -1);
        }
        else {
            if (slot <= 2 || slot > 23) today.add(Calendar.DAY_OF_WEEK, -1);
        }
        if (today.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                today.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            return "weekend";
        }
        if(slot > 8 && slot < 18) return "work";
        else return "non-work";
    }

    public static String [] getAllContext() {
        return new String [] {"work", "non-work", "weekend"};
    }

    public static int [] getTimeSlotByContext (String context) {
        if (context.equals("work")) return new int []{9, 18};
        else if (context.equals("non-work")) return new int []{18, 2};
        else return new int []{9, 2};
    }

}