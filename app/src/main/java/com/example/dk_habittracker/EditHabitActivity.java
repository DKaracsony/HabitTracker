package com.example.dk_habittracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class EditHabitActivity extends AppCompatActivity {

    private EditText editTextHabitName, editTextHabitDescription, editTextGoalValue, editTextCustomMeasurement;
    private Spinner spinnerGoalPeriod, spinnerMeasurementUnit;
    private TextView textViewMeasurementPeriod;
    private Button buttonBuildHabit, buttonQuitHabit;
    private String selectedHabitType;
    private DBHelper dbHelper;
    private Habit habit;
    private int habitId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_habit);

        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
            if (heightDiff < 200) {
                applyFullScreenMode();
            }
        });

        habitId = getIntent().getIntExtra("habit_id", -1);

        dbHelper = new DBHelper(this);

        editTextHabitName = findViewById(R.id.editTextHabitName);
        editTextHabitDescription = findViewById(R.id.editTextHabitDescription);
        editTextGoalValue = findViewById(R.id.editTextGoalValue);
        editTextCustomMeasurement = findViewById(R.id.editTextCustomMeasurement);
        spinnerGoalPeriod = findViewById(R.id.spinnerGoalPeriod);
        spinnerMeasurementUnit = findViewById(R.id.spinnerMeasurementUnit);
        textViewMeasurementPeriod = findViewById(R.id.textViewMeasurementPeriod);
        buttonBuildHabit = findViewById(R.id.buttonBuildHabit);
        buttonQuitHabit = findViewById(R.id.buttonQuitHabit);
        Button buttonSaveHabit = findViewById(R.id.buttonSaveHabit);
        Button buttonCancelEditing = findViewById(R.id.buttonCancelEditing);

        buttonSaveHabit.setOnClickListener(v -> saveHabit());
        buttonCancelEditing.setOnClickListener(v -> {
            Intent intent = new Intent(EditHabitActivity.this, MyHabitsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        spinnerMeasurementUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedUnit = parent.getItemAtPosition(position).toString();

                if (selectedUnit.equals("Custom") || selectedUnit.equals("Vlastné")) {
                    editTextCustomMeasurement.setVisibility(View.VISIBLE);
                } else {
                    editTextCustomMeasurement.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        setupGoalPeriodSynchronization();

        loadHabitData();

        TextView textHeaderHabitName = findViewById(R.id.textHeaderHabitName);

        String habitNameHeader = habit.getName();
        if (habitNameHeader.length() > 15) {
            habitNameHeader = habitNameHeader.substring(0, 15) + "...";
        }

        textHeaderHabitName.setText(habitNameHeader);

        setupFooterNavigation();

        applyFullScreenMode();
    }

    @SuppressWarnings("unused") // Itthagyni, ha lesz idő változtatni a return logikát!!!
    private void returnToHabitDetail() {
        Intent intent = new Intent();
        intent.putExtra("habit_id", habitId);
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void setupGoalPeriodSynchronization() {
        spinnerGoalPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPeriod = parent.getItemAtPosition(position).toString();
                switch (selectedPeriod) {
                    case "Daily":
                    case "Denne":
                        textViewMeasurementPeriod.setText(getString(R.string.period_daily));
                        break;
                    case "Weekly":
                    case "Týždenne":
                        textViewMeasurementPeriod.setText(getString(R.string.period_weekly));
                        break;
                    case "Monthly":
                    case "Mesačne":
                        textViewMeasurementPeriod.setText(getString(R.string.period_monthly));
                        break;
                    default:
                        textViewMeasurementPeriod.setText("");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadHabitData() {
        if (habitId == -1) {
            Toast.makeText(this, getString(R.string.error_loading_habit), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        habit = dbHelper.getHabitById(habitId);
        if (habit == null) {
            Toast.makeText(this, getString(R.string.habit_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editTextHabitName.setText(habit.getName());
        editTextHabitDescription.setText(habit.getDescription());
        editTextGoalValue.setText(String.valueOf(habit.getGoal()));
        selectedHabitType = habit.getHabitType();

        if (selectedHabitType.equals("Build")) {
            buttonBuildHabit.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            buttonQuitHabit.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        } else {
            buttonQuitHabit.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            buttonBuildHabit.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        }

        buttonBuildHabit.setOnClickListener(v -> {
            selectedHabitType = "Build";
            buttonBuildHabit.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            buttonQuitHabit.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        });

        buttonQuitHabit.setOnClickListener(v -> {
            selectedHabitType = "Quit";
            buttonQuitHabit.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            buttonBuildHabit.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        });


        ArrayAdapter<CharSequence> periodAdapter = ArrayAdapter.createFromResource(this,
                R.array.goal_period_options, android.R.layout.simple_spinner_item);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoalPeriod.setAdapter(periodAdapter);

        String currentLanguage = getResources().getConfiguration().getLocales().get(0).getLanguage();
        String localizedGoalPeriod = habit.getGoalPeriod();

        if (currentLanguage.equals("sk")) {
            switch (habit.getGoalPeriod()) {
                case "Daily": localizedGoalPeriod = "Denne"; break;
                case "Weekly": localizedGoalPeriod = "Týždenne"; break;
                case "Monthly": localizedGoalPeriod = "Mesačne"; break;
            }
        }

        int periodPosition = periodAdapter.getPosition(localizedGoalPeriod);
        spinnerGoalPeriod.setSelection(periodPosition);

        int measurementArray = currentLanguage.equals("sk") ? R.array.measurement_units_sk : R.array.measurement_units;
        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                this, measurementArray, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMeasurementUnit.setAdapter(unitAdapter);

        String localizedMeasurementUnit = habit.getMeasurementUnit();
        if (currentLanguage.equals("sk")) {
            switch (habit.getMeasurementUnit()) {
                case "Times": localizedMeasurementUnit = "Krát"; break;
                case "Count": localizedMeasurementUnit = "Počet"; break;
                case "Steps": localizedMeasurementUnit = "Kroky"; break;
                case "Reps": localizedMeasurementUnit = "Opakovania"; break;
                case "Calories": localizedMeasurementUnit = "Kalórie"; break;
                case "Cups": localizedMeasurementUnit = "Poháre"; break;
                case "sec": localizedMeasurementUnit = "sek"; break;
                case "hr": localizedMeasurementUnit = "hoď"; break;
            }
        }

        int unitPosition = unitAdapter.getPosition(localizedMeasurementUnit);
        if (unitPosition == -1) {
            localizedMeasurementUnit = currentLanguage.equals("sk") ? "Vlastné" : "Custom";
            editTextCustomMeasurement.setText(habit.getMeasurementUnit());
            editTextCustomMeasurement.setVisibility(View.VISIBLE);
        } else {
            editTextCustomMeasurement.setVisibility(View.GONE);
        }

        spinnerMeasurementUnit.setSelection(unitAdapter.getPosition(localizedMeasurementUnit));

        setupGoalPeriodSynchronization();
    }

    private void saveHabit() {
        String habitName = editTextHabitName.getText().toString().trim();
        String habitDescription = editTextHabitDescription.getText().toString().trim();
        String goalValueText = editTextGoalValue.getText().toString().trim();
        String goalPeriodSelected = spinnerGoalPeriod.getSelectedItem().toString();
        String measurementUnitSelected = spinnerMeasurementUnit.getSelectedItem().toString();
        String customMeasurement = editTextCustomMeasurement.getText().toString().trim();

        if (habitName.isEmpty() || goalValueText.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_required_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        int goalValue;
        try {
            goalValue = Integer.parseInt(goalValueText);
            if (goalValue <= 0 || goalValue > 1000000) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.invalid_goal_value), Toast.LENGTH_SHORT).show();
            return;
        }

        final String goalPeriod;
        switch (goalPeriodSelected) {
            case "Denne": goalPeriod = "Daily"; break;
            case "Týždenne": goalPeriod = "Weekly"; break;
            case "Mesačne": goalPeriod = "Monthly"; break;
            default: goalPeriod = goalPeriodSelected;
        }

        final String measurementUnit;
        switch (measurementUnitSelected) {
            case "Krát": measurementUnit = "Times"; break;
            case "Počet": measurementUnit = "Count"; break;
            case "Kroky": measurementUnit = "Steps"; break;
            case "Opakovania": measurementUnit = "Reps"; break;
            case "Kalórie": measurementUnit = "Calories"; break;
            case "Poháre": measurementUnit = "Cups"; break;
            case "sek": measurementUnit = "sec"; break;
            case "hoď": measurementUnit = "hr"; break;
            case "Vlastné": measurementUnit = "Custom"; break;
            default: measurementUnit = measurementUnitSelected;
        }

        final String finalMeasurementUnit;
        if (measurementUnit.equals("Custom")) {
            if (!customMeasurement.isEmpty()) {
                finalMeasurementUnit = customMeasurement.trim();
            } else {
                Toast.makeText(this, getString(R.string.enter_custom_measurement), Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            finalMeasurementUnit = measurementUnit;
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.modify_habit_title))
                .setMessage(getString(R.string.modify_habit_message))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    dbHelper.deleteHabitProgress(habitId);

                    // Update habit data
                    habit.setHabitType(selectedHabitType);
                    habit.setName(habitName);
                    habit.setDescription(habitDescription);
                    habit.setGoal(goalValue);
                    habit.setGoalPeriod(goalPeriod);
                    habit.setMeasurementUnit(finalMeasurementUnit);

                    dbHelper.updateHabit(habit);

                    Toast.makeText(this, getString(R.string.habit_updated), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(EditHabitActivity.this, MyHabitsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void setupFooterNavigation() {
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
    }
}