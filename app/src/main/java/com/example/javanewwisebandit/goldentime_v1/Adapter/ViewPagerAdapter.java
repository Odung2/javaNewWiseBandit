package com.example.javanewwisebandit.goldentime_v1.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.javanewwisebandit.goldentime_v1.Fragment.DashBoardFragment;
import com.example.javanewwisebandit.goldentime_v1.Fragment.ExperimentFragment;

//import kr.ac.kaist.jypark.goldentime_v1.Fragment.DashBoardFragment;
//import kr.ac.kaist.jypark.goldentime_v1.Fragment.ExperimentFragment;
//import kr.ac.kaist.jypark.goldentime_v1.Fragment.TimeLineFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private final int NUM_TABS = 2;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 0:
                return new DashBoardFragment();
            case 1:
                return new ExperimentFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}
