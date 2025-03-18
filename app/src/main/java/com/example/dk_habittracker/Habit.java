package com.example.dk_habittracker;

public class Habit {
    private int id;
    private String name;
    private String description;
    private int goal;
    private String measurementUnit;
    private String goalPeriod;
    private String habitType;
    private String createdAt;

    // Constructor
    public Habit(int id, String name, String description, int goal, String measurementUnit, String goalPeriod, String habitType, String createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.goal = goal;
        this.measurementUnit = measurementUnit;
        this.goalPeriod = goalPeriod;
        this.habitType = habitType;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getGoal() { return goal; }
    public void setGoal(int goal) { this.goal = goal; }

    public String getMeasurementUnit() { return measurementUnit; }
    public void setMeasurementUnit(String measurementUnit) { this.measurementUnit = measurementUnit; }

    public String getGoalPeriod() { return goalPeriod; }
    public void setGoalPeriod(String goalPeriod) { this.goalPeriod = goalPeriod; }

    public String getHabitType() { return habitType; }
    public void setHabitType(String habitType) { this.habitType = habitType; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
