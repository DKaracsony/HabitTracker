package com.example.dk_habittracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText editTextEmail, editTextPassword, editTextConfirmPassword, editTextNickname;
    private TextView textEmailStatus, textNicknameStatus, textPasswordStatus, textConfirmPasswordStatus;
    private boolean isEmailAvailable = false;
    private boolean isNicknameAvailable = false;
    private AlertDialog noInternetDialog;
    private boolean isDialogVisible = false;
    private boolean wasInSettings = false;
    private boolean isPasswordStrong = false;
    private boolean isPasswordConfirmed = false;

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

        Button buttonRegister = findViewById(R.id.buttonRegister);
        Button buttonGoBack = findViewById(R.id.buttonGoBack);
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

        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String emailInput = s.toString().trim();

                if (emailInput.isEmpty()) {
                    textEmailStatus.setText("");
                    isEmailAvailable = false;
                    return;
                }

                if (!isValidEmailFormat(emailInput)) {
                    textEmailStatus.setText(getString(R.string.invalid_email_format));
                    textEmailStatus.setTextColor(ContextCompat.getColor(RegisterActivity.this, android.R.color.holo_red_dark));
                    isEmailAvailable = false;
                    return;
                }

                checkEmailAvailability(emailInput);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

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

        ImageView buttonPeekPassword = findViewById(R.id.buttonPeekPassword);
        ImageView buttonPeekConfirm = findViewById(R.id.buttonPeekConfirm);

        final boolean[] isPasswordVisible = {false};
        final boolean[] isConfirmVisible = {false};

        buttonPeekPassword.setOnClickListener(v -> {
            isPasswordVisible[0] = !isPasswordVisible[0];

            if (isPasswordVisible[0]) {
                editTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                buttonPeekPassword.setImageResource(R.drawable.ic_eye_open);
            } else {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                buttonPeekPassword.setImageResource(R.drawable.ic_eye_closed);
            }

            editTextPassword.setSelection(editTextPassword.getText().length());
            editTextPassword.setTypeface(Typeface.DEFAULT);
        });

        buttonPeekConfirm.setOnClickListener(v -> {
            isConfirmVisible[0] = !isConfirmVisible[0];

            if (isConfirmVisible[0]) {
                editTextConfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                buttonPeekConfirm.setImageResource(R.drawable.ic_eye_open);
            } else {
                editTextConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                buttonPeekConfirm.setImageResource(R.drawable.ic_eye_closed);
            }

            editTextConfirmPassword.setSelection(editTextConfirmPassword.getText().length());
            editTextConfirmPassword.setTypeface(Typeface.DEFAULT);
        });

        buttonRegister.setOnClickListener(view -> registerUser());

        View.OnFocusChangeListener fullscreenFocusListener = (v, hasFocus) -> {
            if (hasFocus) {
                setFullScreenMode();
            }
        };

        editTextNickname.setOnFocusChangeListener(fullscreenFocusListener);
        editTextEmail.setOnFocusChangeListener(fullscreenFocusListener);
        editTextPassword.setOnFocusChangeListener(fullscreenFocusListener);
        editTextConfirmPassword.setOnFocusChangeListener(fullscreenFocusListener);

        final View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
            if (heightDiff < 200) {
                setFullScreenMode();
            }
        });
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

    private void setFullScreenMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String nickname = editTextNickname.getText().toString().trim();
        Log.d("RegisterDebug", "email=\"" + email);

        if (email.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmailFormat(email)) {
            Toast.makeText(this, getString(R.string.invalid_email_format), Toast.LENGTH_SHORT).show();
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

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        String userId = mAuth.getCurrentUser().getUid();

                        saveUserToFirestore(userId, email, nickname, () -> {

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

                                        mAuth.signOut();

                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                        finish();
                                    });
                        });
                    } else {
                        String errorMessage = (task.getException() != null) ? task.getException().getMessage() : getString(R.string.unknown_error);
                        Toast.makeText(RegisterActivity.this,
                                getString(R.string.registration_failed, errorMessage),
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
                    onSuccess.run();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(RegisterActivity.this,
                                getString(R.string.firestore_error, e.getMessage()),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private boolean isValidEmailFormat(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @SuppressWarnings("deprecation")
    private void checkEmailAvailability(String email) {
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<String> signInMethods = task.getResult().getSignInMethods();
                        boolean isEmailTakenAuth = signInMethods != null && !signInMethods.isEmpty();

                        db.collection("users").whereEqualTo("email", email).get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    boolean isEmailTakenFirestore = !queryDocumentSnapshots.isEmpty();

                                    if (isEmailTakenAuth || isEmailTakenFirestore) {
                                        textEmailStatus.setText(getString(R.string.email_taken));
                                        textEmailStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                                        isEmailAvailable = false;
                                    } else {
                                        textEmailStatus.setText(getString(R.string.email_available));
                                        textEmailStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                                        isEmailAvailable = true;
                                    }
                                });
                    }
                });
    }

    private void checkNicknameAvailability(String nickname) {
        db.collection("users").whereEqualTo("nickname", nickname).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        textNicknameStatus.setText(getString(R.string.nickname_taken));
                        textNicknameStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                        isNicknameAvailable = false;
                    } else {
                        textNicknameStatus.setText(getString(R.string.nickname_available));
                        textNicknameStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                        isNicknameAvailable = true;
                    }

                });
    }

    private void checkPasswordStrength(String password) {
        if (password.length() < 8) {
            textPasswordStatus.setText(getString(R.string.weak_password));
            textPasswordStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            isPasswordStrong = false;
        } else if (!password.matches(".*[A-Z].*") || !password.matches(".*[0-9].*")) {
            textPasswordStatus.setText(getString(R.string.medium_password));
            textPasswordStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
            isPasswordStrong = false;
        } else {
            textPasswordStatus.setText(getString(R.string.strong_password));
            textPasswordStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            isPasswordStrong = true;
        }
    }

    private void checkPasswordMatch() {
        if (editTextPassword.getText().toString().equals(editTextConfirmPassword.getText().toString())) {
            textConfirmPasswordStatus.setText(getString(R.string.password_match));
            textConfirmPasswordStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            isPasswordConfirmed = true;
        } else {
            textConfirmPasswordStatus.setText(getString(R.string.password_no_match));
            textConfirmPasswordStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            isPasswordConfirmed = false;
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

}