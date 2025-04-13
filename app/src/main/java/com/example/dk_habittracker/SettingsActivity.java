package com.example.dk_habittracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;
    private static final String PREF_DENIED_ONCE = "denied_notifications_once";
    private static final String PREFS_NAME = "UserPreferences";
    private static final String KEY_LANGUAGE = "My_Lang";
    private FirebaseAuth mAuth;
    private Button buttonLogout, buttonReturnToStart;
    private View userDetailsSection;
    private TextView textUsername, textEmail, textUUID;
    private SwitchCompat switchReminder;
    private View reminderTimeSection;
    private TextView textReminderTime;
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

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

        applySavedLanguage();

        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();

        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        Button buttonLanguageSK = findViewById(R.id.buttonLanguageSK);
        Button buttonLanguageEN = findViewById(R.id.buttonLanguageEN);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonReturnToStart = findViewById(R.id.buttonReturnToStart);

        buttonLanguageSK.setOnClickListener(view -> setLocale("sk"));
        buttonLanguageEN.setOnClickListener(view -> setLocale("en"));

        userDetailsSection = findViewById(R.id.userDetailsSection);
        textUsername = findViewById(R.id.textUsername);
        textEmail = findViewById(R.id.textEmail);
        textUUID = findViewById(R.id.textUUID);

        checkLoginStatus();

        buttonLogout.setOnClickListener(view -> {
            mAuth.signOut();
            Toast.makeText(SettingsActivity.this, getString(R.string.signed_out), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SettingsActivity.this, StartActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        buttonReturnToStart.setOnClickListener(view -> {
            Intent intent = new Intent(SettingsActivity.this, StartActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        Button socialButton = findViewById(R.id.buttonSocial);
        Button statisticsButton = findViewById(R.id.buttonStatistics);
        Button addHabitButton = findViewById(R.id.buttonAddHabit);
        Button myHabitsButton = findViewById(R.id.buttonMyHabits);

        statisticsButton.setOnClickListener(view -> navigateTo(StatisticsActivity.class));
        addHabitButton.setOnClickListener(view -> navigateTo(AddHabitActivity.class));
        myHabitsButton.setOnClickListener(view -> navigateTo(MyHabitsActivity.class));
        socialButton.setOnClickListener(view -> navigateTo(SocialActivity.class));

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        switchReminder = findViewById(R.id.switchReminder);
        reminderTimeSection = findViewById(R.id.reminderTimeSection);
        textReminderTime = findViewById(R.id.textReminderTime);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isReminderEnabled = prefs.getBoolean("reminder_enabled", false);
        String timeText = prefs.getString("reminder_time_text", "hh:mm");
        int hour = prefs.getInt("reminder_hour", 8);
        int minute = prefs.getInt("reminder_minute", 0);

        boolean deniedOnce = prefs.getBoolean(PREF_DENIED_ONCE, false);
        if (deniedOnce && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            switchReminder.setChecked(false);
            prefs.edit().putBoolean("reminder_enabled", false).apply();
            reminderTimeSection.setVisibility(View.GONE);
        } else {
            switchReminder.setChecked(isReminderEnabled);
            reminderTimeSection.setVisibility(isReminderEnabled ? View.VISIBLE : View.GONE);
        }

        reminderTimeSection.setVisibility(isReminderEnabled ? View.VISIBLE : View.GONE);
        textReminderTime.setText(timeText);

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefsLocal = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefsLocal.edit();
            editor.putBoolean("reminder_enabled", isChecked).apply();
            reminderTimeSection.setVisibility(isChecked ? View.VISIBLE : View.GONE);

            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                                != PackageManager.PERMISSION_GRANTED) {

                    if (!deniedOnce) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                                REQUEST_NOTIFICATION_PERMISSION);
                    } else {
                        Toast.makeText(this, getString(R.string.notifications_denied_permanently), Toast.LENGTH_LONG).show();
                        switchReminder.setChecked(false);
                        reminderTimeSection.setVisibility(View.GONE);
                    }

                } else {
                    showTimePickerDialog(hour, minute);
                }

            } else {
                cancelReminder();
            }
        });

        Button buttonPickTime = findViewById(R.id.buttonPickTime);
        buttonPickTime.setOnClickListener(v -> {
            showTimePickerDialog(hour, minute);
        });

        Button buttonResetProgress = findViewById(R.id.buttonResetProgress);
        Button buttonDeleteHabits = findViewById(R.id.buttonDeleteHabits);

        buttonResetProgress.setOnClickListener(v -> {
            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(getString(R.string.confirm_reset_title))
                    .setMessage(getString(R.string.confirm_reset_message))
                    .setPositiveButton(getString(R.string.yes), (d, which) -> {
                        try (DBHelper dbHelper = new DBHelper(this)) {
                            dbHelper.resetAllProgress();
                        }
                        Toast.makeText(this, getString(R.string.progress_reset_success), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .create();

            dialog.setOnDismissListener(d -> {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            });
            dialog.show();
        });

        buttonDeleteHabits.setOnClickListener(v -> {
            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(getString(R.string.confirm_delete_title))
                    .setMessage(getString(R.string.confirm_delete_message))
                    .setPositiveButton(getString(R.string.yes), (d, which) -> {
                        try (DBHelper dbHelper = new DBHelper(this)) {
                            dbHelper.deleteAllHabits();
                        }
                        Toast.makeText(this, getString(R.string.habits_deleted_success), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .create();

            dialog.setOnDismissListener(d -> {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            });
            dialog.show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.notifications_enabled), Toast.LENGTH_SHORT).show();

                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                int hour = prefs.getInt("reminder_hour", 8);
                int minute = prefs.getInt("reminder_minute", 0);

                showTimePickerDialog(hour, minute);
            } else {
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                prefs.edit().putBoolean(PREF_DENIED_ONCE, true).apply();

                Toast.makeText(this, getString(R.string.notifications_denied), Toast.LENGTH_SHORT).show();
                switchReminder.setChecked(false);
                reminderTimeSection.setVisibility(View.GONE);
            }
        }
    }

    private void showTimePickerDialog(int preHour, int preMinute) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    textReminderTime.setText(formattedTime);

                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("reminder_time_text", formattedTime);
                    editor.putInt("reminder_hour", hourOfDay);
                    editor.putInt("reminder_minute", minute);
                    editor.apply();

                    setDailyReminder(hourOfDay, minute);
                },
                preHour,
                preMinute,
                true
        );

        timePickerDialog.setOnDismissListener(dialog -> {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        });
        timePickerDialog.show();
    }

    private void setDailyReminder(int hour, int minute) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = getTriggerTime(hour, minute);

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                AlarmManager.INTERVAL_DAY,
                alarmIntent
        );
        Toast.makeText(this, getString(R.string.reminder_set), Toast.LENGTH_SHORT).show();
    }

    private long getTriggerTime(int hour, int minute) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hour);
        calendar.set(java.util.Calendar.MINUTE, minute);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1);
        }

        return calendar.getTimeInMillis();
    }

    private void cancelReminder() {
        Intent intent = new Intent(this, ReminderReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(alarmIntent);

        Toast.makeText(this, getString(R.string.reminder_cancelled), Toast.LENGTH_SHORT).show();
    }

    private void checkLoginStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        boolean isConnected = isInternetAvailable();

        if (user != null && isConnected) {
            buttonLogout.setEnabled(true);
            buttonLogout.setAlpha(1.0f);
            userDetailsSection.setVisibility(View.VISIBLE);
            textUUID.setVisibility(View.GONE);
            buttonReturnToStart.setVisibility(View.GONE);

            textEmail.setText(getString(R.string.email_placeholder, user.getEmail()));

            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists() && document.contains("nickname")) {
                            textUsername.setText(getString(R.string.username_placeholder, document.getString("nickname")));
                        } else {
                            textUsername.setText(getString(R.string.username_placeholder, "-"));
                        }
                    });

        } else {
            buttonLogout.setEnabled(false);
            buttonLogout.setAlpha(0.5f);
            userDetailsSection.setVisibility(View.GONE);
            textUUID.setVisibility(View.VISIBLE);
            buttonReturnToStart.setVisibility(View.VISIBLE);

            SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
            String uuid = sharedPreferences.getString("offline_uuid", "Unknown UUID");
            textUUID.setText(getString(R.string.your_uuid, uuid));
        }
    }

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkLoginStatus();
        }
    };

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

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void setLocale(String lang) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_LANGUAGE, lang);
        editor.apply();

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void applySavedLanguage() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedLanguage = preferences.getString(KEY_LANGUAGE, "en");
        Locale locale = new Locale(savedLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    @Override
    protected void onResume(){
        super.onResume();
        checkLoginStatus();

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
        unregisterReceiver(networkReceiver);
    }

}
