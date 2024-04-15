package com.example.javanewwisebandit.goldentime_v1.Fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.javanewwisebandit.R;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesDateTimeProcess;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesLocalDBProcess;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;

import java.text.DecimalFormat;
import java.util.Calendar;

//import kr.ac.kaist.jypark.goldentime_v1.R;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesDateTimeProcess;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesLocalDBProcess;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashBoardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashBoardFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    boolean isAppChanged;
    boolean isUpdateAfterAppChange;
    ImageView rightArrow;
    ImageView leftArrow;

    ViewGroup dashboardView;
    Context appContext;

    public DashBoardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DashBoardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DashBoardFragment newInstance(String param1, String param2) {
        DashBoardFragment fragment = new DashBoardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        dashboardView = (ViewGroup) inflater.inflate(R.layout.fragment_dash_board_gain, container, false);

        return dashboardView;
    }

    @Override
    public void onViewCreated (@NonNull View view,
                               Bundle savedInstanceState){
        appContext = getActivity().getApplicationContext();

        leftArrow = dashboardView.findViewById(R.id.leftArrowId);
        leftArrow.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickLeftArrow();
            }
        });

        rightArrow = dashboardView.findViewById(R.id.rightArrowId);
        rightArrow.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRightArrow();
            }
        });

        ImageView calendarIcon = dashboardView.findViewById(R.id.calendarIconId);
        calendarIcon.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalendar();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setupDateDashboardUI(
                UtilitiesDateTimeProcess.getTodayDateByDateFormat());

    }

    /****************************** Dashboard 조작 함수 ******************************/
    /** 날짜 왼쪽 화살표 클릭 **/
    private void clickLeftArrow() {
        String currentDateUIFormattedStr = ((TextView) dashboardView.findViewById(R.id.currentDateTextId)).getText().toString();
        String previousDateStr = UtilitiesDateTimeProcess.getPreviousDateByUIComponentDateFormat(currentDateUIFormattedStr);
        int compareResultCode = UtilitiesDateTimeProcess.compareSelectedUIComponentDateWithFirstDate(appContext, previousDateStr);

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
            Toast.makeText(appContext, "이전 날짜 데이터 정보가 없습니다.",Toast.LENGTH_SHORT).show();
        }
    }
    /** 날짜 오른쪽 화살표 클릭 **/
    private void clickRightArrow() {
        String currentDateUIFormattedStr = ((TextView) dashboardView.findViewById(R.id.currentDateTextId)).getText().toString();
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
            Toast.makeText(appContext, "오늘 이후 날짜입니다.\n데이터 정보가 없습니다.",Toast.LENGTH_SHORT).show();
        }
    }


    /** Dashboard 시각화 함수**/
    private void setupDateDashboardUI(String dateStr) {
        /*매개변수 날짜 세팅*/
        TextView todatDateTextView = dashboardView.findViewById(R.id.currentDateTextId);
        todatDateTextView.setText(UtilitiesDateTimeProcess.getDateForUIComponent(dateStr));
        /* 매개변수 날짜에 따른 문구/워딩 세팅 */
        TextView dailyStatisticsWordTextView = dashboardView.findViewById(R.id.dayStatisticsTextId);
        dailyStatisticsWordTextView.setText(UtilitiesDateTimeProcess.getDailyStatisticsWording(dateStr));

        /**매개변수 날짜 일일 통계 데이터 로드**/
        /*사용 시간*/
        dateStr = UtilitiesDateTimeProcess.convertedDateStr(dateStr);
        String[] dashboardDataArry = UtilitiesSharedPrefDataProcess.getDataDashboardUI(appContext, dateStr);
        TextView usageTimeValTextView = dashboardView.findViewById(R.id.dayUsageTimeValId);
        usageTimeValTextView.setText(UtilitiesDateTimeProcess.convertStringTimeToUIComponetFormat(dashboardDataArry[0]));
        /*성공 횟수*/
        TextView successValTextView = dashboardView.findViewById(R.id.daySuccessValId);
        successValTextView.setText(dashboardDataArry[1]);
        /*실패 횟수*/
        TextView failValTextView = dashboardView.findViewById(R.id.dayFailValId);
        //int todayFailNum = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(appContext, "dailyFail");
        //failValTextView.setText(String.valueOf(todayFailNum));
        failValTextView.setText(dashboardDataArry[2]);
        /*차감 골드*/
//        DecimalFormat decimalFormat = new DecimalFormat("###,###");
//        TextView dayLossGoldTextView = dashboardView.findViewById(R.id.dayLossGoldValId);
//
//        String lossGold = decimalFormat.format(UtilitiesLocalDBProcess.getIncentiveSum(appContext, UtilitiesDateTimeProcess.getDateByDBDateFormat(dateStr)));
//        dayLossGoldTextView.setText("- "+lossGold+"골드");
//        /*차감 금액*/
//        TextView dayLossMoneyTextView = dashboardView.findViewById(R.id.dayLossMoneyValId);
//        String lossMoney = decimalFormat.format(UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(appContext, "TodayIncentive") / 10);
//        dayLossMoneyTextView.setText("(- "+lossMoney+"원)");

        /***획득 골드***/
        DecimalFormat decimalFormat = new DecimalFormat("###,###");
        TextView dayGainGoldTextView = dashboardView.findViewById(R.id.dayGainGoldValId);

        String gainGold = decimalFormat.format(UtilitiesLocalDBProcess.getIncentiveSum(appContext, UtilitiesDateTimeProcess.getDateByDBDateFormat(dateStr)));
        dayGainGoldTextView.setText("+ "+gainGold+"골드");
        /*차감 금액*/
        TextView dayGainMoneyTextView = dashboardView.findViewById(R.id.dayGainMoneyValId);
        String gainMoney = decimalFormat.format(UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(appContext, "TodayIncentive") / 10);
        dayGainMoneyTextView.setText("(+ "+gainMoney+"원)");


        /**전체 통계 데이터 로드**/
        /* 전체 통계 문구/워딩 세팅: 현재날짜+현재시간 */
        TextView totalStatisticsWordTextView = dashboardView.findViewById(R.id.totalStatisticsTextId);
        totalStatisticsWordTextView.setText("누적 통계("+UtilitiesDateTimeProcess.getTotalStatisticsWording()+")");

        int totalSuccessNum = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(appContext, "totalSuccess");
        int totalFailNum = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(appContext, "totalFail");
        /*전체 성공횟수*/
        TextView totalSuccessValTextView = dashboardView.findViewById(R.id.totalSuccessValId);
        totalSuccessValTextView.setText(String.valueOf(totalSuccessNum));
        /*전체 실패횟수*/
        TextView totalFailValTextView = dashboardView.findViewById(R.id.totalFailValId);
        totalFailValTextView.setText(String.valueOf(totalFailNum));

//        /*전체 차감골드*/
//        int totalLossGoldInt = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(appContext, "TotalIncentive");
//        TextView totalLossGoldTextView = dashboardView.findViewById(R.id.totalLossGoldValId);
//        String totalLossGold = decimalFormat.format(totalLossGoldInt);
//        totalLossGoldTextView.setText("- "+totalLossGold+"골드");
//        /*전체 차감금액*/
//        TextView totalLossMoneyTextView = dashboardView.findViewById(R.id.totalLossMoneyValId);
//        String totalLossMoney = decimalFormat.format(totalLossGoldInt / 10);
//        totalLossMoneyTextView.setText("(- "+totalLossMoney+"원)");
//
//        if (UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getActivity(), "isFollowup")) {
//            dayLossMoneyTextView.setText("");
//            totalLossMoneyTextView.setText("");
//        }
        /**전체 획득 골드**/
        int totalGainGoldInt = UtilitiesSharedPrefDataProcess.getIntegerSharedPrefData(appContext, "TotalIncentive");
        TextView totalGainGoldTextView = dashboardView.findViewById(R.id.totalGainGoldValId);
        String totalGainGold = decimalFormat.format(totalGainGoldInt);
        totalGainGoldTextView.setText("+ "+totalGainGold+"골드");
        /**전체 획득 금액**/
        TextView totalGainMoneyTextView = dashboardView.findViewById(R.id.totalGainMoneyValId);
        String totalGainMoney = decimalFormat.format(totalGainGoldInt / 10);
        totalGainMoneyTextView.setText("(+ "+totalGainMoney+"원)");

        if (UtilitiesSharedPrefDataProcess.getBooleanSharedPrefData(getActivity(), "isFollowup")) {
            dayGainMoneyTextView.setText("");
            totalGainMoneyTextView.setText("");
        }
    }

    /** Dashboard 시각화 함수: 어제 날짜 **/
    public void showCalendar() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                String selectedDateStr = UtilitiesDateTimeProcess.createSelectedDateForCalendar(month+1, day);
                int dateCode = UtilitiesDateTimeProcess.compareSelectedUIComponentDateWithTodayDate(selectedDateStr);
                if(dateCode > 0)    Toast.makeText(getActivity(), "오늘 이후 날짜입니다.\n데이터 정보가 없습니다.",Toast.LENGTH_SHORT).show();
                else                setupDateDashboardUI(selectedDateStr);
            }
        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        String firstDateStr = UtilitiesSharedPrefDataProcess.getStringSharedPrefData(getActivity(), "firstDate");
        datePickerDialog.getDatePicker().setMinDate(UtilitiesDateTimeProcess.convertFormattedDateToMills(firstDateStr));
        datePickerDialog.show();
    }


}