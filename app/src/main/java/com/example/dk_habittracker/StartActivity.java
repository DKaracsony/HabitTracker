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

    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;
    import java.util.Locale;
    import java.util.UUID;

    public class StartActivity extends AppCompatActivity {

        private SharedPreferences preferences;
        private TextView internetStatusText;
        private boolean hasNavigated = false; // Preventing Duplicate Launches
        private static final String PREFS_NAME = "UserPreferences";
        private static final String KEY_LANGUAGE = "My_Lang";
        private static final String KEY_UUID = "offline_uuid";
        private static final String KEY_MODE = "user_mode";

        private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateInternetStatus();
                checkLoginStatus(); // Only checks login status for online users
            }
        };

        @Override
        protected void onResume() {
            super.onResume();
            setFullScreenMode();
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Apply saved language before setting content view
            applySavedLanguage();

            setContentView(R.layout.activity_start);

            // Initialize UI elements
            internetStatusText = findViewById(R.id.textInternetStatus);
            Button btnEnglish = findViewById(R.id.btnEnglish);
            Button btnSlovak = findViewById(R.id.btnSlovak);
            Button signInButton = findViewById(R.id.button2);
            Button useOfflineButton = findViewById(R.id.button1);

            updateInternetStatus();
            checkLoginStatus(); // Only redirects online users

            btnEnglish.setOnClickListener(view -> setLocale("en"));
            btnSlovak.setOnClickListener(view -> setLocale("sk"));

            signInButton.setOnClickListener(view -> checkInternetAndProceed());
            useOfflineButton.setOnClickListener(view -> handleOfflineMode());

            registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

            setFullScreenMode();
        }

        private void setFullScreenMode() {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        private void handleOfflineMode() {
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            String uuid;
            if (!sharedPreferences.contains(KEY_UUID)) {
                uuid = UUID.randomUUID().toString();
                editor.putString(KEY_UUID, uuid);
                editor.apply();
            } else {
                uuid = sharedPreferences.getString(KEY_UUID, "Unknown UUID");
            }

            Toast.makeText(this, "UUID: " + uuid, Toast.LENGTH_LONG).show();

            editor.putString(KEY_MODE, "offline");
            editor.apply();
            launchFirstActivity(true);
        }

        private boolean isOfflineUser() {
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            return sharedPreferences.contains(KEY_UUID) && "offline".equals(sharedPreferences.getString(KEY_MODE, ""));
        }

        private void launchFirstActivity(boolean isOffline) {
            Intent intent = new Intent(StartActivity.this, MyHabitsActivity.class);
            intent.putExtra("isOffline", isOffline);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // Smooth transition
            finish();
        }


        private void updateInternetStatus() {
            if (isInternetAvailable()) {
                internetStatusText.setText(getString(R.string.internet_connected));
                internetStatusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                internetStatusText.setText(getString(R.string.no_internet));
                internetStatusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        }

        private void checkLoginStatus() {
            if (hasNavigated) return; // Avoid launching twice

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && isInternetAvailable()) {
                hasNavigated = true; // Mark as navigated
                launchFirstActivity(false);
                finish();
            }
        }

        private void checkInternetAndProceed() {
            if (isInternetAvailable()) {
                Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            } else {
                showNoInternetDialog();
            }
        }

        private boolean isInternetAvailable() {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnected();
            }
            return false;
        }

        private void showNoInternetDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.no_internet_title))
                    .setMessage(getString(R.string.no_internet_message))
                    .setPositiveButton(getString(R.string.try_again), (dialog, which) -> checkInternetAndProceed())
                    .setNegativeButton(getString(R.string.back), (dialog, which) -> {
                        dialog.dismiss();
                        setFullScreenMode(); //Full-screen mode is reapplied
                    })
                    .setCancelable(false);

            AlertDialog alert = builder.create();
            alert.show();
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

            // Restart activity to apply changes
            Intent intent = new Intent(this, StartActivity.class);
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
        protected void onDestroy() {
            super.onDestroy();
            unregisterReceiver(networkReceiver);
        }
    }
