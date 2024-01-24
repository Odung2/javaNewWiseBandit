package com.example.javanewwisebandit.goldentime_v1.Activity;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ImageViewCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.javanewwisebandit.R;
import com.example.javanewwisebandit.goldentime_v1.Adapter.ViewPagerAdapter;
import com.example.javanewwisebandit.goldentime_v1.Service.RegisterDailyJobScheduleService;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesDateTimeProcess;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesLocalDBProcess;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

//import kr.ac.kaist.jypark.goldentime_v1.Adapter.ViewPagerAdapter;
//import kr.ac.kaist.jypark.goldentime_v1.R;
//import kr.ac.kaist.jypark.goldentime_v1.Service.RegisterDailyJobScheduleService;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesDateTimeProcess;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesLocalDBProcess;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;

public class MainActivity extends AppCompatActivity {
    boolean isAppChanged;
    boolean isUpdateAfterAppChange;
    ImageView rightArrow;
    ImageView leftArrow;

    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isUpdateAfterAppChange = UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getApplicationContext(),"isUpdateAfterAppChange");
        isAppChanged = UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getApplicationContext(),"isAppChange");

        if(isAppChanged && isUpdateAfterAppChange) {
            setContentView(R.layout.activity_intervention_main);
        }
        else    setContentView(R.layout.activity_baseline_main);
    }


    @Override
    protected void onResume() {
        super.onResume();

        isAppChanged = UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getApplicationContext(),"isAppChange");
        isUpdateAfterAppChange = UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getApplicationContext(), "isUpdateAfterAppChange");

        if(isAppChanged && isUpdateAfterAppChange) {
            /* 중재기간에 보여줄 중재모드 화면 구성해주는 함수 호출 */
            Log.d("AA", "Setup Intervention UI in Main Activity");
            ViewPager2 viewPager2 = findViewById(R.id.viewPager2);
            TabLayout tabLayout = findViewById(R.id.tabLayout);

            ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);

            viewPager2.setAdapter(viewPagerAdapter);

            final List<String> tabNames = Arrays.asList("Dashboard", "Contexts");
            new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
                @Override
                public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                    TextView textView = new TextView(MainActivity.this);
                    textView.setText(tabNames.get(position));
                    tab.setCustomView(textView);
                }
            }).attach();

            Toast.makeText(getApplicationContext(), "GoldenTime 메인화면입니다.", Toast.LENGTH_SHORT).show();
        }
        else {
            /* 체크할 flag들 전부 가져옴 */
            boolean isFirstMainPage = UtilitiesSharedPrefDataProcess.checkFirstMainPageExecution(getApplicationContext());
            boolean isEmailRegistered = UtilitiesSharedPrefDataProcess.checkRegisterEmailStatus(getApplicationContext());
            boolean isBatteryOptimizationPermission = checkBatteryOptimizationPermission();
            boolean isGetUsageStatsPermission = checkUsageStatsPermission();

            /* 이메일 등록했는 지, 설정해줘야할 기본 퍼미션들 잘 설정했는 지 확인하는 로직*/
            if(isEmailRegistered && isGetUsageStatsPermission && isBatteryOptimizationPermission) {
                /* 모든 퍼미션 설정 및 이메일 등록을 정상적으로 완료한 경우 */

                UtilitiesSharedPrefDataProcess.setBooleanDataToSharedPref(getApplicationContext(),"isAppChange",true);
                UtilitiesSharedPrefDataProcess.setBooleanDataToSharedPref(getApplicationContext(),"isUpdateAfterAppChange",true);
                UtilitiesSharedPrefDataProcess.setStringDataToSharedPref(getApplicationContext(),"firstDate", UtilitiesDateTimeProcess.getTodayDateByDateFormat());

                /*첫 메인화면 들어갔을 때만 처리하고 싶은 로직들이 있어서 이렇게 구현*/
                if(isFirstMainPage) {
                    UtilitiesSharedPrefDataProcess.changeFirstMainPageFlagVal(getApplicationContext());
                    Intent intent = new Intent(getApplicationContext(), RegisterDailyJobScheduleService.class);
                    startService(intent);
                    Toast.makeText(getApplicationContext(), "GoldenTime 서비스를 실행합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }


            }
            /* 이메일 등록 또는 퍼미션 설정 등 하나라도 안 한 경우 */
            else {
                /* 아래 코드를 통해 어떤 거 안했는 지 하나씩 체크 */

                if(!isEmailRegistered) {
                    Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                    startActivity(intent);
                }
                if(!isGetUsageStatsPermission)  checkUsageStatsPermission();
                if(!isBatteryOptimizationPermission)    checkBatteryOptimizationPermission();
            }
        }
    }

    /****************************** Dashboard 조작 함수 ******************************/
    /** 날짜 왼쪽 화살표 클릭 **/
    private void clickLeftArrow() {
        String currentDateUIFormattedStr = ((TextView) findViewById(R.id.currentDateTextId)).getText().toString();
        String previousDateStr = UtilitiesDateTimeProcess.getPreviousDateByUIComponentDateFormat(currentDateUIFormattedStr);
        int compareResultCode = UtilitiesDateTimeProcess.compareSelectedUIComponentDateWithFirstDate(getApplicationContext(), previousDateStr);

        if(compareResultCode > 0) {
            /*첫째날보다 이후 날짜 -> 히스토리 데이터 가져오기*/
            ImageViewCompat.setImageTintList(leftArrow, ColorStateList.valueOf(Color.parseColor("#505050")));
            ImageViewCompat.setImageTintList(rightArrow, ColorStateList.valueOf(Color.parseColor("#505050")));
            /** 해당 날짜 데이터 가져오기 **/
            setupDateDashboardUI(previousDateStr);
        }
        else if(compareResultCode == 0) {
            ImageViewCompat.setImageTintList(leftArrow, ColorStateList.valueOf(Color.parseColor("#b4b4b4")));
            ImageViewCompat.setImageTintList(rightArrow, ColorStateList.valueOf(Color.parseColor("#505050")));
            /** 해당 날짜(첫째날) 데이터 가져오기 **/
            setupDateDashboardUI(previousDateStr);
        }
        else {
            /*첫째날 이전 날짜 -> 예외처리*/
            ImageViewCompat.setImageTintList(leftArrow, ColorStateList.valueOf(Color.parseColor("#b4b4b4")));
            ImageViewCompat.setImageTintList(rightArrow, ColorStateList.valueOf(Color.parseColor("#505050")));
            Toast.makeText(getApplicationContext(), "이전 날짜 데이터 정보가 없습니다.",Toast.LENGTH_SHORT).show();
        }
    }
    /** 날짜 오른쪽 화살표 클릭 **/
    private void clickRightArrow() {
        String currentDateUIFormattedStr = ((TextView) findViewById(R.id.currentDateTextId)).getText().toString();
        String nextDateStr = UtilitiesDateTimeProcess.getNextDateByDateFormat(currentDateUIFormattedStr);
        int compareResultCode = UtilitiesDateTimeProcess.compareSelectedUIComponentDateWithTodayDate(nextDateStr);

        if(compareResultCode < 0) {
            ImageViewCompat.setImageTintList(leftArrow, ColorStateList.valueOf(Color.parseColor("#505050")));
            ImageViewCompat.setImageTintList(rightArrow, ColorStateList.valueOf(Color.parseColor("#505050")));
            /** 해당 날짜 데이터 가져오기 **/
            setupDateDashboardUI(nextDateStr);
        }
        else if(compareResultCode == 0) {
            ImageViewCompat.setImageTintList(leftArrow, ColorStateList.valueOf(Color.parseColor("#505050")));
            ImageViewCompat.setImageTintList(rightArrow, ColorStateList.valueOf(Color.parseColor("#b4b4b4")));
            /** 오늘날짜 데이터 가져오기 **/
            setupDateDashboardUI(nextDateStr);
        }
        else {
            /*오늘날짜보다 이후 날짜 -> 예외처리*/
            ImageViewCompat.setImageTintList(leftArrow, ColorStateList.valueOf(Color.parseColor("#505050")));
            ImageViewCompat.setImageTintList(rightArrow, ColorStateList.valueOf(Color.parseColor("#b4b4b4")));
            Toast.makeText(getApplicationContext(), "오늘 이후 날짜입니다.\n데이터 정보가 없습니다.",Toast.LENGTH_SHORT).show();
        }
    }


    /** Dashboard 시각화 함수**/
    private void setupDateDashboardUI(String dateStr) {
        /*매개변수 날짜 세팅*/
        TextView todatDateTextView = findViewById(R.id.currentDateTextId);
        todatDateTextView.setText(UtilitiesDateTimeProcess.getDateForUIComponent(dateStr));
        /* 매개변수 날짜에 따른 문구/워딩 세팅 */
        TextView dailyStatisticsWordTextView = findViewById(R.id.dayStatisticsTextId);
        dailyStatisticsWordTextView.setText(UtilitiesDateTimeProcess.getDailyStatisticsWording(dateStr));

        /**매개변수 날짜 일일 통계 데이터 로드**/
        /*사용 시간*/
        dateStr = UtilitiesDateTimeProcess.convertedDateStr(dateStr);
        String[] dashboardDataArry = UtilitiesSharedPrefDataProcess.getDataDashboardUI(getApplicationContext(), dateStr);
        TextView usageTimeValTextView = findViewById(R.id.dayUsageTimeValId);
        usageTimeValTextView.setText(UtilitiesDateTimeProcess.convertStringTimeToUIComponetFormat(dashboardDataArry[0]));
        /*성공 횟수*/
        TextView successValTextView = findViewById(R.id.daySuccessValId);
        successValTextView.setText(dashboardDataArry[1]);
        /*실패 횟수*/
        TextView failValTextView = findViewById(R.id.dayFailValId);
        int todayFailNum = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(), "dailyFail");
        failValTextView.setText(String.valueOf(todayFailNum));
        /*차감 골드*/
        DecimalFormat decimalFormat = new DecimalFormat("###,###");
        TextView dayLossGoldTextView = findViewById(R.id.dayLossGoldValId);

        String lossGold = decimalFormat.format(UtilitiesLocalDBProcess.getIncentiveSum(getApplicationContext(), UtilitiesDateTimeProcess.getDateByDBDateFormat(dateStr)));
        dayLossGoldTextView.setText("- "+lossGold+"골드");
        /*차감 금액*/
        TextView dayLossMoneyTextView = findViewById(R.id.dayLossMoneyValId);
        String lossMoney = decimalFormat.format(UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(this, "TodayIncentive") / 10);
        dayLossMoneyTextView.setText("- "+lossMoney+"원");

        /**전체 통계 데이터 로드**/
        /* 전체 통계 문구/워딩 세팅: 현재날짜+현재시간 */
        TextView totalStatisticsWordTextView = findViewById(R.id.totalStatisticsTextId);
        totalStatisticsWordTextView.setText("누적 통계("+UtilitiesDateTimeProcess.getTotalStatisticsWording()+")");

        int totalSuccessNum = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(), "totalSuccess");
        int totalFailNum = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(getApplicationContext(), "totalFail");
        /*전체 성공횟수*/
        TextView totalSuccessValTextView = findViewById(R.id.totalSuccessValId);
        totalSuccessValTextView.setText(String.valueOf(totalSuccessNum));
        /*전체 실패횟수*/
        TextView totalFailValTextView = findViewById(R.id.totalFailValId);
        totalFailValTextView.setText(String.valueOf(totalFailNum));

        /*전체 차감골드*/
        int totalLossGoldInt = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(this, "TotalIncentive");
        TextView totalLossGoldTextView = findViewById(R.id.totalLossGoldValId);
        String totalLossGold = decimalFormat.format(totalLossGoldInt);
        totalLossGoldTextView.setText(totalLossGold+"골드");
        /*전체 차감금액*/
        TextView totalLossMoneyTextView = findViewById(R.id.totalLossMoneyValId);
        String totalLossMoney = decimalFormat.format(totalLossGoldInt / 10);
        totalLossMoneyTextView.setText(totalLossMoney+"원");
    }

    /** Dashboard 시각화 함수: 어제 날짜 **/
    public void showCalendar() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                String selectedDateStr = UtilitiesDateTimeProcess.createSelectedDateForCalendar(month+1, day);
                int dateCode = UtilitiesDateTimeProcess.compareSelectedUIComponentDateWithTodayDate(selectedDateStr);
                if(dateCode > 0)    Toast.makeText(getApplicationContext(), "오늘 이후 날짜입니다.\n데이터 정보가 없습니다.",Toast.LENGTH_SHORT).show();
                else                setupDateDashboardUI(selectedDateStr);
            }
        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        String firstDateStr = UtilitiesSharedPrefDataProcess.getStringSharedPrefData(getApplicationContext(), "firstDate");
        datePickerDialog.getDatePicker().setMinDate(UtilitiesDateTimeProcess.convertFormattedDateToMills(firstDateStr));
        datePickerDialog.show();
    }


    /****************************** UsageStats 퍼미션 체크 함수 ******************************/
    private boolean checkUsageStatsPermission() {
        if(!hasPermissionToReadNetworkHistory()) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
            return false;
        }
        else    return true;
    }

    private boolean hasPermissionToReadNetworkHistory() {
        final AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) return true;

        appOps.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS, getApplicationContext().getPackageName(),
                new AppOpsManager.OnOpChangedListener() {
                    @Override
                    public void onOpChanged(String op, String packageName) {
                        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
                        if (mode != AppOpsManager.MODE_ALLOWED) {
                            return;
                        } else {
                            appOps.stopWatchingMode(this);
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(intent);
                        }
                    }
                });

        return false;
    }

    /****************************** Battery 최적화 퍼미션 체크 함수 ******************************/
    private boolean checkBatteryOptimizationPermission() {
        if(!hasPermissionToReadNetworkHistory2()) {
            String packageName = getApplication().getPackageName();
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse(String.format("package:%s", packageName)));
            startActivity(intent);
            return false;
        }
        else return true;
    }

    private boolean hasPermissionToReadNetworkHistory2() {
        String packageName = getApplication().getPackageName();
        // since REQUEST_IGNORE_BATTERY_OPTIMIZATIONS is **not** dangerous permission,
        // but we need to check that app has `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` permission.
        if (PackageManager.PERMISSION_GRANTED != getApplication().getPackageManager()
                .checkPermission(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        getApplication().getPackageName())) { // 권한 체크
            return false;
        }
        PowerManager powerManager = (PowerManager) getApplication().getSystemService(Context.POWER_SERVICE);
        boolean ignoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName);
        if (ignoringBatteryOptimizations) { // 예외사항에 이미 추가되었는지 확인
            return true;
        }
        return false;
    }


}
