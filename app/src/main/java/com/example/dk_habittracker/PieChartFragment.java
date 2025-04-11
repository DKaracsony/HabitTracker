package com.example.dk_habittracker;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class PieChartFragment extends Fragment {

    private final int completed, total;
    public PieChartFragment(int completed, int total) {
        this.completed = completed;
        this.total = total;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pie_chart, container, false);
        PieChart pieChart = view.findViewById(R.id.pieChart);

        setupPieChart(pieChart);
        return view;
    }

    private void setupPieChart(PieChart pieChart) {
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.parseColor("#FFB16C"));
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(false);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(completed, ""));
        entries.add(new PieEntry(total - completed, ""));
        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setColors(Color.GREEN, Color.RED);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        pieChart.setData(data);
        pieChart.invalidate();
        pieChart.animateY(1000);

        Legend legend = pieChart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        legend.setTextSize(14f);
        legend.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        legend.setCustom(new ArrayList<>() {{
            add(new LegendEntry(getString(R.string.completed), Legend.LegendForm.CIRCLE, 10f, 2f, null, Color.GREEN));
            add(new LegendEntry(getString(R.string.not_completed), Legend.LegendForm.CIRCLE, 10f, 2f, null, Color.RED));
        }});
    }
}
