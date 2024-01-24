package com.example.javanewwisebandit.goldentime_v1.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javanewwisebandit.R;
import com.example.javanewwisebandit.goldentime_v1.Model.IncentiveInfoTuple;

import java.util.ArrayList;

//import kr.ac.kaist.jypark.goldentime_v1.Model.IncentiveInfoTuple;
//import kr.ac.kaist.jypark.goldentime_v1.R;

public class ProgressRecyclerViewAdapter extends RecyclerView.Adapter<ProgressRecyclerViewAdapter.ViewHolder> {

    private ArrayList<IncentiveInfoTuple> mData = null;
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView deductedAmountText;
        private final ProgressBar successProgressBar;
        private final TextView successRateText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deductedAmountText = (TextView) itemView.findViewById(R.id.deductedAmountValId);
            successProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            successRateText = (TextView) itemView.findViewById(R.id.successRateValId);
        }

        public TextView getDeductedAmountText() {
            return deductedAmountText;
        }
        public ProgressBar getSuccessProgressBar() {
            return successProgressBar;
        }
        public TextView getSuccessRateText() {
            return successRateText;
        }
    }
    
    public ProgressRecyclerViewAdapter(ArrayList<IncentiveInfoTuple> list){
        mData = list;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
            .inflate(R.layout.recyclerview_progress_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IncentiveInfoTuple tuple = mData.get(position);
        holder.getDeductedAmountText().setText(String.valueOf((int) tuple.getIncentive())+"골드");
        holder.getSuccessProgressBar().setProgress((int) (tuple.getExpectedRate() * 100));
        holder.getSuccessRateText().setText(String.valueOf("("+(int) (tuple.getExpectedRate()* 100))+"%)");
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

}
