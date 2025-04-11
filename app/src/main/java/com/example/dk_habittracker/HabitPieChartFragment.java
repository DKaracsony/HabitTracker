package com.example.dk_habittracker;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HabitPieChartFragment extends Fragment {

    private int habitId;
    private DBHelper dbHelper;
    private Calendar currentMonth;
    private TextView textMonth;
    private PieChart pieChart;

    public HabitPieChartFragment(int habitId) {
        this.habitId = habitId;
    }

    public HabitPieChartFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habit_pie_chart, container, false);

        dbHelper = new DBHelper(requireContext());
        pieChart = view.findViewById(R.id.habitPieChart);
        textMonth = view.findViewById(R.id.textPieMonth);
        TextView buttonPrev = view.findViewById(R.id.buttonPiePrevMonth);
        TextView buttonNext = view.findViewById(R.id.buttonPieNextMonth);

        currentMonth = Calendar.getInstance();
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);

        buttonPrev.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            loadChart();
        });

        buttonNext.setOnClickListener(v -> {
            Calendar temp = (Calendar) currentMonth.clone();
            temp.add(Calendar.MONTH, 1);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.DAY_OF_MONTH, 1);

            if (!temp.after(today)) {
                currentMonth = temp;
                loadChart();
            }
        });

        loadChart();
        return view;
    }

    private void loadChart() {
        Habit habit = dbHelper.getHabitById(habitId);
        if (habit == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("LLLL yyyy", requireContext().getResources().getConfiguration().getLocales().get(0));
        textMonth.setText(sdf.format(currentMonth.getTime()));

        int[] result = dbHelper.getMonthlyCompletionRatio(habitId, currentMonth);
        int completed = result[0];
        int missed = result[1];

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(completed, "Completed"));
        entries.add(new PieEntry(missed, "Missed"));

        PieDataSet dataSet = new PieDataSet(entries, "Success");
        dataSet.setColors(Color.GREEN, Color.RED);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(14f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf(Math.round(value));
            }
        });
        pieChart.setData(pieData);

        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.parseColor("#FFB16C"));

        pieChart.setTouchEnabled(false);
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setUsePercentValues(false);
        pieChart.invalidate();
    }
}
