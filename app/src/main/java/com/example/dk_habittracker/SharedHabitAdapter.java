package com.example.dk_habittracker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SharedHabitAdapter extends RecyclerView.Adapter<SharedHabitAdapter.SharedHabitViewHolder> {

    private final List<SharedHabit> habitList;

    public SharedHabitAdapter(List<SharedHabit> habitList) {
        this.habitList = habitList;
    }

    @NonNull
    @Override
    public SharedHabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shared_habit, parent, false);
        return new SharedHabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SharedHabitViewHolder holder, int position) {
        SharedHabit habit = habitList.get(position);

        holder.textHabitName.setText(habit.habitName);

        if (!habit.description.isEmpty()) {
            holder.textHabitDescription.setVisibility(View.VISIBLE);
            holder.textHabitDescription.setText(habit.description);
        } else {
            holder.textHabitDescription.setVisibility(View.GONE);
        }

        String currentLanguage = holder.itemView.getContext().getResources().getConfiguration().getLocales().get(0).getLanguage();

        String translatedGoalPeriod;
        switch (habit.goalPeriod) {
            case "Daily":
                translatedGoalPeriod = currentLanguage.equals("sk") ? "Denne" : "Daily";
                break;
            case "Weekly":
                translatedGoalPeriod = currentLanguage.equals("sk") ? "Týždenne" : "Weekly";
                break;
            case "Monthly":
                translatedGoalPeriod = currentLanguage.equals("sk") ? "Mesačne" : "Monthly";
                break;
            default:
                translatedGoalPeriod = habit.goalPeriod;
        }

        String translatedMeasurement;
        switch (habit.measurement) {
            case "Times":
                translatedMeasurement = currentLanguage.equals("sk") ? "Krát" : "Times";
                break;
            case "Count":
                translatedMeasurement = currentLanguage.equals("sk") ? "Počet" : "Count";
                break;
            case "Steps":
                translatedMeasurement = currentLanguage.equals("sk") ? "Kroky" : "Steps";
                break;
            case "Reps":
                translatedMeasurement = currentLanguage.equals("sk") ? "Opakovania" : "Reps";
                break;
            case "Calories":
                translatedMeasurement = currentLanguage.equals("sk") ? "Kalórie" : "Calories";
                break;
            case "Cups":
                translatedMeasurement = currentLanguage.equals("sk") ? "Poháre" : "Cups";
                break;
            case "sec":
                translatedMeasurement = currentLanguage.equals("sk") ? "sek" : "sec";
                break;
            case "hr":
                translatedMeasurement = currentLanguage.equals("sk") ? "hoď" : "hr";
                break;
            default:
                translatedMeasurement = habit.measurement;
        }

        String goalText;
        if (habit.habitType.equals("Quit")) {
            goalText = habit.goalValue + " " + translatedMeasurement + " (MAX)\n/ " + translatedGoalPeriod;
        } else {
            goalText = habit.goalValue + " " + translatedMeasurement + "\n/ " + translatedGoalPeriod;
        }

        holder.textHabitGoal.setText(goalText);
        holder.textSharedBy.setText(holder.itemView.getContext().getString(R.string.shared_by, habit.sharedByUsername));

        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, SharedHabitDetailActivity.class);
            intent.putExtra("habit", habit);
            intent.putExtra("isMyHabit", habit.sharedByUsername.equals(getCurrentUsername(context)));

            context.startActivity(intent);

            if (context instanceof SocialActivity) {
                ((SocialActivity) context).finish();
                ((SocialActivity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    private String getCurrentUsername(Context context) {
        return context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
                .getString("username", "");
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public static class SharedHabitViewHolder extends RecyclerView.ViewHolder {
        TextView textHabitName, textHabitDescription, textHabitGoal, textSharedBy;

        public SharedHabitViewHolder(@NonNull View itemView) {
            super(itemView);
            textHabitName = itemView.findViewById(R.id.textHabitName);
            textHabitDescription = itemView.findViewById(R.id.textHabitDescription);
            textHabitGoal = itemView.findViewById(R.id.textHabitGoal);
            textSharedBy = itemView.findViewById(R.id.textSharedBy);
        }
    }
}