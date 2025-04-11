package com.example.dk_habittracker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class ChartPagerAdapter extends FragmentStateAdapter {

    private final List<Fragment> chartFragments;

    public ChartPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<Fragment> chartFragments) {
        super(fragmentActivity);
        this.chartFragments = chartFragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return chartFragments.get(position);
    }

    @Override
    public int getItemCount() {
        return chartFragments.size();
    }
}
