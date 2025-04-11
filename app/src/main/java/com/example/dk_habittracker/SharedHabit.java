package com.example.dk_habittracker;

import java.io.Serializable;

public class SharedHabit implements Serializable {
    public String habitName;
    public String description;
    public int goalValue;
    public String measurement;
    public String goalPeriod;
    public String habitType;
    public String sharedByUsername;

    public SharedHabit() {}

    public SharedHabit(String habitName, String description, int goalValue, String measurement, String goalPeriod, String habitType, String sharedByUsername) {
        this.habitName = habitName;
        this.description = description;
        this.goalValue = goalValue;
        this.measurement = measurement;
        this.goalPeriod = goalPeriod;
        this.habitType = habitType;
        this.sharedByUsername = sharedByUsername;
    }
}
