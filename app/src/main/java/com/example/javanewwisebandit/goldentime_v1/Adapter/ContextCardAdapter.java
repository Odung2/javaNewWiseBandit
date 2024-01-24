package com.example.javanewwisebandit.goldentime_v1.Adapter;

import java.util.ArrayList;

import android.content.Context;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javanewwisebandit.R;
import com.example.javanewwisebandit.goldentime_v1.Model.ContextCardDataObject;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesDateTimeProcess;

//import kr.ac.kaist.jypark.goldentime_v1.Model.ContextCardDataObject;
//import kr.ac.kaist.jypark.goldentime_v1.Model.IncentiveInfoTuple;
//import kr.ac.kaist.jypark.goldentime_v1.R;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesDateTimeProcess;

public class ContextCardAdapter extends RecyclerView.Adapter<ContextCardAdapter.ViewHolder> {

    private ArrayList<ContextCardDataObject> mData = null ;
    private Context context;

    public class ViewHolder extends RecyclerView.ViewHolder {
        ViewGroup cardView;
        View detailView;

        TextView context;
        TextView cardDescript;
        /*TextView likelihood;
        TextView optimalIncentive;
        TextView timeSlot;
        TextView summaryText;

        ImageButton expandableBtn;*/

        RecyclerView progressRecyclerView;

        ViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardViewBaseLayout);
            detailView = itemView.findViewById(R.id.detailMABLayout);

            context = itemView.findViewById(R.id.cardTitleTextId);
            //cardDescript = itemView.findViewById(R.id.cardDescriptionTextId);
            /*likelihood = itemView.findViewById(R.id.likelihoodValId);
            optimalIncentive = itemView.findViewById(R.id.optimalIncentiveValId);
            timeSlot = itemView.findViewById(R.id.timeSlotValId);
            summaryText = itemView.findViewById(R.id.summaryTextValId);
            expandableBtn = itemView.findViewById(R.id.expandableBtn);*/
            progressRecyclerView = itemView.findViewById(R.id.progressViewId);
        }

    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    public ContextCardAdapter(Context context, ArrayList<ContextCardDataObject> list) {
        mData = list ;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.fragment_context_viewgroup, parent, false) ;
        ViewHolder vh = new ViewHolder(view) ;

        return vh ;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ContextCardDataObject data = mData.get(position);

        int[] timeBox = UtilitiesDateTimeProcess.getTimeSlotByContext(data.getContext());
        String[] timeBoxAdd = new String [timeBox.length];
        for (int i=0; i<timeBox.length; i++){
            if (timeBox[i] > 12){
                timeBoxAdd[i] = timeBox[i]%12 + "pm";
            }
            else {
                timeBoxAdd[i] = timeBox[i] + "am";
            }
        }
        String contextString;
        if (data.getContext().equals("work")){
            contextString = "평일 근무 시간";
        } else if (data.getContext().equals("non-work")){
            contextString = "평일 여가 시간";
        } else if (data.getContext().equals("weekend")){
            contextString = "주말";
        } else {
            contextString = "";
        }
        int incentive = (int) data.getOptimalIncentiveTuple().getIncentive();
        /*int expectedRate = (int) (data.getOptimalIncentiveTuple().getExpectedRate() * 100);
        int timeSlot = data.getTotalTimeSlot();*/

        holder.context.setText(context.getString(R.string.cardTitleText, contextString, timeBoxAdd[0], timeBoxAdd[1]));
        //holder.cardDescript.setText(context.getString(R.string.cardDescriptionText, incentive));
        /*holder.likelihood.setText(context.getString(R.string.likelihoodVal, expectedRate));
        holder.optimalIncentive.setText(context.getString(R.string.optimalIncentiveVal, incentive));
        holder.timeSlot.setText(context.getString(R.string.timeSlotVal, timeSlot));
        holder.summaryText.setText(context.getString(R.string.summaryTextVal, timeSlot, incentive, expectedRate));*/

        holder.progressRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.progressRecyclerView.setAdapter(new ProgressRecyclerViewAdapter(data.getIncentiveInfoTupleList()));
    
        /*holder.detailView.setVisibility(View.GONE);
        holder.cardView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    float deg = holder.expandableBtn.getRotation() + 180F;
                    holder.expandableBtn.animate().rotation(deg).setInterpolator(new AccelerateDecelerateInterpolator());

                    if(holder.detailView.getVisibility() == View.VISIBLE){
                        TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
                        holder.detailView.setVisibility(View.GONE);
                    } else {
                        TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
                        holder.detailView.setVisibility(View.VISIBLE);
                    }
                }

                return true;
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return mData.size() ;
    }
}