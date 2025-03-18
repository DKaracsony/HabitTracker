package com.example.dk_habittracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    //DB Info
    private static final String DATABASE_NAME = "habits.db";
    private static final int DATABASE_VERSION = 1;

    //Habits Table
    private static final String TABLE_HABITS = "habits";
    private static final String COLUMN_HABIT_ID = "id";
    private static final String COLUMN_HABIT_NAME = "name";
    private static final String COLUMN_HABIT_DESCRIPTION = "description";
    private static final String COLUMN_HABIT_GOAL = "goal";
    private static final String COLUMN_HABIT_MEASUREMENT = "measurement_unit";
    private static final String COLUMN_HABIT_PERIOD = "goal_period";
    private static final String COLUMN_HABIT_TYPE = "habit_type";
    private static final String COLUMN_HABIT_CREATED_AT = "created_at";

    //Habit Progress Table
    private static final String TABLE_HABIT_PROGRESS = "habit_progress";
    private static final String COLUMN_PROGRESS_ID = "id";
    private static final String COLUMN_PROGRESS_HABIT_ID = "habit_id";
    private static final String COLUMN_PROGRESS_DATE = "date";
    private static final String COLUMN_PROGRESS_VALUE = "progress";
    private static final String COLUMN_PROGRESS_COMPLETED = "completed";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Habits table
        String createHabitsTable = "CREATE TABLE " + TABLE_HABITS + " (" +
                COLUMN_HABIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_HABIT_NAME + " TEXT NOT NULL, " +
                COLUMN_HABIT_DESCRIPTION + " TEXT, " +
                COLUMN_HABIT_GOAL + " INTEGER NOT NULL, " +
                COLUMN_HABIT_MEASUREMENT + " TEXT NOT NULL, " +
                COLUMN_HABIT_PERIOD + " TEXT NOT NULL, " +
                COLUMN_HABIT_TYPE + " TEXT NOT NULL, " +
                COLUMN_HABIT_CREATED_AT + " TEXT NOT NULL)";

        // Create Habit Progress table
        String createProgressTable = "CREATE TABLE " + TABLE_HABIT_PROGRESS + " (" +
                COLUMN_PROGRESS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PROGRESS_HABIT_ID + " INTEGER NOT NULL, " +
                COLUMN_PROGRESS_DATE + " TEXT NOT NULL, " +
                COLUMN_PROGRESS_VALUE + " INTEGER DEFAULT 0, " +
                COLUMN_PROGRESS_COMPLETED + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY(" + COLUMN_PROGRESS_HABIT_ID + ") REFERENCES " + TABLE_HABITS + "(" + COLUMN_HABIT_ID + "))";

        db.execSQL(createHabitsTable);
        db.execSQL(createProgressTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABIT_PROGRESS);
        onCreate(db);
    }

    //Insert new habit
    public long insertHabit(Habit habit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_NAME, habit.getName());
        values.put(COLUMN_HABIT_DESCRIPTION, habit.getDescription());
        values.put(COLUMN_HABIT_GOAL, habit.getGoal());
        values.put(COLUMN_HABIT_MEASUREMENT, habit.getMeasurementUnit());
        values.put(COLUMN_HABIT_PERIOD, habit.getGoalPeriod());
        values.put(COLUMN_HABIT_TYPE, habit.getHabitType());
        values.put(COLUMN_HABIT_CREATED_AT, habit.getCreatedAt());

        long id = db.insert(TABLE_HABITS, null, values);
        db.close();
        return id;
    }

    //Insert Habit Progress
    public long insertHabitProgress(HabitProgress progress) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROGRESS_HABIT_ID, progress.getHabitId());
        values.put(COLUMN_PROGRESS_DATE, progress.getDate());
        values.put(COLUMN_PROGRESS_VALUE, progress.getProgress());
        values.put(COLUMN_PROGRESS_COMPLETED, progress.isCompleted() ? 1 : 0);

        long id = db.insert(TABLE_HABIT_PROGRESS, null, values);
        db.close();
        return id;
    }

    //Fetch All Habits
    public List<Habit> getAllHabits() {
        List<Habit> habitList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HABITS, null);

        if (cursor.moveToFirst()) {
            do {
                Habit habit = new Habit(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7)
                );
                habitList.add(habit);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return habitList;
    }

    //For MyHabitsActivity List
    public int getProgressForPeriod(int habitId, String goalPeriod, String selectedDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] periodDates = getStartAndEndDate(goalPeriod, selectedDate);
        String startDate = periodDates[0];
        String endDate = periodDates[1];

        Cursor cursor = db.rawQuery("SELECT COALESCE(SUM(progress), 0) FROM " + TABLE_HABIT_PROGRESS +
                        " WHERE habit_id = ? AND date BETWEEN ? AND ?",
                new String[]{String.valueOf(habitId), startDate, endDate});

        int progress = 0;
        if (cursor.moveToFirst()) {
            progress = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return progress;
    }

    //Date Interval Definition
    private String[] getStartAndEndDate(String goalPeriod, String selectedDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        try {
            calendar.setTime(sdf.parse(selectedDate));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String startDate = selectedDate;
        String endDate = selectedDate;

        if (goalPeriod.equalsIgnoreCase("Weekly") || goalPeriod.equalsIgnoreCase("Týždenne")) {
            // Move to Monday of the current week
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            startDate = sdf.format(calendar.getTime());

            // Move to Sunday of the same week
            calendar.add(Calendar.DAY_OF_WEEK, 6);
            endDate = sdf.format(calendar.getTime());
        } else if (goalPeriod.equalsIgnoreCase("Monthly") || goalPeriod.equalsIgnoreCase("Mesačne")) {
            // Move to the first day of the current month
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            startDate = sdf.format(calendar.getTime());

            // Move to the last day of the month
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            endDate = sdf.format(calendar.getTime());
        }
        return new String[]{startDate, endDate};
    }

    //Not Needed ATM
    public List<HabitProgress> getProgressByDate(String date) {
        List<HabitProgress> progressList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HABIT_PROGRESS + " WHERE " + COLUMN_PROGRESS_DATE + " = ?", new String[]{date});

        if (cursor.moveToFirst()) {
            do {
                HabitProgress progress = new HabitProgress(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getInt(4) == 1
                );
                progressList.add(progress);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return progressList;
    }

    //Leave in for testing
    public void deleteAllHabits() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_HABITS);
        db.execSQL("DELETE FROM " + TABLE_HABIT_PROGRESS);
        db.close();
    }

    // Get habit by ID
    public Habit getHabitById(int habitId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HABITS + " WHERE id = ?", new String[]{String.valueOf(habitId)});

        if (cursor.moveToFirst()) {
            Habit habit = new Habit(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7)
            );
            cursor.close();
            return habit;
        }
        cursor.close();
        return null;
    }

    // Update habit progress
    public void updateHabitProgress(int habitId, String date, int newProgress) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROGRESS_VALUE, newProgress);

        int rowsUpdated = db.update(TABLE_HABIT_PROGRESS, values,
                COLUMN_PROGRESS_HABIT_ID + " = ? AND " + COLUMN_PROGRESS_DATE + " = ?",
                new String[]{String.valueOf(habitId), date});

        // If no existing progress, insert a new entry
        if (rowsUpdated == 0) {
            values.put(COLUMN_PROGRESS_HABIT_ID, habitId);
            values.put(COLUMN_PROGRESS_DATE, date);
            db.insert(TABLE_HABIT_PROGRESS, null, values);
        }

        db.close();
    }

    // Delete habit and associated progress
    public void deleteHabit(int habitId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HABIT_PROGRESS, COLUMN_PROGRESS_HABIT_ID + " = ?", new String[]{String.valueOf(habitId)});
        db.delete(TABLE_HABITS, COLUMN_HABIT_ID + " = ?", new String[]{String.valueOf(habitId)});
        db.close();
    }

    public void deleteHabitProgress(int habitId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HABIT_PROGRESS, COLUMN_PROGRESS_HABIT_ID + " = ?", new String[]{String.valueOf(habitId)});
        db.close();
    }

    //EditHabitActivity Editing
    public void updateHabit(Habit habit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HABIT_NAME, habit.getName());
        values.put(COLUMN_HABIT_DESCRIPTION, habit.getDescription());
        values.put(COLUMN_HABIT_GOAL, habit.getGoal());
        values.put(COLUMN_HABIT_MEASUREMENT, habit.getMeasurementUnit());
        values.put(COLUMN_HABIT_PERIOD, habit.getGoalPeriod());
        values.put(COLUMN_HABIT_TYPE, habit.getHabitType());

        db.update(TABLE_HABITS, values, COLUMN_HABIT_ID + " = ?", new String[]{String.valueOf(habit.getId())});
        db.close();
    }
}
