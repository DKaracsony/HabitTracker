package com.example.dk_habittracker;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddHabitActivity extends AppCompatActivity {

    private EditText editTextHabitName, editTextHabitDescription, editTextGoalValue, editTextCustomMeasurement;
    private Spinner spinnerGoalPeriod, spinnerMeasurementUnit;
    private TextView textViewMeasurementPeriod;
    private Button buttonBuildHabit;
    private Button buttonQuitHabit;
    private CheckBox checkboxShareHabit;
    private String selectedHabitType = "Build", currentUsername = null, currentUserUid = null;

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
        setContentView(R.layout.activity_add_habit);

        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
            if (heightDiff < 200) {
                applyFullScreenMode();
            }
        });

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
        checkboxShareHabit = findViewById(R.id.checkboxShareHabit);

        buttonBuildHabit.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        buttonQuitHabit.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));

        ArrayAdapter<CharSequence> goalPeriodAdapter = ArrayAdapter.createFromResource(
                this, R.array.goal_period_options, android.R.layout.simple_spinner_item);
        goalPeriodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoalPeriod.setAdapter(goalPeriodAdapter);

        String currentLanguage = getResources().getConfiguration().getLocales().get(0).getLanguage();
        int measurementArray = currentLanguage.equals("sk") ? R.array.measurement_units_sk : R.array.measurement_units;

        ArrayAdapter<CharSequence> measurementAdapter = ArrayAdapter.createFromResource(
                this, measurementArray, android.R.layout.simple_spinner_item);
        measurementAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMeasurementUnit.setAdapter(measurementAdapter);

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
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        buttonSaveHabit.setOnClickListener(v -> saveHabit());

        setupFooterNavigation();

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        updateShareCheckboxVisibility();

        registerReceiver(networkReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void updateShareCheckboxVisibility() {
        boolean isConnected = isInternetAvailable();
        FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (isConnected && user != null) {
            currentUserUid = user.getUid();

            FirebaseFirestore.getInstance().collection("users")
                    .document(currentUserUid)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists() && document.contains("nickname")) {
                            currentUsername = document.getString("nickname");
                            checkboxShareHabit.setVisibility(View.VISIBLE);
                        } else {
                            currentUsername = null;
                            checkboxShareHabit.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        currentUsername = null;
                        checkboxShareHabit.setVisibility(View.GONE);
                    });
        } else {
            checkboxShareHabit.setChecked(false);
            checkboxShareHabit.setVisibility(View.GONE);
            currentUsername = null;
            currentUserUid = null;
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm != null) {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        return false;
    }

    private final android.content.BroadcastReceiver networkReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, android.content.Intent intent) {
            updateShareCheckboxVisibility();
        }
    };

    private void saveHabit() {
        String habitName = editTextHabitName.getText().toString().trim();
        String habitDescription = editTextHabitDescription.getText().toString().trim();
        String goalValueStr = editTextGoalValue.getText().toString().trim();
        String goalPeriodSelected = spinnerGoalPeriod.getSelectedItem().toString();
        String measurementUnitSelected = spinnerMeasurementUnit.getSelectedItem().toString();
        String customMeasurement = editTextCustomMeasurement.getText().toString().trim();

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

        if (habitName.isEmpty()) {
            Toast.makeText(this, getString(R.string.enter_habit_name), Toast.LENGTH_SHORT).show();
            return;
        }

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

        if (measurementUnit.equals("Custom")) {
            if (!customMeasurement.isEmpty()) {
                measurementUnit = customMeasurement.trim();
            } else {
                Toast.makeText(this, getString(R.string.enter_custom_measurement), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String createdAt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        try (DBHelper dbHelper = new DBHelper(this)) {
            Habit newHabit = new Habit(0, habitName, habitDescription, goalValue, measurementUnit, goalPeriod, selectedHabitType, createdAt);
            long habitId = dbHelper.insertHabit(newHabit);

            if (habitId == -1) {
                Toast.makeText(this, getString(R.string.habit_save_failed), Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.habit_save_failed), Toast.LENGTH_SHORT).show();
            Log.e("AddHabitActivity", "Error while saving habit: ", e);
            return;
        }

        if (checkboxShareHabit.getVisibility() == View.VISIBLE &&
                checkboxShareHabit.isChecked() &&
                currentUsername != null &&
                currentUserUid != null &&
                isInternetAvailable()) {

            Map<String, Object> sharedHabit = new HashMap<>();
            sharedHabit.put("habitName", habitName);
            sharedHabit.put("description", habitDescription);
            sharedHabit.put("goalValue", goalValue);
            sharedHabit.put("measurement", measurementUnit);
            sharedHabit.put("goalPeriod", goalPeriod);
            sharedHabit.put("habitType", selectedHabitType);
            sharedHabit.put("sharedByUsername", currentUsername);
            sharedHabit.put("sharedByUID", currentUserUid);
            sharedHabit.put("sharedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

            FirebaseFirestore.getInstance()
                    .collection("sharedHabits")
                    .add(sharedHabit)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, getString(R.string.habit_saved_offline_and_shared), Toast.LENGTH_SHORT).show();
                        restartActivity();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, getString(R.string.habit_saved_but_share_failed), Toast.LENGTH_LONG).show();
                        restartActivity();
                    });

        } else {
            Toast.makeText(this, getString(R.string.habit_saved_offline), Toast.LENGTH_SHORT).show();
            restartActivity();
        }
    }

    private void restartActivity() {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkReceiver);
    }

}
