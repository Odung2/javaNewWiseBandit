package com.example.javanewwisebandit.goldentime_v1.Fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.javanewwisebandit.R;
import com.example.javanewwisebandit.goldentime_v1.Adapter.ContextCardAdapter;
import com.example.javanewwisebandit.goldentime_v1.Incentive;
import com.example.javanewwisebandit.goldentime_v1.Model.ContextCardDataObject;
import com.example.javanewwisebandit.goldentime_v1.Model.IncentiveInfoTuple;
import com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase.AppDatabaseExpectThread;
import com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase.AppDatabaseViewThread;
import com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase.ViewTuple;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesDateTimeProcess;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExperimentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExperimentFragment extends Fragment {

    RecyclerView recyclerView;
    ContextCardAdapter mAdapter;
    ArrayList<ContextCardDataObject> mData = null;
    ArrayList<IncentiveInfoTuple> mSubData = null;

    ViewGroup experimentView;
    Context appContext;

    public ExperimentFragment() {
        // Required empty public constructor
    }

    public static ExperimentFragment newInstance() {
        ExperimentFragment fragment = new ExperimentFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        experimentView = (ViewGroup) inflater.inflate(R.layout.fragment_experiment, container, false);
        appContext = getActivity().getApplicationContext();

        recyclerView = experimentView.findViewById(R.id.recycleLayout);

        //TODO: context 별 timeslot 개수 가져오는 thread 실행, 각 incentive마다 expect 결과값 모아서 IncentiveInfoTuple 저장

        String[] contexts = UtilitiesDateTimeProcess.getAllContext();
        mData = new ArrayList<>();
        for (String context : contexts) {
            AppDatabaseViewThread thread = new AppDatabaseViewThread(context, appContext);
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                //TODO: handle exception
                return experimentView;
            }
            int timeSlot = thread.getSelectResult();

            AppDatabaseExpectThread ethread = new AppDatabaseExpectThread(context, appContext);
            ethread.start();
            try {
                ethread.join();
            } catch (InterruptedException e) {
                //TODO: handle exception
                return experimentView;
            }
            List<ViewTuple> result = ethread.getSelectResult();

            mSubData = new ArrayList<>();
            IncentiveInfoTuple optimal = new IncentiveInfoTuple(0, 0);
            for (ViewTuple tuple : result){
                double expectedRate = (double) tuple.numSuccess / tuple.numTotalTry;
                IncentiveInfoTuple infoTuple = new IncentiveInfoTuple(tuple.incentive, expectedRate);
                mSubData.add(infoTuple);
                if (optimal.getExpectedRate() == 0 && optimal.getIncentive() == 0) optimal = infoTuple;
                else if (optimal.getExpectedRate() < expectedRate) optimal = infoTuple;
                else if (optimal.getExpectedRate() == expectedRate && optimal.getIncentive() >= tuple.incentive) optimal = infoTuple;
            }

            if (mSubData.size() < Incentive.getInstance().incentives.length){
                for (double incentive : Incentive.getInstance().incentives){
                    if (mSubData.stream().allMatch(tuple -> tuple.getIncentive() != incentive))
                    mSubData.add(new IncentiveInfoTuple(incentive, 0));
                }
            }

            ContextCardDataObject object = new ContextCardDataObject(context, optimal, timeSlot, mSubData);
            mData.add(object);
        }

        // Adapter, LayoutManager 연결
        mAdapter = new ContextCardAdapter(appContext, mData);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(appContext));

        return experimentView;
    }
}