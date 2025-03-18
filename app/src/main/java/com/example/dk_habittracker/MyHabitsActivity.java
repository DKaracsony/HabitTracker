package com.example.dk_habittracker;
//CLEAN LIBS!!!

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MyHabitsActivity extends AppCompatActivity {

    private TextView textViewSelectedDate;
    private Calendar calendar;
    private RecyclerView recyclerViewNotCompleted, recyclerViewCompleted;
    private HabitAdapter notCompletedAdapter, completedAdapter;
    private List<Habit> notCompletedHabits, completedHabits;
    private DBHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_habits);
        applyFullScreenMode();
        dbHelper = new DBHelper(this);

        // Initialize UI elements
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        ImageButton buttonPreviousDate = findViewById(R.id.buttonPreviousDate);
        ImageButton buttonNextDate = findViewById(R.id.buttonNextDate);

        recyclerViewNotCompleted = findViewById(R.id.recyclerViewNotCompleted);
        recyclerViewCompleted = findViewById(R.id.recyclerViewCompleted);

        recyclerViewNotCompleted.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCompleted.setLayoutManager(new LinearLayoutManager(this));

        calendar = Calendar.getInstance();

        // Check if a date was passed from HabitDetailActivity
        String passedDate = getIntent().getStringExtra("selected_date");
        if (passedDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                calendar.setTime(dateFormat.parse(passedDate)); // Set calendar to the passed date
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        updateDateDisplay();

        buttonPreviousDate.setOnClickListener(v -> changeDate(-1));
        buttonNextDate.setOnClickListener(v -> changeDate(1));

        // Handle Footer Navigation
        Button socialButton = findViewById(R.id.buttonSocial);
        Button statisticsButton = findViewById(R.id.buttonStatistics);
        Button addHabitButton = findViewById(R.id.buttonAddHabit);
        Button myHabitsButton = findViewById(R.id.buttonMyHabits);
        Button settingsButton = findViewById(R.id.buttonSettings);

        socialButton.setOnClickListener(view -> navigateTo(SocialActivity.class));
        statisticsButton.setOnClickListener(view -> navigateTo(StatisticsActivity.class));
        addHabitButton.setOnClickListener(view -> navigateTo(AddHabitActivity.class));
        settingsButton.setOnClickListener(view -> navigateTo(SettingsActivity.class));

        // Apply full-screen mode
        applyFullScreenMode();
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        textViewSelectedDate.setText(dateFormat.format(calendar.getTime()));

        ImageButton buttonNextDate = findViewById(R.id.buttonNextDate);
        Calendar today = Calendar.getInstance();
        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            buttonNextDate.setEnabled(false);
            buttonNextDate.setAlpha(0.5f);
        } else {
            buttonNextDate.setEnabled(true);
            buttonNextDate.setAlpha(1.0f);
        }
        loadHabitsForDate();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyFullScreenMode(); //Reapply FullScreen
        }
    }

    private void changeDate(int days) {
        calendar.add(Calendar.DAY_OF_MONTH, days);
        updateDateDisplay();
    }

    private void loadHabitsForDate() {
        String selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        List<Habit> allHabits = dbHelper.getAllHabits();
        notCompletedHabits = new ArrayList<>();
        completedHabits = new ArrayList<>();

        for (Habit habit : allHabits) {
            int progress = dbHelper.getProgressForPeriod(habit.getId(), habit.getGoalPeriod(), selectedDate);

            if (habit.getHabitType().equals("Quit")) {
                if (progress < habit.getGoal()) {
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

        // Lists (Empty)
        TextView textViewEmptyNotCompleted = findViewById(R.id.textViewEmptyNotCompleted);
        TextView textViewEmptyCompleted = findViewById(R.id.textViewEmptyCompleted);

        // Empty list messages
        if (notCompletedHabits.isEmpty()) {
            textViewEmptyNotCompleted.setVisibility(View.VISIBLE);
        } else {
            textViewEmptyNotCompleted.setVisibility(View.GONE);
        }

        if (completedHabits.isEmpty()) {
            textViewEmptyCompleted.setVisibility(View.VISIBLE);
        } else {
            textViewEmptyCompleted.setVisibility(View.GONE);
        }

        // Update adapters and set RecyclerView
        notCompletedAdapter = new HabitAdapter(this, notCompletedHabits, selectedDate);
        completedAdapter = new HabitAdapter(this, completedHabits, selectedDate);

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

        // Problematic Full Screen after transition
        new android.os.Handler().postDelayed(() -> getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN), 500);
    }


}