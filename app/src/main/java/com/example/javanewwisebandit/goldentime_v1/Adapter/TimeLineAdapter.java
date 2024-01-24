package com.example.javanewwisebandit.goldentime_v1.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javanewwisebandit.R;
import com.example.javanewwisebandit.goldentime_v1.Model.TimeLineModel;

import java.sql.Array;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

//import kr.ac.kaist.jypark.goldentime_v1.Model.TimeLineModel;
//import kr.ac.kaist.jypark.goldentime_v1.R;

public class TimeLineAdapter extends RecyclerView.Adapter<TimeLineAdapter.ViewHolder> {
    private List<TimeLineModel> modelList = null;
    private Context context;

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView title;
        TextView content;
        TextView date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.tv_title);
            content = itemView.findViewById(R.id.tv_content);
            date = itemView.findViewById(R.id.tv_date);
        }
    }

    public TimeLineAdapter(Context context, ArrayList<TimeLineModel> modelList) {
        this.context = context;
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.timeline_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final TimeLineModel model = modelList.get(position);
        final int color = ContextCompat.getColor(context,
                model.isHead ? android.R.color.black : android.R.color.darker_gray);
        holder.title.setTextColor(color);
        holder.title.setText(model.text);
        
        holder.content.setTextColor(color);
        holder.content.setText(model.content);
        
        holder.date.setTextColor(color);
        holder.date.setText(model.time);
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
