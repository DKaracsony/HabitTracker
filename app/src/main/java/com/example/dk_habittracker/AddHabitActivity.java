package com.example.dk_habittracker;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;


import androidx.appcompat.app.AppCompatActivity;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class AddHabitActivity extends AppCompatActivity {


    // UI Elements
    private EditText editTextHabitName, editTextHabitDescription, editTextGoalValue, editTextCustomMeasurement;
    private Spinner spinnerGoalPeriod, spinnerMeasurementUnit;
    private TextView textViewMeasurementPeriod;
    private Button buttonBuildHabit, buttonQuitHabit, buttonSaveHabit;
    private String selectedHabitType = "Build";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);


        // *
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        // *
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
            if (heightDiff < 200) {
                applyFullScreenMode();
            }
        });


        //UI Elements Initialization
        editTextHabitName = findViewById(R.id.editTextHabitName);
        editTextHabitDescription = findViewById(R.id.editTextHabitDescription);
        editTextGoalValue = findViewById(R.id.editTextGoalValue);
        editTextCustomMeasurement = findViewById(R.id.editTextCustomMeasurement);
        spinnerGoalPeriod = findViewById(R.id.spinnerGoalPeriod);
        spinnerMeasurementUnit = findViewById(R.id.spinnerMeasurementUnit);
        textViewMeasurementPeriod = findViewById(R.id.textViewMeasurementPeriod);
        buttonBuildHabit = findViewById(R.id.buttonBuildHabit);
        buttonQuitHabit = findViewById(R.id.buttonQuitHabit);
        buttonSaveHabit = findViewById(R.id.buttonSaveHabit);


        //Habit Type Buttons Colors
        buttonBuildHabit.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        buttonQuitHabit.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));


        //Goal Period Spinner Set up
        ArrayAdapter<CharSequence> goalPeriodAdapter = ArrayAdapter.createFromResource(
                this, R.array.goal_period_options, android.R.layout.simple_spinner_item);
        goalPeriodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoalPeriod.setAdapter(goalPeriodAdapter);


        //Fetch Language Setting
        String currentLanguage = getResources().getConfiguration().locale.getLanguage();
        int measurementArray = currentLanguage.equals("sk") ? R.array.measurement_units_sk : R.array.measurement_units;


        //Measurement Unit Spinner Set Up
        ArrayAdapter<CharSequence> measurementAdapter = ArrayAdapter.createFromResource(
                this, measurementArray, android.R.layout.simple_spinner_item);
        measurementAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMeasurementUnit.setAdapter(measurementAdapter);


        // Handle Habit Type Selection
        buttonBuildHabit.setOnClickListener(v -> {
            selectedHabitType = "Build";
            buttonBuildHabit.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            buttonQuitHabit.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        });

        buttonQuitHabit.setOnClickListener(v -> {
            selectedHabitType = "Quit";
            buttonQuitHabit.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
            buttonBuildHabit.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        });


        // Handle Measurement Type Selection
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
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        // Handle Goal Period Selection
        spinnerGoalPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPeriod = parent.getItemAtPosition(position).toString();
                switch (selectedPeriod) {
                    case "Daily":
                        textViewMeasurementPeriod.setText("/Daily");
                        break;
                    case "Weekly":
                        textViewMeasurementPeriod.setText("/Weekly");
                        break;
                    case "Monthly":
                        textViewMeasurementPeriod.setText("/Monthly");
                        break;
                    case "Denne":
                        textViewMeasurementPeriod.setText("/Denne");
                        break;
                    case "Týždenne":
                        textViewMeasurementPeriod.setText("/Týždenne");
                        break;
                    case "Mesačne":
                        textViewMeasurementPeriod.setText("/Mesačne");
                        break;
                    default:
                        textViewMeasurementPeriod.setText("");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        // Save Habit Button Click
        buttonSaveHabit.setOnClickListener(v -> saveHabit());


        // Handle Footer Navigation
        setupFooterNavigation();


        // Apply full-screen mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


    private void saveHabit() {
        String habitName = editTextHabitName.getText().toString().trim();
        String habitDescription = editTextHabitDescription.getText().toString().trim();
        String goalValueStr = editTextGoalValue.getText().toString().trim();
        String goalPeriodSelected = spinnerGoalPeriod.getSelectedItem().toString();
        String measurementUnitSelected = spinnerMeasurementUnit.getSelectedItem().toString();
        String customMeasurement = editTextCustomMeasurement.getText().toString().trim();

        // Convert goal period to English before saving, if in Slovak
        String goalPeriod;
        switch (goalPeriodSelected) {
            case "Denne":
                goalPeriod = "Daily";
                break;
            case "Týždenne":
                goalPeriod = "Weekly";
                break;
            case "Mesačne":
                goalPeriod = "Monthly";
                break;
            default:
                goalPeriod = goalPeriodSelected;
        }

        // Ensure habit name is not empty
        if (habitName.isEmpty()) {
            Toast.makeText(this, getString(R.string.enter_habit_name), Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate goal value
        int goalValue;
        try {
            goalValue = Integer.parseInt(goalValueStr);
            if (goalValue <= 0 || goalValue > 1000000) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.invalid_goal_value), Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert Slovak measurement unit to English before saving
        String measurementUnit;
        switch (measurementUnitSelected) {
            case "Krát":
                measurementUnit = "Times";
                break;
            case "Počet":
                measurementUnit = "Count";
                break;
            case "Kroky":
                measurementUnit = "Steps";
                break;
            case "Opakovania":
                measurementUnit = "Reps";
                break;
            case "Kalórie":
                measurementUnit = "Calories";
                break;
            case "Poháre":
                measurementUnit = "Cups";
                break;
            case "sek":
                measurementUnit = "sec";
                break;
            case "hoď":
                measurementUnit = "hr";
                break;
            case "Vlastné":
                measurementUnit = "Custom";
                break;
            default:
                measurementUnit = measurementUnitSelected;
        }

        // Handle custom measurement unit
        if (measurementUnit.equals("Custom")) {
            if (!customMeasurement.isEmpty()) {
                measurementUnit = customMeasurement.trim();
            } else {
                Toast.makeText(this, getString(R.string.enter_custom_measurement), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Save habit to database
        DBHelper dbHelper = new DBHelper(this);
        String createdAt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Create a Habit object
        Habit newHabit = new Habit(0, habitName, habitDescription, goalValue, measurementUnit, goalPeriod, selectedHabitType, createdAt);

        // Insert habit
        long habitId = dbHelper.insertHabit(newHabit);

        if (habitId != -1) {
            Toast.makeText(this, getString(R.string.habit_saved_offline), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.habit_save_failed), Toast.LENGTH_SHORT).show();
        }

        // Restart the activity to clear all input fields
        Intent intent = getIntent();
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        startActivity(intent);
    }


    private void setupFooterNavigation() {
        findViewById(R.id.buttonSocial).setOnClickListener(v -> navigateTo(SocialActivity.class));
        findViewById(R.id.buttonStatistics).setOnClickListener(v -> navigateTo(StatisticsActivity.class));
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
    protected void onResume(){
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


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyFullScreenMode();
        }
    }
}
