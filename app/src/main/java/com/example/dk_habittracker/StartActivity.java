    package com.example.dk_habittracker;

    import android.content.BroadcastReceiver;
    import android.content.Context;
    import android.content.Intent;
    import android.content.IntentFilter;
    import android.content.SharedPreferences;
    import android.content.res.Configuration;
    import android.net.ConnectivityManager;
    import android.net.NetworkCapabilities;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.Button;

    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;

    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseUser;

    import java.util.Locale;

    public class StartActivity extends AppCompatActivity {

        private boolean hasNavigated = false;
        private static final String PREFS_NAME = "UserPreferences";
        private static final String KEY_LANGUAGE = "My_Lang";
        private static final String KEY_MODE = "user_mode";

        private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkLoginStatus();
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

            applySavedLanguage();

            setContentView(R.layout.activity_start);

            Button btnEnglish = findViewById(R.id.btnEnglish);
            Button btnSlovak = findViewById(R.id.btnSlovak);
            Button signInButton = findViewById(R.id.button2);
            Button useOfflineButton = findViewById(R.id.button1);

            checkLoginStatus();

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

            editor.putString(KEY_MODE, "offline");
            editor.apply();
            launchFirstActivity(true);
        }

        private void launchFirstActivity(boolean isOffline) {
            Intent intent = new Intent(StartActivity.this, MyHabitsActivity.class);
            intent.putExtra("isOffline", isOffline);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }

        private void checkLoginStatus() {
            if (hasNavigated) return;

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && isInternetAvailable()) {
                hasNavigated = true;
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
            if (cm == null) return false;

            android.net.Network network = cm.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                            || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }


        private void showNoInternetDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.no_internet_title))
                    .setMessage(getString(R.string.no_internet_message))
                    .setPositiveButton(getString(R.string.try_again), (dialog, which) -> checkInternetAndProceed())
                    .setNegativeButton(getString(R.string.back), (dialog, which) -> {
                        dialog.dismiss();
                        setFullScreenMode();
                    })
                    .setCancelable(false);

            AlertDialog alert = builder.create();
            alert.show();
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

            Intent intent = new Intent(this, StartActivity.class);
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
        protected void onDestroy() {
            super.onDestroy();
            unregisterReceiver(networkReceiver);
        }
    }
