package com.example.dk_habittracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
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

        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        Button buttonResetPassword = findViewById(R.id.buttonResetPassword);
        buttonResetPassword.setOnClickListener(view -> {
            if (isInternetAvailable()) {
                checkIfEmailExists();
            } else {
                showNoInternetDialog();
            }
        });

        Button buttonGoBack = findViewById(R.id.buttonGoBackForgot);
        buttonGoBack.setOnClickListener(view -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

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
                        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                        finish();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this,
                                getString(R.string.password_reset_failed,
                                        task.getException() != null ? task.getException().getMessage() : R.string.unknown_error),
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
        wasInSettings = false;
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
            android.net.Network network = cm.getActiveNetwork();
            if (network == null) return false;

            android.net.NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            return capabilities != null && (
                            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        }
        return false;
    }
}
