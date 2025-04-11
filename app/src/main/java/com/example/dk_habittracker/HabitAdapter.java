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

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private final List<Habit> habitList;
    private final Context context;
    private final DBHelper dbHelper;
    private final String selectedDate;

    public HabitAdapter(Context context, List<Habit> habitList, String selectedDate) {
        this.context = context;
        this.habitList = habitList;
        this.dbHelper = new DBHelper(context);
        this.selectedDate = selectedDate;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.habit_list_item, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habitList.get(position);
        holder.textHabitName.setText(habit.getName());

        if (!habit.getDescription().isEmpty()) {
            holder.textHabitDescription.setText(habit.getDescription());
            holder.textHabitDescription.setVisibility(View.VISIBLE);
        } else {
            holder.textHabitDescription.setVisibility(View.GONE);
        }

        int progress = dbHelper.getProgressForPeriod(habit.getId(), habit.getGoalPeriod(), selectedDate);

        String currentLanguage = context.getResources().getConfiguration().getLocales().get(0).getLanguage();

        String translatedGoalPeriod;
        switch (habit.getGoalPeriod()) {
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
                translatedGoalPeriod = habit.getGoalPeriod();
        }

        String translatedMeasurementUnit;
        switch (habit.getMeasurementUnit()) {
            case "Times":
                translatedMeasurementUnit = currentLanguage.equals("sk") ? "Krát" : "Times";
                break;
            case "Count":
                translatedMeasurementUnit = currentLanguage.equals("sk") ? "Počet" : "Count";
                break;
            case "Steps":
                translatedMeasurementUnit = currentLanguage.equals("sk") ? "Kroky" : "Steps";
                break;
            case "Reps":
                translatedMeasurementUnit = currentLanguage.equals("sk") ? "Opakovania" : "Reps";
                break;
            case "Calories":
                translatedMeasurementUnit = currentLanguage.equals("sk") ? "Kalórie" : "Calories";
                break;
            case "Cups":
                translatedMeasurementUnit = currentLanguage.equals("sk") ? "Poháre" : "Cups";
                break;
            case "m":
                translatedMeasurementUnit = "m";
                break;
            case "km":
                translatedMeasurementUnit = "km";
                break;
            case "sec":
                translatedMeasurementUnit = currentLanguage.equals("sk") ? "sek" : "sec";
                break;
            case "min":
                translatedMeasurementUnit = "min";
                break;
            case "hr":
                translatedMeasurementUnit = currentLanguage.equals("sk") ? "hoď" : "hr";
                break;
            case "ml":
                translatedMeasurementUnit = "ml";
                break;
            case "g":
                translatedMeasurementUnit = "g";
                break;
            case "mg":
                translatedMeasurementUnit = "mg";
                break;
            default:
                translatedMeasurementUnit = habit.getMeasurementUnit();
        }

        String goalText;
        if (habit.getHabitType().equals("Quit")) {
            goalText = progress + " / " + habit.getGoal() + " " + translatedMeasurementUnit + " (MAX) \n/ " + translatedGoalPeriod;
        } else {
            goalText = progress + " / " + habit.getGoal() + " " + translatedMeasurementUnit + " \n/ " + translatedGoalPeriod;
        }
        holder.textHabitGoal.setText(goalText);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HabitDetailActivity.class);
            intent.putExtra("habit_id", habit.getId());
            intent.putExtra("selected_date", selectedDate);
            context.startActivity(intent);

            if (context instanceof MyHabitsActivity) {
                ((MyHabitsActivity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                ((MyHabitsActivity) context).finish();
            } else if (context instanceof DailyStatisticsActivity) {
                ((DailyStatisticsActivity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                ((DailyStatisticsActivity) context).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView textHabitName, textHabitDescription, textHabitGoal;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            textHabitName = itemView.findViewById(R.id.textHabitName);
            textHabitDescription = itemView.findViewById(R.id.textHabitDescription);
            textHabitGoal = itemView.findViewById(R.id.textHabitGoal);
        }
    }
}
