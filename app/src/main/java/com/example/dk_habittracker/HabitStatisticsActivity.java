package com.example.dk_habittracker;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class HabitStatisticsActivity extends AppCompatActivity {

    private TextView textHabitDescription, textHabitType, textHabitGoal, textGoalPeriod, textMeasurement;
    private TextView textViewStreak, textViewLongestStreak, textViewHabitStrength, textViewHabitStrengthPhrase;
    private int habitId;
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
        setContentView(R.layout.activity_habit_statistics);

        TextView textViewHabitTitle = findViewById(R.id.textViewHabitTitle);
        String habitName = getIntent().getStringExtra("habit_name");

        textViewStreak = findViewById(R.id.textViewStreak);
        textViewLongestStreak = findViewById(R.id.textViewLongestStreak);
        textViewHabitStrength = findViewById(R.id.textViewHabitStrength);
        textViewHabitStrengthPhrase = findViewById(R.id.textViewHabitStrengthPhrase);
        viewPager = findViewById(R.id.viewPagerCharts);
        tabIndicator = findViewById(R.id.tabIndicator);
        textHabitType = findViewById(R.id.textHabitType);
        textHabitGoal = findViewById(R.id.textHabitGoal);
        textGoalPeriod = findViewById(R.id.textGoalPeriod);
        textMeasurement = findViewById(R.id.textMeasurement);
        textHabitDescription = findViewById(R.id.textHabitDescription);

        habitId = getIntent().getIntExtra("habit_id", -1);

        if (habitName != null && habitName.length() > 15) {
            habitName = habitName.substring(0, 15) + "...";
        }
        textViewHabitTitle.setText(habitName != null ? habitName : "");

        loadHabitStats();

        setupChartPager();

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

        applyFullScreenMode();
    }

    private void setupChartPager() {
        List<androidx.fragment.app.Fragment> chartFragments = new ArrayList<>();
        chartFragments.add(new HabitLineChartFragment(habitId));
        chartFragments.add(new HabitBarChartFragment(habitId));
        chartFragments.add(new HabitPieChartFragment(habitId));

        HabitChartPagerAdapter adapter = new HabitChartPagerAdapter(this, chartFragments);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabIndicator, viewPager, (tab, position) -> {}).attach();
    }

    private void loadHabitStats() {
        DBHelper dbHelper = new DBHelper(this);
        Habit habit = dbHelper.getHabitById(habitId);
        if (habit == null) return;

        String currentLanguage = getResources().getConfiguration().getLocales().get(0).getLanguage();

        String description = habit.getDescription().trim();
        if (!description.isEmpty()) {
            textHabitDescription.setText(getString(R.string.description_with_value, description));
            textHabitDescription.setVisibility(View.VISIBLE);
        } else {
            textHabitDescription.setVisibility(View.GONE);
        }

        String translatedHabitType = habit.getHabitType();
        switch (translatedHabitType) {
            case "Build":
                translatedHabitType = currentLanguage.equals("sk") ? "Budovať" : "Build"; break;
            case "Quit":
                translatedHabitType = currentLanguage.equals("sk") ? "Prestať" : "Quit"; break;
        }
        textHabitType.setText(getString(R.string.habit_type_with_value, translatedHabitType));

        textHabitGoal.setText(getString(R.string.goal_with_value, habit.getGoal()));

        String translatedGoalPeriod = habit.getGoalPeriod();
        switch (translatedGoalPeriod) {
            case "Daily":
                translatedGoalPeriod = currentLanguage.equals("sk") ? "Denne" : "Daily"; break;
            case "Weekly":
                translatedGoalPeriod = currentLanguage.equals("sk") ? "Týždenne" : "Weekly"; break;
            case "Monthly":
                translatedGoalPeriod = currentLanguage.equals("sk") ? "Mesačne" : "Monthly"; break;
        }
        textGoalPeriod.setText(getString(R.string.goal_period_with_value, translatedGoalPeriod));

        String translatedMeasurement = habit.getMeasurementUnit();
        switch (translatedMeasurement) {
            case "Times": translatedMeasurement = currentLanguage.equals("sk") ? "Krát" : "Times"; break;
            case "Count": translatedMeasurement = currentLanguage.equals("sk") ? "Počet" : "Count"; break;
            case "Steps": translatedMeasurement = currentLanguage.equals("sk") ? "Kroky" : "Steps"; break;
            case "Reps": translatedMeasurement = currentLanguage.equals("sk") ? "Opakovania" : "Reps"; break;
            case "Calories": translatedMeasurement = currentLanguage.equals("sk") ? "Kalórie" : "Calories"; break;
            case "Cups": translatedMeasurement = currentLanguage.equals("sk") ? "Poháre" : "Cups"; break;
            case "sec": translatedMeasurement = currentLanguage.equals("sk") ? "sek" : "sec"; break;
            case "hr": translatedMeasurement = currentLanguage.equals("sk") ? "hoď" : "hr"; break;
            case "min": case "km": case "m": case "ml": case "g": case "mg": break; // Use as-is
            default: break;
        }
        textMeasurement.setText(getString(R.string.measurement_with_value, translatedMeasurement));

        int currentStreak = dbHelper.getCurrentStreak(habitId);

        String unit = "";
        String period = habit.getGoalPeriod();

        if (currentLanguage.equals("sk")) {
            switch (period) {
                case "Daily":
                    if (currentStreak == 1) unit = "Deň";
                    else if (currentStreak >= 2 && currentStreak <= 4) unit = "Dni";
                    else unit = "Dní";
                    textViewStreak.setText(getString(R.string.current_streak_with_unit, currentStreak, unit));
                    break;

                case "Weekly":
                    if (currentStreak == 1) unit = "Týždeň";
                    else if (currentStreak >= 2 && currentStreak <= 4) unit = "Týždne";
                    else unit = "Týždňov";
                    textViewStreak.setText(getString(R.string.current_streak_with_unit, currentStreak, unit));
                    break;

                case "Monthly":
                    if (currentStreak == 1) unit = "Mesiac";
                    else if (currentStreak >= 2 && currentStreak <= 4) unit = "Mesiace";
                    else unit = "Mesiacov";
                    textViewStreak.setText(getString(R.string.current_streak_with_unit, currentStreak, unit));
                    break;
            }

        } else {
            switch (period) {
                case "Daily":
                    unit = currentStreak == 1 ? "Day" : "Days";
                    break;
                case "Weekly":
                    unit = currentStreak == 1 ? "Week" : "Weeks";
                    break;
                case "Monthly":
                    unit = currentStreak == 1 ? "Month" : "Months";
                    break;
            }
            textViewStreak.setText(getString(R.string.current_streak_with_unit, currentStreak, unit));
        }

        int longestStreak = dbHelper.getLongestStreak(habitId);
        String longestUnit = "";

        if (currentLanguage.equals("sk")) {
            switch (period) {
                case "Daily":
                    if (longestStreak == 1) longestUnit = "Deň";
                    else if (longestStreak >= 2 && longestStreak <= 4) longestUnit = "Dni";
                    else longestUnit = "Dní";
                    textViewLongestStreak.setText(getString(R.string.longest_streak_with_unit, longestStreak, longestUnit));
                    break;

                case "Weekly":
                    if (longestStreak == 1) longestUnit = "Týždeň";
                    else if (longestStreak >= 2 && longestStreak <= 4) longestUnit = "Týždne";
                    else longestUnit = "Týždňov";
                    textViewLongestStreak.setText(getString(R.string.longest_streak_with_unit, longestStreak, longestUnit));
                    break;

                case "Monthly":
                    if (longestStreak == 1) longestUnit = "Mesiac";
                    else if (longestStreak >= 2 && longestStreak <= 4) longestUnit = "Mesiace";
                    else longestUnit = "Mesiacov";
                    textViewLongestStreak.setText(getString(R.string.longest_streak_with_unit, longestStreak, longestUnit));
                    break;
            }
        } else {
            switch (period) {
                case "Daily": longestUnit = longestStreak == 1 ? "Day" : "Days"; break;
                case "Weekly": longestUnit = longestStreak == 1 ? "Week" : "Weeks"; break;
                case "Monthly": longestUnit = longestStreak == 1 ? "Month" : "Months"; break;
            }
            textViewLongestStreak.setText(getString(R.string.longest_streak_with_unit, longestStreak, longestUnit));
        }

        String fullStrengthText = dbHelper.getHabitStrengthInfo(habitId, currentLanguage);
        if (fullStrengthText.contains("%")) {
            String[] parts = fullStrengthText.split("%", 2);
            String percentagePart = parts[0].trim() + "%";
            String phrasePart = parts[1].trim();
            textViewHabitStrength.setText(currentLanguage.equals("sk") ? "Sila návyku: " + percentagePart : "Habit Strength: " + percentagePart);
            String emoji = phrasePart.replaceAll(".*?(\\p{So}).*", "$1");
            textViewHabitStrengthPhrase.setText(getString(R.string.habit_strength_phrase_with_emoji, phrasePart, emoji));
        }

        dbHelper.close();
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
}
