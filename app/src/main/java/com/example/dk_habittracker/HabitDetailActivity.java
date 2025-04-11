package com.example.dk_habittracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class HabitDetailActivity extends AppCompatActivity {

    private TextView textCurrentProgress;
    private DBHelper dbHelper;
    private Habit habit;
    private int currentProgress;
    private String selectedDate;
    private EditText editTextManualProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_detail);

        dbHelper = new DBHelper(this);

        int habitId = getIntent().getIntExtra("habit_id", -1);
        selectedDate = getIntent().getStringExtra("selected_date");

        if (habitId == -1 || selectedDate == null) {
            Toast.makeText(this, "Invalid habit data.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        habit = dbHelper.getHabitById(habitId);
        if (habit == null) {
            Toast.makeText(this, "Habit not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView textHabitName = findViewById(R.id.textHeaderHabitName);
        TextView textHabitDescription = findViewById(R.id.textHabitDescription);
        TextView textHabitGoal = findViewById(R.id.textHabitGoal);
        TextView textHabitType = findViewById(R.id.textHabitType);
        TextView textGoalPeriod = findViewById(R.id.textGoalPeriod);
        TextView textMeasurement = findViewById(R.id.textMeasurement);

        textCurrentProgress = findViewById(R.id.textCurrentProgress);

        Button buttonIncrease = findViewById(R.id.buttonIncrease);
        Button buttonDecrease = findViewById(R.id.buttonDecrease);
        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonDelete = findViewById(R.id.buttonDelete);
        Button buttonCancel = findViewById(R.id.buttonCancel);
        Button buttonEditHabit = findViewById(R.id.buttonEditHabit);
        Button buttonAddManualProgress = findViewById(R.id.buttonAddManualProgress);

        editTextManualProgress = findViewById(R.id.editTextManualProgress);
        editTextManualProgress.setFocusable(false);
        editTextManualProgress.setFocusableInTouchMode(true);

        editTextManualProgress.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                applyFullScreenMode();
            }
        });

        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
            if (heightDiff < 200) {
                applyFullScreenMode();
            }
        });

        rootView.setFocusable(true);
        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();

        buttonSave.setText(getString(R.string.save));
        buttonDelete.setText(getString(R.string.delete));
        buttonCancel.setText(getString(R.string.cancel));

        String currentLanguage = getResources().getConfiguration().getLocales().get(0).getLanguage();

        String habitNameHeader = habit.getName();
        if (habitNameHeader.length() > 15) {
            habitNameHeader = habitNameHeader.substring(0, 15) + "...";
        }
        textHabitName.setText(habitNameHeader);

        buttonAddManualProgress.setOnClickListener(v -> {
            String inputText = editTextManualProgress.getText().toString().trim();

            if (!inputText.isEmpty()) {
                try {
                    int newValue = Integer.parseInt(inputText);

                    if (newValue < 0 || newValue > 100000) {
                        Toast.makeText(HabitDetailActivity.this, getString(R.string.enter_reasonable_value), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentProgress = newValue;
                    textCurrentProgress.setText(getString(R.string.progress_with_value, currentProgress));
                    editTextManualProgress.setText("");

                } catch (NumberFormatException e) {
                    Toast.makeText(HabitDetailActivity.this, getString(R.string.enter_reasonable_value), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(HabitDetailActivity.this, getString(R.string.enter_progress_value), Toast.LENGTH_SHORT).show();
            }
        });

        buttonEditHabit.setOnClickListener(v -> {
            Intent intent = new Intent(HabitDetailActivity.this, EditHabitActivity.class);
            intent.putExtra("habit_id", habit.getId());
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        if (habit.getDescription().trim().isEmpty()) {
            textHabitDescription.setVisibility(View.GONE);
        } else {
            textHabitDescription.setText(getString(R.string.description_with_value, habit.getDescription()));
            textHabitDescription.setVisibility(View.VISIBLE);
        }

        textHabitDescription.setText(getString(R.string.description_with_value, habit.getDescription()));
        textHabitGoal.setText(getString(R.string.goal_with_value, habit.getGoal()));
        textHabitType.setText(getString(R.string.habit_type_with_value, habit.getHabitType()));

        String translatedGoalPeriod;
        switch (habit.getGoalPeriod()) {
            case "Daily":
                translatedGoalPeriod = currentLanguage.equals("sk") ? "Denne" : "Daily";
                break;
            case "Weekly":
                translatedGoalPeriod = currentLanguage.equals("sk") ? "Týždenne" : "Weekly";
                break;
            case "Monthly":
                translatedGoalPeriod = currentLanguage.equals("sk") ? "Mesačne" : "Monthly";
                break;
            default:
                translatedGoalPeriod = habit.getGoalPeriod();
        }

        textGoalPeriod.setText(getString(R.string.goal_period_with_value, translatedGoalPeriod));

        String translatedHabitType;
        switch (habit.getHabitType()) {
            case "Build":
                translatedHabitType = currentLanguage.equals("sk") ? "Budovať" : "Build";
                break;
            case "Quit":
                translatedHabitType = currentLanguage.equals("sk") ? "Prestať" : "Quit";
                break;
            default:
                translatedHabitType = habit.getHabitType();
        }

        textHabitType.setText(getString(R.string.habit_type_with_value, translatedHabitType));

        String translatedMeasurementUnit;
        switch (habit.getMeasurementUnit()) {
            case "Times":
                translatedMeasurementUnit = currentLanguage.equals("sk") ? "Krát" : "Times";
                break;
            case "Count":
                translatedMeasurementUnit = currentLanguage.equals("sk") ? "Počet" : "Count";
                break;
            case "Steps":
                translatedMeasurementUnit = currentLanguage.equals("sk") ? "Kroky" : "Steps";
                break;
            case "Reps":
                translatedMeasurementUnit = currentLanguage.equals("sk") ? "Opakovania" : "Reps";
                break;
            case "Calories":
                translatedMeasurementUnit = currentLanguage.equals("sk") ? "Kalórie" : "Calories";
                break;
            case "Cups":
                translatedMeasurementUnit = currentLanguage.equals("sk") ? "Poháre" : "Cups";
                break;
            case "m":
                translatedMeasurementUnit = "m";
                break;
            case "km":
                translatedMeasurementUnit = "km";
                break;
            case "sec":
                translatedMeasurementUnit = currentLanguage.equals("sk") ? "sek" : "sec";
                break;
            case "min":
                translatedMeasurementUnit = "min";
                break;
            case "hr":
                translatedMeasurementUnit = currentLanguage.equals("sk") ? "hoď" : "hr";
                break;
            case "ml":
                translatedMeasurementUnit = "ml";
                break;
            case "g":
                translatedMeasurementUnit = "g";
                break;
            case "mg":
                translatedMeasurementUnit = "mg";
                break;
            default:
                translatedMeasurementUnit = habit.getMeasurementUnit();
        }

        textMeasurement.setText(getString(R.string.measurement_with_value, translatedMeasurementUnit));

        applyFullScreenMode();

        currentProgress = dbHelper.getProgressForExactDate(habit.getId(), selectedDate);
        textCurrentProgress.setText(getString(R.string.progress_with_value, currentProgress));

        buttonIncrease.setOnClickListener(v -> {
            currentProgress++;
            textCurrentProgress.setText(getString(R.string.progress_with_value, currentProgress));
        });

        buttonDecrease.setOnClickListener(v -> {
            if (currentProgress > 0) {
                currentProgress--;
                textCurrentProgress.setText(getString(R.string.progress_with_value, currentProgress));
            }
        });

        buttonSave.setOnClickListener(v -> {
            dbHelper.updateHabitProgress(habit.getId(), selectedDate, currentProgress);
            Toast.makeText(this, getString(R.string.progress_updated), Toast.LENGTH_SHORT).show();
            returnToMyHabits();
        });

        TextView textProgressDateIndicator = findViewById(R.id.textProgressDateIndicator);

        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date parsedDate = inputFormat.parse(selectedDate);

            if (parsedDate != null) {
                java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("d. MMMM yyyy", getResources().getConfiguration().getLocales().get(0));
                String displayDate = outputFormat.format(parsedDate);
                textProgressDateIndicator.setText(getString(R.string.progress_for_date_with_value, displayDate));
            } else {
                textProgressDateIndicator.setText(getString(R.string.progress_for_date_with_value, selectedDate));
            }
        } catch (Exception e) {
            textProgressDateIndicator.setText(getString(R.string.progress_for_date_with_value, selectedDate));
        }

        buttonDelete.setOnClickListener(v -> new AlertDialog.Builder(HabitDetailActivity.this)
                .setTitle(getString(R.string.delete_habit_title))
                .setMessage(getString(R.string.delete_habit_message))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    dbHelper.deleteHabit(habit.getId());
                    Toast.makeText(HabitDetailActivity.this, getString(R.string.habit_deleted), Toast.LENGTH_SHORT).show();
                    returnToMyHabits();
                })
                .setNegativeButton(getString(R.string.no), null)
                .show());

        buttonCancel.setOnClickListener(v -> returnToMyHabits());
        setupFooterNavigation();
    }

    private void applyFullScreenMode()
    {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    private void returnToMyHabits() {
        Intent intent = new Intent(HabitDetailActivity.this, MyHabitsActivity.class);
        intent.putExtra("selected_date", selectedDate);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void setupFooterNavigation() {
        findViewById(R.id.buttonSocial).setOnClickListener(v -> navigateTo(SocialActivity.class));
        findViewById(R.id.buttonStatistics).setOnClickListener(v -> navigateTo(StatisticsActivity.class));
        findViewById(R.id.buttonAddHabit).setOnClickListener(v -> navigateTo(AddHabitActivity.class));
        findViewById(R.id.buttonMyHabits).setOnClickListener(v -> navigateTo(MyHabitsActivity.class));
        findViewById(R.id.buttonSettings).setOnClickListener(v -> navigateTo(SettingsActivity.class));
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
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}