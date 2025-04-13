package com.example.dk_habittracker;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyHabitsActivity extends AppCompatActivity {

    private TextView textViewSelectedDate;
    private Calendar calendar;
    private RecyclerView recyclerViewNotCompleted, recyclerViewCompleted;
    private DBHelper dbHelper;

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
        setContentView(R.layout.activity_my_habits);
        applyFullScreenMode();
        dbHelper = new DBHelper(this);

        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        ImageButton buttonPreviousDate = findViewById(R.id.buttonPreviousDate);
        ImageButton buttonNextDate = findViewById(R.id.buttonNextDate);

        recyclerViewNotCompleted = findViewById(R.id.recyclerViewNotCompleted);
        recyclerViewCompleted = findViewById(R.id.recyclerViewCompleted);

        recyclerViewNotCompleted.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCompleted.setLayoutManager(new LinearLayoutManager(this));

        calendar = Calendar.getInstance();

        String passedDate = getIntent().getStringExtra("selected_date");
        if (passedDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                java.util.Date parsedDate = dateFormat.parse(passedDate);
                if (parsedDate != null) {
                    calendar.setTime(parsedDate);
                } else {
                    android.util.Log.w("MyHabitsActivity", "Parsed date is null for: " + passedDate);
                }
            } catch (Exception e) {
                android.util.Log.e("MyHabitsActivity", "Failed to parse passed date: " + passedDate, e);
            }
        }

        updateDateDisplay();

        buttonPreviousDate.setOnClickListener(v -> changeDate(-1));
        buttonNextDate.setOnClickListener(v -> changeDate(1));

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

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        textViewSelectedDate.setText(dateFormat.format(calendar.getTime()));

        ImageButton buttonNextDate = findViewById(R.id.buttonNextDate);
        ImageButton buttonPreviousDate = findViewById(R.id.buttonPreviousDate);
        Calendar today = Calendar.getInstance();

        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            buttonNextDate.setEnabled(false);
            buttonNextDate.setAlpha(0.5f);
        } else {
            buttonNextDate.setEnabled(true);
            buttonNextDate.setAlpha(1.0f);
        }

        boolean disablePrevious = false;
        List<Habit> allHabits = dbHelper.getAllHabits();
        if (!allHabits.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date firstCreated = sdf.parse(allHabits.get(0).getCreatedAt());
                if (firstCreated == null) {
                    Log.e("MyHabitsActivity", "First habit has null creation date");
                    return;
                }

                Calendar earliestCreation = Calendar.getInstance();
                earliestCreation.setTime(firstCreated);

                for (Habit habit : allHabits) {
                    Date habitDate = sdf.parse(habit.getCreatedAt());
                    if (habitDate == null) {
                        Log.w("MyHabitsActivity", "Skipped habit with null creation date: " + habit.getName());
                        continue;
                    }

                    Calendar habitCreated = Calendar.getInstance();
                    habitCreated.setTime(habitDate);

                    if (habitCreated.before(earliestCreation)) {
                        earliestCreation = (Calendar) habitCreated.clone();
                    }
                }

                Calendar calMinusOne = (Calendar) calendar.clone();
                calMinusOne.add(Calendar.DAY_OF_MONTH, -1);
                disablePrevious = calMinusOne.before(earliestCreation);

            } catch (Exception e) {
                Log.e("MyHabitsActivity", "Error while checking earliest creation date", e);
            }
        }

        if (disablePrevious) {
            buttonPreviousDate.setEnabled(false);
            buttonPreviousDate.setAlpha(0.5f);
        } else {
            buttonPreviousDate.setEnabled(true);
            buttonPreviousDate.setAlpha(1.0f);
        }

        loadHabitsForDate();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyFullScreenMode();
        }
    }

    private void changeDate(int days) {
        Calendar newCal = (Calendar) calendar.clone();
        newCal.add(Calendar.DAY_OF_MONTH, days);

        List<Habit> allHabits = dbHelper.getAllHabits();
        if (!allHabits.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date firstCreated = sdf.parse(allHabits.get(0).getCreatedAt());
                if (firstCreated == null) {
                    Log.e("MyHabitsActivity", "First habit has null creation date");
                    return;
                }

                Calendar earliestCreation = Calendar.getInstance();
                earliestCreation.setTime(firstCreated);

                for (Habit habit : allHabits) {
                    Date habitDate = sdf.parse(habit.getCreatedAt());
                    if (habitDate == null) {
                        Log.w("MyHabitsActivity", "Skipped habit with null creation date: " + habit.getName());
                        continue;
                    }

                    Calendar habitCreated = Calendar.getInstance();
                    habitCreated.setTime(habitDate);
                    if (habitCreated.before(earliestCreation)) {
                        earliestCreation = (Calendar) habitCreated.clone();
                    }
                }

                if (newCal.before(earliestCreation)) {
                    return;
                }

            } catch (Exception e) {
                Log.e("MyHabitsActivity", "Error while parsing habit creation dates", e);
            }
        }

        calendar = newCal;
        updateDateDisplay();
    }

    private void loadHabitsForDate() {
        String selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        List<Habit> notCompletedHabits = new ArrayList<>();
        List<Habit> completedHabits = new ArrayList<>();
        List<Habit> allHabits = dbHelper.getAllHabits();

        for (Habit habit : allHabits) {
            int progress = dbHelper.getProgressForPeriod(habit.getId(), habit.getGoalPeriod(), selectedDate);

            if (habit.getHabitType().equals("Quit")) {
                if (progress <= habit.getGoal()) {
                    completedHabits.add(habit);
                } else {
                    notCompletedHabits.add(habit);
                }
            } else {
                if (progress >= habit.getGoal()) {
                    completedHabits.add(habit);
                } else {
                    notCompletedHabits.add(habit);
                }
            }
        }

        TextView textViewEmptyNotCompleted = findViewById(R.id.textViewEmptyNotCompleted);
        TextView textViewEmptyCompleted = findViewById(R.id.textViewEmptyCompleted);

        textViewEmptyNotCompleted.setVisibility(notCompletedHabits.isEmpty() ? View.VISIBLE : View.GONE);
        textViewEmptyCompleted.setVisibility(completedHabits.isEmpty() ? View.VISIBLE : View.GONE);

        HabitAdapter notCompletedAdapter = new HabitAdapter(this, notCompletedHabits, selectedDate);
        HabitAdapter completedAdapter = new HabitAdapter(this, completedHabits, selectedDate);

        recyclerViewNotCompleted.setAdapter(notCompletedAdapter);
        recyclerViewCompleted.setAdapter(completedAdapter);
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyFullScreenMode();
    }

    private void applyFullScreenMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        new android.os.Handler().postDelayed(() -> getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN), 500);
    }
}