package com.example.dk_habittracker;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SocialActivity extends AppCompatActivity {

    private TextView textViewNoAccess;
    private View mainContent;
    private FirebaseAuth mAuth;
    private SharedHabitAdapter adapter;
    private SharedHabitAdapter myAdapter;
    private final List<SharedHabit> sharedHabitList = new ArrayList<>();
    private final List<SharedHabit> mySharedHabitList = new ArrayList<>();

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
        setContentView(R.layout.activity_social);

        mAuth = FirebaseAuth.getInstance();

        textViewNoAccess = findViewById(R.id.textViewNoAccess);
        mainContent = findViewById(R.id.mainContent);

        RecyclerView recyclerViewSharedHabits = findViewById(R.id.recyclerViewSharedHabits);
        recyclerViewSharedHabits.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SharedHabitAdapter(sharedHabitList);
        recyclerViewSharedHabits.setAdapter(adapter);

        RecyclerView recyclerViewMySharedHabits = findViewById(R.id.recyclerViewMySharedHabits);
        recyclerViewMySharedHabits.setLayoutManager(new LinearLayoutManager(this));
        myAdapter = new SharedHabitAdapter(mySharedHabitList);
        recyclerViewMySharedHabits.setAdapter(myAdapter);

        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

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

        checkAccessConditions();
        applyFullScreenMode();
    }

    private void checkAccessConditions() {
        FirebaseUser user = mAuth.getCurrentUser();
        boolean isConnected = isInternetAvailable();

        if (user == null || !isConnected) {
            textViewNoAccess.setVisibility(View.VISIBLE);
            mainContent.setVisibility(View.GONE);
        } else {
            textViewNoAccess.setVisibility(View.GONE);
            mainContent.setVisibility(View.VISIBLE);
            loadSharedHabits();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadSharedHabits() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            android.util.Log.d("SocialActivity", "Current user is null!");
            return;
        }

        String userId = currentUser.getUid();

        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists() && document.contains("nickname")) {
                        String currentUsername = document.getString("nickname");
                        android.util.Log.d("SocialActivity", "Current Firestore username: " + currentUsername);

                        getSharedPreferences("UserPreferences", MODE_PRIVATE)
                                .edit()
                                .putString("username", currentUsername)
                                .apply();

                        FirebaseFirestore.getInstance()
                                .collection("sharedHabits")
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    sharedHabitList.clear();
                                    mySharedHabitList.clear();

                                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                        SharedHabit habit = doc.toObject(SharedHabit.class);

                                        android.util.Log.d("SocialActivity", "Checking habit shared by: " + habit.sharedByUsername);

                                        if (habit.sharedByUsername.equals(currentUsername)) {
                                            android.util.Log.d("SocialActivity", "→ ADDED to MY list: " + habit.habitName);
                                            mySharedHabitList.add(habit);
                                        } else {
                                            android.util.Log.d("SocialActivity", "→ ADDED to OTHER list: " + habit.habitName);
                                            sharedHabitList.add(habit);
                                        }
                                    }

                                    myAdapter.notifyDataSetChanged();
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e("SocialActivity", "Failed to load shared habits", e);
                                });

                    } else {
                        android.util.Log.w("SocialActivity", "User document not found or missing nickname");
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("SocialActivity", "Failed to fetch user nickname", e);
                });
    }

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkAccessConditions();
        }
    };

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        android.net.Network network = cm.getActiveNetwork();
        if (network == null) return false;

        android.net.NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        return capabilities != null && (
                capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET));
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
        checkAccessConditions();
        applyFullScreenMode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkReceiver);
    }
}
