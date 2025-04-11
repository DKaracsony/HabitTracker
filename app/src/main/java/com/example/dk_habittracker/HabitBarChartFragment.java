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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HabitBarChartFragment extends Fragment {

    private final int habitId;
    private Calendar currentWeekStart;
    private TextView textWeekRange;
    private BarChart barChart;

    public HabitBarChartFragment(int habitId) {
        this.habitId = habitId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habit_bar_chart, container, false);

        barChart = view.findViewById(R.id.habitBarChart);
        textWeekRange = view.findViewById(R.id.textWeekRange);
        TextView buttonPrevWeek = view.findViewById(R.id.buttonPrevWeek);
        TextView buttonNextWeek = view.findViewById(R.id.buttonNextWeek);

        currentWeekStart = Calendar.getInstance();
        int dayOfWeek = currentWeekStart.get(Calendar.DAY_OF_WEEK);
        int offset = (dayOfWeek == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - dayOfWeek;
        currentWeekStart.add(Calendar.DAY_OF_MONTH, offset);

        loadChart();

        buttonPrevWeek.setOnClickListener(v -> {
            currentWeekStart.add(Calendar.DAY_OF_MONTH, -7);
            loadChart();
        });

        buttonNextWeek.setOnClickListener(v -> {
            Calendar nextWeek = (Calendar) currentWeekStart.clone();
            nextWeek.add(Calendar.DAY_OF_MONTH, 7);
            if (!nextWeek.after(getCurrentWeekStart())) {
                currentWeekStart = nextWeek;
                loadChart();
            }
        });

        return view;
    }

    private Calendar getCurrentWeekStart() {
        Calendar today = Calendar.getInstance();
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        int offset = (dayOfWeek == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - dayOfWeek;
        today.add(Calendar.DAY_OF_MONTH, offset);
        return today;
    }

    private void loadChart() {
        try (DBHelper dbHelper = new DBHelper(requireContext())) {
            List<Integer> completionRates = dbHelper.getWeeklyCompletionRates(habitId, currentWeekStart);

            SimpleDateFormat displayFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());
            Calendar weekEnd = (Calendar) currentWeekStart.clone();
            weekEnd.add(Calendar.DAY_OF_MONTH, 6);
            String range = displayFormat.format(currentWeekStart.getTime()) + " - " + displayFormat.format(weekEnd.getTime());
            textWeekRange.setText(range);

            List<BarEntry> entries = new ArrayList<>();
            for (int i = 0; i < completionRates.size(); i++) {
                entries.add(new BarEntry(i, completionRates.get(i)));
            }

            BarDataSet dataSet = new BarDataSet(entries, "");
            List<Integer> colors = new ArrayList<>();
            String habitType = dbHelper.getHabitById(habitId).getHabitType();

            for (BarEntry entry : entries) {
                float y = entry.getY();
                if (habitType.equalsIgnoreCase("Quit")) {
                    colors.add(y == 0f ? Color.parseColor("#F44336") : Color.parseColor("#4CAF50"));
                } else {
                    colors.add(y < 100f ? Color.parseColor("#F44336") : Color.parseColor("#4CAF50"));
                }
            }

            dataSet.setColors(colors);
            dataSet.setValueTextColor(Color.BLACK);

            BarData barData = new BarData(dataSet);
            barData.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return Math.round(value) + "%";
                }
            });

            barChart.setData(barData);

            List<String> weekdays = Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
            XAxis xAxis = barChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(weekdays));
            xAxis.setGranularity(1f);
            xAxis.setLabelCount(7);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);

            barChart.getAxisLeft().setAxisMinimum(0f);
            barChart.getAxisLeft().setAxisMaximum(100f);
            barChart.getAxisLeft().setDrawGridLines(false);
            barChart.getAxisLeft().setLabelCount(5, true);
            barChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return ((int) value) + "%";
                }
            });

            barChart.getAxisRight().setEnabled(false);
            barChart.getLegend().setEnabled(false);
            barChart.getDescription().setEnabled(false);

            barChart.setTouchEnabled(false);
            barChart.setScaleEnabled(false);
            barChart.setPinchZoom(false);
            barChart.setDoubleTapToZoomEnabled(false);
            barChart.setDragEnabled(false);
            barChart.setHighlightPerTapEnabled(false);

            barChart.invalidate();
        }
    }
}
