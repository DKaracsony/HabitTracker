package com.example.dk_habittracker;

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
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private AlertDialog noInternetDialog;
    private boolean isDialogVisible = false;

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;

    // Activity Result Launcher for WiFi settings
    private final ActivityResultLauncher<Intent> wifiSettingsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (!isInternetAvailable()) {
                    showNoInternetDialog();
                }
            });

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isInternetAvailable()) {
                showNoInternetDialog();
            } else if (isDialogVisible) {
                dismissNoInternetDialog();
                Toast.makeText(LoginActivity.this, getString(R.string.internet_restored), Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        //For Keyboard Visibility
        setKeyboardListener();

        Button buttonGoBack = findViewById(R.id.buttonGoBack);
        Button buttonRegister = findViewById(R.id.buttonRegister);
        Button buttonForgotPassword = findViewById(R.id.buttonForgotPassword);
        buttonForgotPassword.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });


        buttonGoBack.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, StartActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });


        buttonRegister.setOnClickListener(view -> {
            if (isInternetAvailable()) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            } else {
                showNoInternetDialog();
            }
        });


        buttonLogin.setOnClickListener(view -> loginUser());

        if (!isInternetAvailable()) {
            showNoInternetDialog();
        }

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }


    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            Toast.makeText(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                            // Ensure full-screen mode before transition
                            setFullScreenMode();
                            // To MyHabitsActivity
                            Intent intent = new Intent(LoginActivity.this, MyHabitsActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, getString(R.string.verify_email_prompt), Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    private void setKeyboardListener() {
        final View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
            if (heightDiff < 200) { // Keyboard is likely hidden
                setFullScreenMode();
            }
        });
    }

    private void showNoInternetDialog() {
        if (noInternetDialog == null || !noInternetDialog.isShowing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.no_internet_title))
                    .setMessage(getString(R.string.no_internet_message))
                    .setPositiveButton(getString(R.string.go_to_wifi_settings), (dialog, which) ->
                            wifiSettingsLauncher.launch(new Intent(Settings.ACTION_WIFI_SETTINGS))
                    )
                    .setNegativeButton(getString(R.string.back), (dialog, which) -> {
                        startActivity(new Intent(LoginActivity.this, StartActivity.class));
                        finish();
                    })
                    .setCancelable(false);

            noInternetDialog = builder.create();
            noInternetDialog.show();
            isDialogVisible = true;
        }
    }

    private void dismissNoInternetDialog() {
        if (isDialogVisible && noInternetDialog != null && noInternetDialog.isShowing()) {
            noInternetDialog.dismiss();
            isDialogVisible = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isInternetAvailable()) {
            showNoInternetDialog();
        }
        setFullScreenMode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkReceiver);
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


}
