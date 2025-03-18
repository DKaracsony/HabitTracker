package com.example.dk_habittracker;

public class HabitProgress {
    private int id;
    private int habitId;
    private String date;
    private int progress;
    private boolean isCompleted;

    // Constructor
    public HabitProgress(int id, int habitId, String date, int progress, boolean isCompleted) {
        this.id = id;
        this.habitId = habitId;
        this.date = date;
        this.progress = progress;
        this.isCompleted = isCompleted;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHabitId() { return habitId; }
    public void setHabitId(int habitId) { this.habitId = habitId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
