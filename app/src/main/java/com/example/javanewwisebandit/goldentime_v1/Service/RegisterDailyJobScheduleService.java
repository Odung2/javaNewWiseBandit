package com.example.javanewwisebandit.goldentime_v1.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.javanewwisebandit.goldentime_v1.JobWorker.AppChangeWorker;
import com.example.javanewwisebandit.goldentime_v1.JobWorker.OnTimeAlarmWorker;
import com.example.javanewwisebandit.goldentime_v1.JobWorker.OnTimeDBUpdateWorker;
import com.example.javanewwisebandit.goldentime_v1.JobWorker.UsageStatDBUpdateWorker;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;

import java.util.Random;
import java.util.concurrent.TimeUnit;

//import kr.ac.kaist.jypark.goldentime_v1.JobWorker.AppChangeWorker;
//import kr.ac.kaist.jypark.goldentime_v1.JobWorker.OnTimeAlarmWorker;
//import kr.ac.kaist.jypark.goldentime_v1.JobWorker.OnTimeDBUpdateWorker;
//import kr.ac.kaist.jypark.goldentime_v1.JobWorker.UsageStatDBUpdateWorker;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesSharedPrefDataProcess;

public class RegisterDailyJobScheduleService extends Service {
    public RegisterDailyJobScheduleService() {}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PeriodicWorkRequest periodicWorkRequest1 = new PeriodicWorkRequest.Builder(OnTimeAlarmWorker.class, 1, TimeUnit.DAYS).build(); //정시 타이머 알람 등록 job
        PeriodicWorkRequest periodicWorkRequest2 = new PeriodicWorkRequest.Builder(OnTimeDBUpdateWorker.class, 1, TimeUnit.DAYS).build(); //OnTime 사용 통계 DB저장 job
        PeriodicWorkRequest periodicWorkRequest3 = new PeriodicWorkRequest.Builder(UsageStatDBUpdateWorker.class, 1, TimeUnit.DAYS).build(); //UsageStats 사용 통계 DB저장 job
        OneTimeWorkRequest oneTimeWorkRequest    = new OneTimeWorkRequest.Builder(AppChangeWorker.class).build();       //baseline -> intervention 앱 전환하는 job

        WorkManager.getInstance(getApplicationContext()).enqueue(periodicWorkRequest1);
        WorkManager.getInstance(getApplicationContext()).enqueue(periodicWorkRequest2);
        WorkManager.getInstance(getApplicationContext()).enqueue(periodicWorkRequest3);
        WorkManager.getInstance(getApplicationContext()).enqueue(oneTimeWorkRequest);


        /** DB에 업데이트하는 시간을 랜덤하게 형성한 후 원격에 저장하기 **/
        /* 랜덤 시간(분,초) 생성 */
        Random rand = new Random();
        int hourOfTheMin = rand.nextInt(31)+10;
        int hourOfTheSec = rand.nextInt(60);
        int ustatsHourOfTheMin = rand.nextInt(41)+10;
        int ustatsHourOfTheSec = rand.nextInt(60);

        /* 예외처리: updateTime(분)이 두개가 똑같으면 다르게 설정 */
        if(hourOfTheMin == ustatsHourOfTheMin)  ustatsHourOfTheMin -= 5;


        /* 랜덤 시간(분,초) 저장 */
        UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(getApplicationContext(),"updateMin", hourOfTheMin);
        UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(getApplicationContext(),"updateSec", hourOfTheSec);
        UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(getApplicationContext(),"updateUsageStatsMin", ustatsHourOfTheMin);
        UtilitiesSharedPrefDataProcess.setIntegerDataToSharedPref(getApplicationContext(),"updateUsageStatsSec", ustatsHourOfTheSec);

        /* 원격 DB에 업데이트 시간(분,초)을 저장 */
        Intent serviceIntent = new Intent(getApplicationContext(), RemoteDBUpdateService.class);
        serviceIntent.setAction(RemoteDBUpdateService.ACTION_INITIAL_DATA_UPDATE);
        serviceIntent.putExtra("updateTime", hourOfTheMin+":"+hourOfTheSec);
        serviceIntent.putExtra("ustatsUpdateTime", ustatsHourOfTheMin+":"+ustatsHourOfTheSec);
        getApplicationContext().startService(serviceIntent);

        return START_NOT_STICKY;
    }

}
