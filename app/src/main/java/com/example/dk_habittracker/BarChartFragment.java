package com.example.dk_habittracker;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class BarChartFragment extends Fragment {

    private final List<Habit> habits;
    private final DBHelper dbHelper;
    private final String selectedDate;

    public BarChartFragment(List<Habit> habits, DBHelper dbHelper, String selectedDate) {
        this.habits = habits;
        this.dbHelper = dbHelper;
        this.selectedDate = selectedDate;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bar_chart, container, false);
        BarChart barChart = view.findViewById(R.id.barChart);

        setupBarChart(barChart);
        return view;
    }

    private void setupBarChart(BarChart barChart) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> habitNames = new ArrayList<>();
        List<Integer> barColors = new ArrayList<>();

        for (int i = 0; i < habits.size(); i++) {
            Habit habit = habits.get(i);
            int progress = dbHelper.getProgressForPeriod(habit.getId(), habit.getGoalPeriod(), selectedDate);

            entries.add(new BarEntry(i, progress));
            String habitName = habit.getName();
            if (habitName.length() > 14) {
                habitName = habitName.substring(0, 14) + "...";
            }
            habitNames.add(habitName);

            boolean isCompleted = habit.getHabitType().equals("Quit")
                    ? progress <= habit.getGoal()
                    : progress >= habit.getGoal();

            barColors.add(isCompleted ? Color.GREEN : Color.RED);
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(barColors);
        dataSet.setValueTextSize(13.5f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.8f);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        barChart.setData(data);
        barChart.invalidate();
        barChart.animateY(1000);

        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setEnabled(false);
        barChart.getAxisRight().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(habitNames));
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        int visibleBarLimit = 3;
        int baseWidthPerBar = 110;

        barChart.setVisibleXRangeMaximum(visibleBarLimit);
        barChart.getLayoutParams().width = Math.max(habitNames.size(), visibleBarLimit) * baseWidthPerBar;
        xAxis.setTextSize(13f);

        barChart.requestLayout();

        xAxis.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        xAxis.setLabelCount(habitNames.size());
        xAxis.setAvoidFirstLastClipping(false);

        barChart.getDescription().setEnabled(false);
        barChart.setDrawBorders(false);
        barChart.getLegend().setEnabled(false);
        barChart.setExtraBottomOffset(15f);

        barChart.requestLayout();
    }

}
