package com.example.dk_habittracker;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class DailyStatisticsActivity extends AppCompatActivity {

    private TextView textViewEmptyCompleted, textViewEmptyNotCompleted;
    private RecyclerView recyclerViewCompleted, recyclerViewNotCompleted;
    private DBHelper dbHelper;
    private String selectedDate;
    private ViewPager2 viewPager;
    private TabLayout tabIndicator;

    @Override
    protected void attachBaseContext(Context newBase) {
        Configuration overrideConfig = new Configuration(newBase.getResources().getConfiguration());
        overrideConfig.fontScale = 1.0f;
        Context context = newBase.createConfigurationContext(overrideConfig);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_statistics);

        applyFullScreenMode();

        dbHelper = new DBHelper(this);

        selectedDate = getIntent().getStringExtra("selected_date");

        TextView textViewDate = findViewById(R.id.textViewDate);
        textViewDate.setText(String.format(getString(R.string.selected_date), selectedDate));

        recyclerViewCompleted = findViewById(R.id.recyclerViewCompleted);
        recyclerViewNotCompleted = findViewById(R.id.recyclerViewNotCompleted);
        textViewEmptyCompleted = findViewById(R.id.textViewEmptyCompleted);
        textViewEmptyNotCompleted = findViewById(R.id.textViewEmptyNotCompleted);
        viewPager = findViewById(R.id.viewPagerCharts);
        tabIndicator = findViewById(R.id.tabIndicator);

        recyclerViewCompleted.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotCompleted.setLayoutManager(new LinearLayoutManager(this));

        loadHabitsForDate();

        Button socialButton = findViewById(R.id.buttonSocial);
        Button statisticsButton = findViewById(R.id.buttonStatistics);
        Button addHabitButton = findViewById(R.id.buttonAddHabit);
        Button myHabitsButton = findViewById(R.id.buttonMyHabits);
        Button settingsButton = findViewById(R.id.buttonSettings);

        socialButton.setOnClickListener(view -> navigateTo(SocialActivity.class));
        statisticsButton.setOnClickListener(view -> navigateTo(StatisticsActivity.class));
        addHabitButton.setOnClickListener(view -> navigateTo(AddHabitActivity.class));
        myHabitsButton.setOnClickListener(view -> navigateTo(MyHabitsActivity.class));
        settingsButton.setOnClickListener(view -> navigateTo(SettingsActivity.class));
    }

    private void applyFullScreenMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyFullScreenMode();
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void loadHabitsForDate() {
        List<Habit> allHabits = dbHelper.getAllHabits();
        List<Habit> completedHabits = new ArrayList<>();
        List<Habit> notCompletedHabits = new ArrayList<>();

        int completedCount = 0;
        for (Habit habit : allHabits) {
            int progress = dbHelper.getProgressForPeriod(habit.getId(), habit.getGoalPeriod(), selectedDate);

            if (habit.getHabitType().equals("Quit")) {
                if (progress <= habit.getGoal()) {
                    completedHabits.add(habit);
                    completedCount++;
                } else {
                    notCompletedHabits.add(habit);
                }
            } else {
                if (progress >= habit.getGoal()) {
                    completedHabits.add(habit);
                    completedCount++;
                } else {
                    notCompletedHabits.add(habit);
                }
            }
        }

        setupChartPager(completedCount, allHabits.size(), allHabits);

        HabitAdapter completedAdapter = new HabitAdapter(this, completedHabits, selectedDate);
        HabitAdapter notCompletedAdapter = new HabitAdapter(this, notCompletedHabits, selectedDate);

        recyclerViewCompleted.setAdapter(completedAdapter);
        recyclerViewNotCompleted.setAdapter(notCompletedAdapter);

        textViewEmptyCompleted.setVisibility(completedHabits.isEmpty() ? View.VISIBLE : View.GONE);
        textViewEmptyNotCompleted.setVisibility(notCompletedHabits.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupChartPager(int completed, int total, List<Habit> habits) {
        List<androidx.fragment.app.Fragment> chartFragments = new ArrayList<>();
        chartFragments.add(new PieChartFragment(completed, total));
        chartFragments.add(new BarChartFragment(habits, dbHelper, selectedDate));

        ChartPagerAdapter adapter = new ChartPagerAdapter(this, chartFragments);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabIndicator, viewPager, (tab, position) -> {}).attach();

        viewPager.setPageTransformer((page, position) -> {
            float scaleFactor = Math.max(0.85f, 1 - Math.abs(position));
            page.setScaleY(scaleFactor);
        });
    }
}
