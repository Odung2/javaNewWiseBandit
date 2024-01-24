package com.example.javanewwisebandit.goldentime_v1.Fragment;
//import com.lin.timeline.TimeLineDecoration;
//import static com.lin.timeline.TimeLineDecoration.BEGIN;
//import static com.lin.timeline.TimeLineDecoration.END_FULL;
//import static com.lin.timeline.TimeLineDecoration.NORMAL;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javanewwisebandit.R;
import com.example.javanewwisebandit.goldentime_v1.Adapter.TimeLineAdapter;
import com.example.javanewwisebandit.goldentime_v1.Model.TimeLineModel;
import com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase.AppDatabaseGetAllThread;
import com.example.javanewwisebandit.goldentime_v1.RoomDB.AppDatabase.TimeLineTuple;


import java.util.ArrayList;
import java.util.List;

public class TimeLineFragment extends Fragment {

    RecyclerView recyclerView;
    TimeLineAdapter mAdapter;
    
    ArrayList<TimeLineModel> mDataList = null;

    ViewGroup timelineView;
    Context appContext;
    
    public TimeLineFragment() {
        // Required empty public constructor
    }
    
    public static TimeLineFragment newInstance() {
        TimeLineFragment fragment = new TimeLineFragment();
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
        timelineView = (ViewGroup) inflater.inflate(R.layout.fragment_timeline, container, false);
        appContext = getActivity().getApplicationContext();

//        final TimeLineDecoration decoration = new TimeLineDecoration(appContext)
//                .setLineColor(R.color.colorPrimary)
//                .setLineWidth(1)
//                .setLeftDistance(16)
//                .setTopDistance(12)
//                .setBeginMarker(R.drawable.begin_marker)
//                .setMarkerRadius(8)
//                .setMarkerColor(R.color.colorAccent)
//                .setCallback(new TimeLineDecoration.TimeLineAdapter() {
//                    //or new TimeLineDecoration.TimeLineCallback
//                    @Override public int getTimeLineType(int position) {
//                        if (position == 0) return BEGIN;
//                        else if (position == mAdapter.getItemCount() - 1) return END_FULL;
//                        else return NORMAL;
//                    }
//                });

        //TODO: room DB에서 데이터 가져오기

        AppDatabaseGetAllThread thread = new AppDatabaseGetAllThread(appContext);
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mDataList = new ArrayList<>();
        List<TimeLineTuple> subList = thread.getAllTimeLineModels();

        for (int i = 0; i < subList.size(); i++) {
            String contentStr = subList.get(i).success ? "골드 획득하셨습니다." : "골드 획득에 실패하셨습니다.";
            mDataList.add(new TimeLineModel(subList.get(i).success, "TODO: 사용 시간", subList.get(i).date + subList.get(i).timeSlot, subList.get(i).incentive + contentStr));
        }

        mAdapter = new TimeLineAdapter(appContext, mDataList);
        recyclerView = timelineView.findViewById(R.id.recycle_timeline);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(appContext));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        recyclerView.addItemDecoration(decoration);

        // Inflate the layout for this fragment
        return timelineView;
    }
}