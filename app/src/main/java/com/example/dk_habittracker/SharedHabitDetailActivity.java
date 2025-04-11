package com.example.dk_habittracker;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SharedHabitDetailActivity extends AppCompatActivity {

    private SharedHabit habit;
    private boolean isMyHabit;
    private DBHelper dbHelper;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_habit_detail);
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (!isConnected() || mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "No access. Returning...", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DBHelper(this);
        firestore = FirebaseFirestore.getInstance();

        habit = (SharedHabit) getIntent().getSerializableExtra("habit");
        isMyHabit = getIntent().getBooleanExtra("isMyHabit", false);

        setupUI();
    }

    private void setupUI() {
        EditText nameField = findViewById(R.id.editTextHabitName);
        EditText descField = findViewById(R.id.editTextHabitDescription);
        EditText goalField = findViewById(R.id.editTextGoalValue);
        EditText customMeasurementField = findViewById(R.id.editTextCustomMeasurement);
        Spinner spinnerPeriod = findViewById(R.id.spinnerGoalPeriod);
        Spinner spinnerUnit = findViewById(R.id.spinnerMeasurementUnit);
        TextView periodLabel = findViewById(R.id.textViewMeasurementPeriod);
        Button buttonBuild = findViewById(R.id.buttonBuildHabit);
        Button buttonQuit = findViewById(R.id.buttonQuitHabit);

        Button buttonCancel = findViewById(R.id.buttonCancel);
        Button buttonAction = findViewById(R.id.buttonAction);

        nameField.setText(habit.habitName);
        if (habit.description != null && !habit.description.trim().isEmpty()) {
            descField.setText(habit.description);
        } else {
            descField.setText("");
        }
        goalField.setText(String.valueOf(habit.goalValue));

        if (habit.habitType.equals("Build")) {
            buttonBuild.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            buttonQuit.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        } else {
            buttonQuit.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            buttonBuild.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        }

        nameField.setEnabled(false);
        descField.setEnabled(false);
        goalField.setEnabled(false);
        customMeasurementField.setEnabled(false);
        spinnerPeriod.setEnabled(false);
        spinnerUnit.setEnabled(false);
        buttonBuild.setEnabled(false);
        buttonQuit.setEnabled(false);

        ArrayAdapter<CharSequence> periodAdapter = ArrayAdapter.createFromResource(this,
                R.array.goal_period_options, android.R.layout.simple_spinner_item);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(periodAdapter);

        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(this,
                Locale.getDefault().getLanguage().equals("sk") ?
                        R.array.measurement_units_sk : R.array.measurement_units,
                android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        String periodValue = habit.goalPeriod;
        String lang = Locale.getDefault().getLanguage();
        if (lang.equals("sk")) {
            switch (periodValue) {
                case "Daily": periodValue = "Denne"; break;
                case "Weekly": periodValue = "Týždenne"; break;
                case "Monthly": periodValue = "Mesačne"; break;
            }
        }
        spinnerPeriod.setSelection(periodAdapter.getPosition(periodValue));

        periodLabel.setText(getString(R.string.goal_period_prefix, periodValue));

        String unitValue = habit.measurement;
        String localizedUnit = unitValue;
        if (lang.equals("sk")) {
            switch (unitValue) {
                case "Times": localizedUnit = "Krát"; break;
                case "Count": localizedUnit = "Počet"; break;
                case "Steps": localizedUnit = "Kroky"; break;
                case "Reps": localizedUnit = "Opakovania"; break;
                case "Calories": localizedUnit = "Kalórie"; break;
                case "Cups": localizedUnit = "Poháre"; break;
                case "sec": localizedUnit = "sek"; break;
                case "hr": localizedUnit = "hoď"; break;
            }
        }

        if (unitAdapter.getPosition(localizedUnit) == -1) {
            customMeasurementField.setVisibility(View.VISIBLE);
            customMeasurementField.setText(unitValue);
            spinnerUnit.setSelection(unitAdapter.getPosition(lang.equals("sk") ? "Vlastné" : "Custom"));
        } else {
            spinnerUnit.setSelection(unitAdapter.getPosition(localizedUnit));
        }

        TextView header = findViewById(R.id.textHeaderHabitName);
        header.setText(habit.habitName.length() > 15 ? habit.habitName.substring(0, 15) + "..." : habit.habitName);

        if (isMyHabit) {
            buttonAction.setText(getString(R.string.remove_sharing));
            buttonAction.setOnClickListener(v -> removeFromFirestore());
        } else {
            buttonAction.setText(getString(R.string.adapt_habit));
            buttonAction.setOnClickListener(v -> saveToLocalDatabase());
        }

        buttonCancel.setOnClickListener(v -> {
            startActivity(new Intent(this, SocialActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        Button socialButton = findViewById(R.id.buttonSocial);
        Button statisticsButton = findViewById(R.id.buttonStatistics);
        Button addHabitButton = findViewById(R.id.buttonAddHabit);
        Button myHabitsButton = findViewById(R.id.buttonMyHabits);
        Button settingsButton = findViewById(R.id.buttonSettings);

        statisticsButton.setOnClickListener(view -> navigateTo(StatisticsActivity.class));
        addHabitButton.setOnClickListener(view -> navigateTo(AddHabitActivity.class));
        myHabitsButton.setOnClickListener(view -> navigateTo(MyHabitsActivity.class));
        socialButton.setOnClickListener(view -> navigateTo(SocialActivity.class));
        settingsButton.setOnClickListener(view -> navigateTo(SettingsActivity.class));

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

    private void saveToLocalDatabase() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Habit newHabit = new Habit(
                0,
                habit.habitName,
                habit.description,
                habit.goalValue,
                habit.measurement,
                habit.goalPeriod,
                habit.habitType,
                today
        );

        dbHelper.insertHabit(newHabit);
        Toast.makeText(this, getString(R.string.habit_adapted), Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, SocialActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void removeFromFirestore() {
        firestore.collection("sharedHabits")
                .whereEqualTo("habitName", habit.habitName)
                .whereEqualTo("sharedByUsername", habit.sharedByUsername)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        snapshot.getDocuments().get(0).getReference().delete();
                        Toast.makeText(this, getString(R.string.habit_removed), Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(this, SocialActivity.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();

                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to remove.", Toast.LENGTH_SHORT).show());
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        android.net.Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;

        android.net.NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null &&
                (capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    private final android.content.BroadcastReceiver networkReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkAccessConditions();
        }
    };

    private void checkAccessConditions() {
        boolean isConnected = isInternetAvailable();
        boolean isLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;

        if (!isConnected || !isLoggedIn) {
            String lang = Locale.getDefault().getLanguage();
            String message = lang.equals("sk") ?
                    "Stratilo sa pripojenie, návrat späť..." :
                    "Connection lost, returning...";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            startActivity(new Intent(this, SocialActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;

        android.net.Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;

        android.net.NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null &&
                (capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAccessConditions();
        applyFullScreenMode();
    }
}