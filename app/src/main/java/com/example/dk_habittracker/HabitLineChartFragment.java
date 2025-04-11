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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HabitLineChartFragment extends Fragment {

    private int habitId;
    private Calendar currentMonth;
    private DBHelper dbHelper;
    private LineChart lineChart;
    private TextView textMonth;

    public HabitLineChartFragment(int habitId) {
        this.habitId = habitId;
    }

    public HabitLineChartFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habit_line_chart, container, false);

        dbHelper = new DBHelper(requireContext());
        lineChart = view.findViewById(R.id.habitLineChart);
        textMonth = view.findViewById(R.id.textMonth);

        TextView buttonPrevMonth = view.findViewById(R.id.buttonPrevMonth);
        TextView buttonNextMonth = view.findViewById(R.id.buttonNextMonth);

        currentMonth = Calendar.getInstance();
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);

        buttonPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            loadChart();
        });

        buttonNextMonth.setOnClickListener(v -> {
            Calendar nextMonth = (Calendar) currentMonth.clone();
            nextMonth.add(Calendar.MONTH, 1);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.DAY_OF_MONTH, 1);

            if (!nextMonth.after(today)) {
                currentMonth = nextMonth;
                loadChart();
            }
        });

        loadChart();

        return view;
    }

    private void loadChart() {
        SimpleDateFormat sdfDisplay = new SimpleDateFormat("LLLL yyyy", requireContext().getResources().getConfiguration().getLocales().get(0));
        textMonth.setText(sdfDisplay.format(currentMonth.getTime()));

        List<HabitProgress> progressList = dbHelper.getHabitProgressForCurrentMonth(habitId, (Calendar) currentMonth.clone());

        List<Entry> entries = new ArrayList<>();
        List<Integer> circleColors = new ArrayList<>();

        Habit habit = dbHelper.getHabitById(habitId);
        int goal = habit.getGoal();
        String type = habit.getHabitType();

        for (int i = 0; i < progressList.size(); i++) {
            HabitProgress progress = progressList.get(i);
            int value = progress.getProgress();
            entries.add(new Entry(i, value));

            boolean isCompleted = type.equalsIgnoreCase("Quit") ? value <= goal : value >= goal;
            circleColors.add(isCompleted ? Color.GREEN : Color.RED);
        }

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setColor(Color.BLACK);
        dataSet.setCircleColors(circleColors);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawValues(true);
        dataSet.setLineWidth(2f);
        dataSet.setDrawFilled(true);
        dataSet.setDrawCircles(true);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                return String.valueOf((int) entry.getY());
            }
        });

        LineData lineData = new LineData(dataSet);

        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);

        SimpleDateFormat sdfAxis = new SimpleDateFormat("dd.MM.", Locale.getDefault());
        Calendar labelDate = (Calendar) currentMonth.clone();

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int day = (int) value;
                if (day >= 0 && day < progressList.size()) {
                    labelDate.set(Calendar.DAY_OF_MONTH, day + 1);
                    return sdfAxis.format(labelDate.getTime());
                }
                return "";
            }
        });

        lineChart.getAxisLeft().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.setDrawGridBackground(false);

        lineChart.setTouchEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setDragEnabled(false);
        lineChart.setHighlightPerTapEnabled(false);

        lineChart.invalidate();
    }
}
