package com.example.dk_habittracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FollowedHabitAdapter extends RecyclerView.Adapter<FollowedHabitAdapter.ViewHolder> {

    private final Context context;
    private final List<Habit> habitList;

    public FollowedHabitAdapter(Context context, List<Habit> habitList) {
        this.context = context;
        this.habitList = habitList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.followed_habit_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habitList.get(position);
        holder.textViewHabitName.setText(habit.getName());
        holder.textViewHabitDescription.setText(habit.getDescription());

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        layoutParams.bottomMargin = 16;
        holder.itemView.setLayoutParams(layoutParams);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HabitStatisticsActivity.class);
            intent.putExtra("habit_id", habit.getId());
            intent.putExtra("habit_name", habit.getName());
            intent.putExtra("habit_description", habit.getDescription());
            context.startActivity(intent);

            if (context instanceof Activity) {
                ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                ((Activity) context).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewHabitName, textViewHabitDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewHabitName = itemView.findViewById(R.id.textViewHabitName);
            textViewHabitDescription = itemView.findViewById(R.id.textViewHabitDescription);
        }
    }
}
