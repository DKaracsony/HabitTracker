package com.example.dk_habittracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private Calendar today, selectedCalendar, monthStart, monthEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Initialize CalendarView
        calendarView = findViewById(R.id.calendarView);
        today = Calendar.getInstance();
        selectedCalendar = Calendar.getInstance();
        monthStart = getMonthStart();
        monthEnd = getMonthEnd();

        //Allow navigation but restrict it beyond the current month
        setCalendarLimits();

        //Highlight current date in light orange
        highlightCurrentDate();

        //Disable future dates within the month
        disableFutureDates();

        // Listen for date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedCalendar.set(year, month, dayOfMonth);

            // Prevent selecting future dates
            if (selectedCalendar.after(today)) {
                return; // Ignore clicks on future dates
            }

            // Convert selected date to formatted string
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);

            // Open MyHabitsActivity with the selected date
            Intent intent = new Intent(StatisticsActivity.this, MyHabitsActivity.class);
            intent.putExtra("selected_date", selectedDate);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        // Handle Footer Button Clicks
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

        // Apply full-screen mode
        applyFullScreenMode();
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

    //Set the calendar navigation limits
    private void setCalendarLimits() {
        calendarView.setMinDate(getEarliestAvailableDate().getTimeInMillis()); // Allow full past navigation
        calendarView.setMaxDate(monthEnd.getTimeInMillis()); // Restrict beyond the current month
    }

    //Disable future dates in the current month
    private void disableFutureDates() {
        calendarView.setMaxDate(today.getTimeInMillis()); // Users can only select past and current days
    }

    //Get the first available date for navigation (allows past months)
    private Calendar getEarliestAvailableDate() {
        Calendar earliest = Calendar.getInstance();
        earliest.set(Calendar.YEAR, 2000);
        earliest.set(Calendar.MONTH, Calendar.JANUARY);
        return earliest;
    }

    //Get the start of the current month
    private Calendar getMonthStart() {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_MONTH, 1);
        return start;
    }

    //Get the last day of the current month
    private Calendar getMonthEnd() {
        Calendar end = Calendar.getInstance();
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
        return end;
    }

    //Highlight the current date in light orange
    private void highlightCurrentDate() {
        calendarView.setFocusedMonthDateColor(Color.parseColor("#FFA500")); // Light Orange - Not Working Fix!!
    }
}
