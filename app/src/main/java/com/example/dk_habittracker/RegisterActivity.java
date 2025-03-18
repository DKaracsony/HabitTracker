package com.example.dk_habittracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText editTextEmail, editTextPassword, editTextConfirmPassword, editTextNickname;
    private TextView textEmailStatus, textNicknameStatus, textPasswordStatus, textConfirmPasswordStatus;
    private Button buttonRegister, buttonGoBack;

    private boolean isEmailAvailable = false;
    private boolean isNicknameAvailable = false;

    private AlertDialog noInternetDialog;
    private boolean isDialogVisible = false;
    private boolean wasInSettings = false;

    private boolean isPasswordStrong = false;
    private boolean isPasswordConfirmed = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextEmail = findViewById(R.id.editTextRegisterEmail);
        editTextPassword = findViewById(R.id.editTextRegisterPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextNickname = findViewById(R.id.editTextRegisterNickname);

        textEmailStatus = findViewById(R.id.textEmailStatus);
        textNicknameStatus = findViewById(R.id.textNicknameStatus);
        textPasswordStatus = findViewById(R.id.textPasswordStatus);
        textConfirmPasswordStatus = findViewById(R.id.textConfirmPasswordStatus);

        buttonRegister = findViewById(R.id.buttonRegister);
        buttonGoBack = findViewById(R.id.buttonGoBack);

        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

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

        buttonGoBack.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });


        // Real-time Email Availability Check
        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    checkEmailAvailability(s.toString().trim());
                } else {
                    textEmailStatus.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Real-time Nickname Availability Check
        editTextNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    checkNicknameAvailability(s.toString().trim());
                } else {
                    textNicknameStatus.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Real-time Password Strength Check
        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkPasswordStrength(s.toString());
                checkPasswordMatch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Real-time Password Confirmation Check
        editTextConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    checkPasswordMatch();
                } else {
                    textConfirmPasswordStatus.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Register Button Click
        buttonRegister.setOnClickListener(view -> registerUser());
    }

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isInternetAvailable()) {
                showNoInternetDialog();
            } else if (isDialogVisible) {
                dismissNoInternetDialog();
                Toast.makeText(RegisterActivity.this, getString(R.string.internet_restored), Toast.LENGTH_SHORT).show();
            }
        }
    };


    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String nickname = editTextNickname.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEmailAvailable) {
            Toast.makeText(this, getString(R.string.email_in_use), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNicknameAvailable) {
            Toast.makeText(this, getString(R.string.nickname_in_use), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPasswordStrong) {
            Toast.makeText(this, getString(R.string.password_not_strong), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPasswordConfirmed) {
            Toast.makeText(this, getString(R.string.passwords_no_match), Toast.LENGTH_SHORT).show();
            return;
        }

        //Register User with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        //Get UID before sign-out
                        String userId = mAuth.getCurrentUser().getUid();

                        //Save user data to Firestore **before signing out**
                        saveUserToFirestore(userId, email, nickname, () -> {

                            //Send Email Verification only AFTER Firestore Save
                            mAuth.getCurrentUser().sendEmailVerification()
                                    .addOnCompleteListener(verificationTask -> {
                                        if (verificationTask.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this,
                                                    getString(R.string.verification_email_sent, email),
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(RegisterActivity.this,
                                                    getString(R.string.verification_email_failed),
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                        //Safe to log out
                                        mAuth.signOut();

                                        //Redirect to LoginActivity
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    });
                        });
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                getString(R.string.registration_failed, task.getException().getMessage()),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String userId, String email, String nickname, Runnable onSuccess) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("nickname", nickname);
        userMap.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(userId).set(userMap)
                .addOnSuccessListener(aVoid -> {
                    //Send email and log-out
                    onSuccess.run();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(RegisterActivity.this,
                                getString(R.string.firestore_error, e.getMessage()),
                                Toast.LENGTH_SHORT).show()
                );
    }


    //Check Email Availability in Firebase Authentication + Firestore
    private void checkEmailAvailability(String email) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        boolean isEmailTakenAuth = !task.getResult().getSignInMethods().isEmpty();

                        db.collection("users").whereEqualTo("email", email).get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    boolean isEmailTakenFirestore = !queryDocumentSnapshots.isEmpty();

                                    if (isEmailTakenAuth || isEmailTakenFirestore) {
                                        textEmailStatus.setText(getString(R.string.email_taken));
                                        textEmailStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                                        isEmailAvailable = false;
                                    } else {
                                        textEmailStatus.setText(getString(R.string.email_available));
                                        textEmailStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                                        isEmailAvailable = true;
                                    }
                                });
                    }
                });
    }

    //Check Nickname Availability in Firestore
    private void checkNicknameAvailability(String nickname) {
        db.collection("users").whereEqualTo("nickname", nickname).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        textNicknameStatus.setText(getString(R.string.nickname_taken));
                        textNicknameStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        isNicknameAvailable = false;
                    } else {
                        textNicknameStatus.setText(getString(R.string.nickname_available));
                        textNicknameStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        isNicknameAvailable = true;
                    }

                });
    }


    //Check Password Strength
    private void checkPasswordStrength(String password) {
        if (password.length() < 8) {
            textPasswordStatus.setText(getString(R.string.weak_password));
            textPasswordStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            isPasswordStrong = false; //Weak password
        } else if (!password.matches(".*[A-Z].*") || !password.matches(".*[0-9].*")) {
            textPasswordStatus.setText(getString(R.string.medium_password));
            textPasswordStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            isPasswordStrong = false; //Medium password still not acceptable
        } else {
            textPasswordStatus.setText(getString(R.string.strong_password));
            textPasswordStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            isPasswordStrong = true; //Only strong passwords are accepted
        }
    }


    //Check Password Confirmation
    private void checkPasswordMatch() {
        if (editTextPassword.getText().toString().equals(editTextConfirmPassword.getText().toString())) {
            textConfirmPasswordStatus.setText(getString(R.string.password_match));
            textConfirmPasswordStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            isPasswordConfirmed = true; //Passwords match
        } else {
            textConfirmPasswordStatus.setText(getString(R.string.password_no_match));
            textConfirmPasswordStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            isPasswordConfirmed = false; //Passwords don't match
        }
    }


    private void showNoInternetDialog() {
        if (noInternetDialog == null || !noInternetDialog.isShowing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.no_internet_title))
                    .setMessage(getString(R.string.no_internet_message))
                    .setPositiveButton(getString(R.string.go_to_wifi_settings), (dialog, which) -> {
                        wasInSettings = true;
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    })
                    .setNegativeButton(getString(R.string.back), (dialog, which) -> {
                        startActivity(new Intent(RegisterActivity.this, StartActivity.class));
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
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

}