package com.example.javanewwisebandit.goldentime_v1.Utils;

import android.content.Context;
import android.util.Log;

import com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase.AppDatabaseIncentiveThread;

//import kr.ac.kaist.jypark.goldentime_v1.RoomDB.AppDatabase.AppDatabaseIncentiveThread;

public class UtilitiesLocalDBProcess {
    public static int getIncentiveSum(Context context, String dateStr){
        AppDatabaseIncentiveThread thread = new AppDatabaseIncentiveThread(context, dateStr);
        Log.d("AA", "get Incentive Sum Thread state: "+thread.getState());
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            //TODO: handle exception
            return 0;
        }
        int result = thread.getSum();
        Log.d("AA", "Room DB debugging, sum of incentive: " + result);

        return result;
    }
}
