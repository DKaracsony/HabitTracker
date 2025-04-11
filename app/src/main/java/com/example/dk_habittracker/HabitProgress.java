package com.example.dk_habittracker;

/** @noinspection ALL*/

public class HabitProgress {
    private int id;
    private int habitId;
    private String date;
    private int progress;

    public HabitProgress(int id, int habitId, String date, int progress) {
        this.id = id;
        this.habitId = habitId;
        this.date = date;
        this.progress = progress;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getHabitId() { return habitId; }
    public void setHabitId(int habitId) { this.habitId = habitId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
}
