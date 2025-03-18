package com.example.dk_habittracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SocialActivity extends AppCompatActivity {

    private TextView textViewNoAccess;
    private View mainContent; // The main layout that will be hidden if access is denied
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        textViewNoAccess = findViewById(R.id.textViewNoAccess);
        mainContent = findViewById(R.id.mainContent); // This should be the parent layout of the activity content

        // Register network change receiver
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        // Footer Navigation Buttons
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

        // Initial check for access conditions
        checkAccessConditions();

        // Apply full-screen mode
        applyFullScreenMode();
    }

    private void checkAccessConditions() {
        FirebaseUser user = mAuth.getCurrentUser();
        boolean isConnected = isInternetAvailable();

        if (user == null || !isConnected) {
            // No access: Show message, hide main content
            textViewNoAccess.setVisibility(View.VISIBLE);
            mainContent.setVisibility(View.GONE);
        } else {
            // Access granted: Show content, hide message
            textViewNoAccess.setVisibility(View.GONE);
            mainContent.setVisibility(View.VISIBLE);
        }
    }

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkAccessConditions(); // Update UI in real-time when internet state changes
        }
    };

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
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
        checkAccessConditions(); // Ensure access conditions are checked when resuming activity
        applyFullScreenMode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkReceiver); //Unregister
    }
}
