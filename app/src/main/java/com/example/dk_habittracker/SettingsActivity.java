package com.example.dk_habittracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPreferences";
    private static final String KEY_LANGUAGE = "My_Lang";
    private FirebaseAuth mAuth;
    private Button buttonLogout;
    private View userDetailsSection;
    private TextView textUsername, textEmail, textUUID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply saved language before setting content view
        applySavedLanguage();

        setContentView(R.layout.activity_settings);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        // Language Buttons
        Button buttonLanguageSK = findViewById(R.id.buttonLanguageSK);
        Button buttonLanguageEN = findViewById(R.id.buttonLanguageEN);
        buttonLogout = findViewById(R.id.buttonLogout);

        buttonLanguageSK.setOnClickListener(view -> setLocale("sk"));
        buttonLanguageEN.setOnClickListener(view -> setLocale("en"));

        userDetailsSection = findViewById(R.id.userDetailsSection);
        textUsername = findViewById(R.id.textUsername);
        textEmail = findViewById(R.id.textEmail);
        textUUID = findViewById(R.id.textUUID);

        //Check login status and internet before enabling the logout button
        checkLoginStatus();

        // Handle Log Out Button
        buttonLogout.setOnClickListener(view -> {
            mAuth.signOut();
            Toast.makeText(SettingsActivity.this, getString(R.string.signed_out), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SettingsActivity.this, StartActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        // Navigation Buttons
        Button socialButton = findViewById(R.id.buttonSocial);
        Button statisticsButton = findViewById(R.id.buttonStatistics);
        Button addHabitButton = findViewById(R.id.buttonAddHabit);
        Button myHabitsButton = findViewById(R.id.buttonMyHabits);

        statisticsButton.setOnClickListener(view -> navigateTo(StatisticsActivity.class));
        addHabitButton.setOnClickListener(view -> navigateTo(AddHabitActivity.class));
        myHabitsButton.setOnClickListener(view -> navigateTo(MyHabitsActivity.class));
        socialButton.setOnClickListener(view -> navigateTo(SocialActivity.class));

        // Apply full-screen mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void checkLoginStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        boolean isConnected = isInternetAvailable();

        if (user != null && isConnected) {
            //User is online and logged in -> Show user details
            buttonLogout.setEnabled(true);
            buttonLogout.setAlpha(1.0f);
            userDetailsSection.setVisibility(View.VISIBLE);
            textUUID.setVisibility(View.GONE);

            //Fetch user data (Firebase Authentication only stores email, nickname comes from Firestore [Users Table])
            textEmail.setText(getString(R.string.email_placeholder) + " " + user.getEmail());

            //Fetch username from Firestore - Users Table
            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists() && document.contains("nickname")) {
                            textUsername.setText(getString(R.string.username_placeholder) + " " + document.getString("nickname"));
                        } else {
                            textUsername.setText(getString(R.string.username_placeholder) + " -");
                        }
                    });

        } else {
            //No internet or not logged in -> Show UUID
            buttonLogout.setEnabled(false);
            buttonLogout.setAlpha(0.5f);
            userDetailsSection.setVisibility(View.GONE);
            textUUID.setVisibility(View.VISIBLE);

            //Retrieve UUID from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
            String uuid = sharedPreferences.getString("offline_uuid", "Unknown UUID");
            textUUID.setText(getString(R.string.your_uuid) + " " + uuid);
        }
    }


    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkLoginStatus(); //Real-time updates for login status and UUID
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

    private void setLocale(String lang) {
        // Save selected language in SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_LANGUAGE, lang);
        editor.apply();

        // Apply language change
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Restart the activity to apply changes globally
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void applySavedLanguage() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedLanguage = preferences.getString(KEY_LANGUAGE, "en"); // Default to English
        Locale locale = new Locale(savedLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    @Override
    protected void onResume(){
        super.onResume();
        checkLoginStatus(); //Ensure button is updated when activity resumes

        // Apply full-screen mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkReceiver); //Unregister
    }


}
