package com.example.dk_habittracker;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private DBHelper dbHelper;
    private Calendar today, selectedCalendar, monthEnd;

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
        setContentView(R.layout.activity_statistics);

        dbHelper = new DBHelper(this);

        calendarView = findViewById(R.id.calendarView);
        today = Calendar.getInstance();
        selectedCalendar = Calendar.getInstance();
        monthEnd = getMonthEnd();

        setCalendarLimits();

        disableFutureDates();

        RecyclerView recyclerViewFollowedHabits = findViewById(R.id.recyclerViewFollowedHabits);
        recyclerViewFollowedHabits.setLayoutManager(new LinearLayoutManager(this));

        loadAllHabits();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedCalendar.set(year, month, dayOfMonth);

            if (selectedCalendar.after(today)) {
                return;
            }

            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);

            Intent intent = new Intent(StatisticsActivity.this, DailyStatisticsActivity.class);
            intent.putExtra("selected_date", selectedDate);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        Button socialButton = findViewById(R.id.buttonSocial);
        Button statisticsButton = findViewById(R.id.buttonStatistics);
        Button addHabitButton = findViewById(R.id.buttonAddHabit);
        Button myHabitsButton = findViewById(R.id.buttonMyHabits);
        Button settingsButton = findViewById(R.id.buttonSettings);

        socialButton.setOnClickListener(view -> navigateTo(SocialActivity.class));
        addHabitButton.setOnClickListener(view -> navigateTo(AddHabitActivity.class));
        statisticsButton.setOnClickListener(view -> navigateTo(StatisticsActivity.class));
        myHabitsButton.setOnClickListener(view -> navigateTo(MyHabitsActivity.class));
        settingsButton.setOnClickListener(view -> navigateTo(SettingsActivity.class));

        applyFullScreenMode();

        NestedScrollView scrollView = findViewById(R.id.scrollViewStatistics);
        scrollView.postDelayed(() -> {
            scrollView.fullScroll(View.FOCUS_UP);
            scrollView.smoothScrollTo(0, 0);
        }, 100);
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
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

    private void loadAllHabits() {
        List<Habit> habitList = dbHelper.getAllHabits();
        TextView textViewNoHabitsMessage = findViewById(R.id.textViewNoHabitsMessage);
        RecyclerView recyclerViewFollowedHabits = findViewById(R.id.recyclerViewFollowedHabits);

        if (habitList.isEmpty()) {
            textViewNoHabitsMessage.setVisibility(View.VISIBLE);
            recyclerViewFollowedHabits.setVisibility(View.GONE);
        } else {
            textViewNoHabitsMessage.setVisibility(View.GONE);
            recyclerViewFollowedHabits.setVisibility(View.VISIBLE);

            FollowedHabitAdapter followedHabitAdapter = new FollowedHabitAdapter(this, habitList);
            recyclerViewFollowedHabits.setAdapter(followedHabitAdapter);

        }
    }

    private void setCalendarLimits() {
        calendarView.setMinDate(getEarliestAvailableDate().getTimeInMillis());
        calendarView.setMaxDate(monthEnd.getTimeInMillis());
    }

    private void disableFutureDates() {
        calendarView.setMaxDate(today.getTimeInMillis());
    }

    private Calendar getEarliestAvailableDate() {
        Calendar earliest = Calendar.getInstance();
        earliest.set(Calendar.YEAR, 2000);
        earliest.set(Calendar.MONTH, Calendar.JANUARY);
        return earliest;
    }

    private Calendar getMonthEnd() {
        Calendar end = Calendar.getInstance();
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
        return end;
    }

}
