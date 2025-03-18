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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private Button buttonResetPassword, buttonGoBack;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private AlertDialog noInternetDialog;
    private boolean isDialogVisible = false;
    private boolean wasInSettings = false;

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isInternetAvailable()) {
                showNoInternetDialog();
            } else if (isDialogVisible) {
                dismissNoInternetDialog();
                Toast.makeText(ForgotPasswordActivity.this,
                        getString(R.string.internet_restored), Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextEmail = findViewById(R.id.editTextForgotPasswordEmail);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);
        buttonGoBack = findViewById(R.id.buttonGoBackForgot);

        // Register network receiver
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        // Handle Password Reset
        buttonResetPassword.setOnClickListener(view -> {
            if (isInternetAvailable()) {
                checkIfEmailExists();
            } else {
                showNoInternetDialog();
            }
        });

        // Go Back Button
        buttonGoBack.setOnClickListener(view -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish(); // Close ForgotPasswordActivity
        });


        // Show pop-up immediately if there is no internet (Possibly removing it sicne Receiver is registered)
        if (!isInternetAvailable()) {
            showNoInternetDialog();
        }

        //FS
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void checkIfEmailExists() {
        String email = editTextEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, getString(R.string.enter_email), Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        sendPasswordResetEmail(email);
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this,
                                getString(R.string.email_not_registered), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(ForgotPasswordActivity.this,
                                getString(R.string.firestore_error, e.getMessage()), Toast.LENGTH_SHORT).show()
                );
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                getString(R.string.password_reset_sent), Toast.LENGTH_LONG).show();
                        // ✅ Redirect to LoginActivity
                        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                        finish(); // Close ForgotPasswordActivity
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this,
                                getString(R.string.password_reset_failed, task.getException().getMessage()),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showNoInternetDialog() {
        if (noInternetDialog == null || !noInternetDialog.isShowing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.no_internet_title))
                    .setMessage(getString(R.string.no_internet_message))
                    .setPositiveButton(getString(R.string.go_to_wifi_settings), (dialog, which) -> {
                        wasInSettings = true; // Mark that user went to settings
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    })
                    .setNegativeButton(getString(R.string.back), (dialog, which) -> {
                        startActivity(new Intent(ForgotPasswordActivity.this, StartActivity.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
        if (wasInSettings && !isInternetAvailable()) {
            showNoInternetDialog();
        }
        wasInSettings = false; // Reset flag
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

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }
}
