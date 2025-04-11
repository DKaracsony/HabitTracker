package com.example.dk_habittracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "habits.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_HABITS = "habits";
    private static final String COLUMN_HABIT_ID = "id";
    private static final String COLUMN_HABIT_NAME = "name";
    private static final String COLUMN_HABIT_DESCRIPTION = "description";
    private static final String COLUMN_HABIT_GOAL = "goal";
    private static final String COLUMN_HABIT_MEASUREMENT = "measurement_unit";
    private static final String COLUMN_HABIT_PERIOD = "goal_period";
    private static final String COLUMN_HABIT_TYPE = "habit_type";
    private static final String COLUMN_HABIT_CREATED_AT = "created_at";

    private static final String TABLE_HABIT_PROGRESS = "habit_progress";
    private static final String COLUMN_PROGRESS_ID = "id";
    private static final String COLUMN_PROGRESS_HABIT_ID = "habit_id";
    private static final String COLUMN_PROGRESS_DATE = "date";
    private static final String COLUMN_PROGRESS_VALUE = "progress";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createHabitsTable = "CREATE TABLE " + TABLE_HABITS + " (" +
                COLUMN_HABIT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_HABIT_NAME + " TEXT NOT NULL, " +
                COLUMN_HABIT_DESCRIPTION + " TEXT, " +
                COLUMN_HABIT_GOAL + " INTEGER NOT NULL, " +
                COLUMN_HABIT_MEASUREMENT + " TEXT NOT NULL, " +
                COLUMN_HABIT_PERIOD + " TEXT NOT NULL, " +
                COLUMN_HABIT_TYPE + " TEXT NOT NULL, " +
                COLUMN_HABIT_CREATED_AT + " TEXT NOT NULL)";

        String createProgressTable = "CREATE TABLE " + TABLE_HABIT_PROGRESS + " (" +
                COLUMN_PROGRESS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PROGRESS_HABIT_ID + " INTEGER NOT NULL, " +
                COLUMN_PROGRESS_DATE + " TEXT NOT NULL, " +
                COLUMN_PROGRESS_VALUE + " INTEGER DEFAULT 0, " +
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

    public int getProgressForExactDate(int habitId, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(progress, 0) FROM " + TABLE_HABIT_PROGRESS + " WHERE habit_id = ? AND date = ?",
                new String[]{String.valueOf(habitId), date}
        );

        int progress = 0;
        if (cursor.moveToFirst()) {
            progress = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return progress;
    }

    private String[] getStartAndEndDate(String goalPeriod, String selectedDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        try {
            Date parsedDate = sdf.parse(selectedDate);
            if (parsedDate != null) {
                calendar.setTime(parsedDate);
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Error parsing date: " + selectedDate, e);
        }

        String startDate = selectedDate;
        String endDate = selectedDate;

        if (goalPeriod.equalsIgnoreCase("Weekly") || goalPeriod.equalsIgnoreCase("Týždenne")) {
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            startDate = sdf.format(calendar.getTime());

            calendar.add(Calendar.DAY_OF_WEEK, 6);
            endDate = sdf.format(calendar.getTime());
        } else if (goalPeriod.equalsIgnoreCase("Monthly") || goalPeriod.equalsIgnoreCase("Mesačne")) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            startDate = sdf.format(calendar.getTime());

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            endDate = sdf.format(calendar.getTime());
        }

        return new String[]{startDate, endDate};
    }

    public void deleteAllHabits() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_HABITS);
        db.execSQL("DELETE FROM " + TABLE_HABIT_PROGRESS);
        db.close();
    }

    public void resetAllProgress() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_HABIT_PROGRESS);
        db.close();
    }

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

    public void updateHabitProgress(int habitId, String date, int newProgress) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROGRESS_VALUE, newProgress);

        int rowsUpdated = db.update(TABLE_HABIT_PROGRESS, values,
                COLUMN_PROGRESS_HABIT_ID + " = ? AND " + COLUMN_PROGRESS_DATE + " = ?",
                new String[]{String.valueOf(habitId), date});

        if (rowsUpdated == 0) {
            values.put(COLUMN_PROGRESS_HABIT_ID, habitId);
            values.put(COLUMN_PROGRESS_DATE, date);
            db.insert(TABLE_HABIT_PROGRESS, null, values);
        }

        db.close();
    }

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

    public List<HabitProgress> getHabitProgressForCurrentMonth(int habitId, Calendar inputMonth) {
        List<HabitProgress> progressList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Habit habit = getHabitById(habitId);
        if (habit == null) return progressList;

        String goalPeriod = habit.getGoalPeriod();

        Calendar calendar = (Calendar) inputMonth.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int cumulativeMonthly = 0;

        int currentWeek = -1;
        int cumulativeWeekly = 0;

        for (int i = 0; i < daysInMonth; i++) {
            String date = sdf.format(calendar.getTime());
            int dayProgress = getProgressForDate(db, habitId, date);

            int displayValue = 0;

            if (goalPeriod.equalsIgnoreCase("Daily") || goalPeriod.equalsIgnoreCase("Denne")) {
                displayValue = dayProgress;

            } else if (goalPeriod.equalsIgnoreCase("Weekly") || goalPeriod.equalsIgnoreCase("Týždenne")) {
                int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);

                if (weekOfYear != currentWeek) {
                    currentWeek = weekOfYear;
                    cumulativeWeekly = 0;

                    Calendar tracker = (Calendar) calendar.clone();
                    tracker.setFirstDayOfWeek(Calendar.MONDAY);
                    tracker.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

                    for (int d = 0; d < 7; d++) {
                        if (tracker.after(calendar)) break;
                        String weekDate = sdf.format(tracker.getTime());
                        cumulativeWeekly += getProgressForDate(db, habitId, weekDate);
                        tracker.add(Calendar.DAY_OF_MONTH, 1);
                    }
                } else {
                    cumulativeWeekly += dayProgress;
                }

                displayValue = cumulativeWeekly;

            } else if (goalPeriod.equalsIgnoreCase("Monthly") || goalPeriod.equalsIgnoreCase("Mesačne")) {
                cumulativeMonthly += dayProgress;
                displayValue = cumulativeMonthly;

            }

            progressList.add(new HabitProgress(-1, habitId, date, displayValue));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        db.close();
        return progressList;
    }

    public List<Integer> getWeeklyCompletionRates(int habitId, Calendar weekStart) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Integer> completionRates = new ArrayList<>();

        int goal = 1;
        String habitType = "Build";
        String goalPeriod = "daily";

        Cursor habitCursor = db.rawQuery(
                "SELECT " + COLUMN_HABIT_GOAL + ", " + COLUMN_HABIT_TYPE + ", " + COLUMN_HABIT_PERIOD +
                        " FROM " + TABLE_HABITS + " WHERE " + COLUMN_HABIT_ID + " = ?",
                new String[]{String.valueOf(habitId)}
        );

        if (habitCursor.moveToFirst()) {
            goal = habitCursor.getInt(0);
            habitType = habitCursor.getString(1);
            goalPeriod = habitCursor.getString(2);
        }
        habitCursor.close();

        Calendar calendar = (Calendar) weekStart.clone();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Map<String, Integer> monthlyProgressMap = new HashMap<>();

        Set<Integer> encounteredMonths = new HashSet<>();
        for (int i = 0; i < 7; i++) {
            int month = calendar.get(Calendar.MONTH);
            if (!encounteredMonths.contains(month)) {
                encounteredMonths.add(month);
                Calendar monthStart = (Calendar) calendar.clone();
                monthStart.set(Calendar.DAY_OF_MONTH, 1);
                int daysInMonth = monthStart.getActualMaximum(Calendar.DAY_OF_MONTH);

                for (int d = 0; d < daysInMonth; d++) {
                    String dateStr = sdf.format(monthStart.getTime());
                    monthlyProgressMap.put(dateStr, getProgressForDate(db, habitId, dateStr));
                    monthStart.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        calendar = (Calendar) weekStart.clone();

        int cumulativeWeekly = 0;

        for (int i = 0; i < 7; i++) {
            String currentDateStr = sdf.format(calendar.getTime());
            int percentage;

            if (goalPeriod.equalsIgnoreCase("daily") || goalPeriod.equalsIgnoreCase("denne")) {
                int dailyProgress = getProgressForDate(db, habitId, currentDateStr);
                if (habitType.equalsIgnoreCase("Quit")) {
                    percentage = Math.max(100 - (int) ((dailyProgress / (float) goal) * 100), 0);
                } else {
                    percentage = Math.min((int) ((dailyProgress / (float) goal) * 100), 100);
                }

            } else if (goalPeriod.equalsIgnoreCase("weekly") || goalPeriod.equalsIgnoreCase("týždenne")) {
                int dailyProgress = getProgressForDate(db, habitId, currentDateStr);
                cumulativeWeekly += dailyProgress;

                if (habitType.equalsIgnoreCase("Quit")) {
                    percentage = Math.max(100 - (int) ((cumulativeWeekly / (float) goal) * 100), 0);
                } else {
                    percentage = Math.min((int) ((cumulativeWeekly / (float) goal) * 100), 100);
                }

            } else {
                int cumulativeMonthly = 0;

                Calendar monthStart = (Calendar) calendar.clone();
                monthStart.set(Calendar.DAY_OF_MONTH, 1);

                Calendar tmp = (Calendar) monthStart.clone();
                while (!tmp.after(calendar)) {
                    String key = sdf.format(tmp.getTime());
                    Integer value = monthlyProgressMap.getOrDefault(key, 0);
                    cumulativeMonthly += (value != null ? value : 0);
                    tmp.add(Calendar.DAY_OF_MONTH, 1);
                }

                if (habitType.equalsIgnoreCase("Quit")) {
                    percentage = Math.max(100 - (int) ((cumulativeMonthly / (float) goal) * 100), 0);
                } else {
                    percentage = Math.min((int) ((cumulativeMonthly / (float) goal) * 100), 100);
                }
            }

            completionRates.add(percentage);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        db.close();
        return completionRates;
    }

    public int getLongestStreak(int habitId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Habit habit = getHabitById(habitId);
        if (habit == null) return 0;

        String goalPeriod = habit.getGoalPeriod();
        String habitType = habit.getHabitType();
        int goal = habit.getGoal();

        int currentStreak = 0;
        int longestStreak = 0;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar createdAtCal = Calendar.getInstance();
        try {
            Date parsedDate = sdf.parse(habit.getCreatedAt());
            if (parsedDate == null) {
                return 0;
            }
            createdAtCal.setTime(parsedDate);
        } catch (ParseException e) {
            Log.e("DBHelper", "Failed to parse habit creation date", e);
            return 0;
        }

        while (true) {
            String startDate = "", endDate = "";

            switch (goalPeriod) {
                case "Daily":
                    startDate = sdf.format(calendar.getTime());
                    endDate = startDate;
                    break;

                case "Weekly":
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    int daysBackToMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - Calendar.MONDAY);
                    calendar.add(Calendar.DAY_OF_YEAR, -daysBackToMonday);
                    startDate = sdf.format(calendar.getTime());

                    calendar.add(Calendar.DAY_OF_YEAR, 6);
                    endDate = sdf.format(calendar.getTime());

                    calendar.add(Calendar.DAY_OF_YEAR, -6);
                    break;

                case "Monthly":
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    startDate = sdf.format(calendar.getTime());
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    endDate = sdf.format(calendar.getTime());
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    break;
            }

            try {
                Date currentEndDate = sdf.parse(endDate);
                if (currentEndDate == null || currentEndDate.before(createdAtCal.getTime())) {
                    Log.d("LongestStreak", "Reached before habit creation. Stopping.");
                    break;
                }
            } catch (ParseException e) {
                Log.e("DBHelper", "Failed to parse endDate in getLongestStreak", e);
                break;
            }

            Log.d("LongestStreak", "Checking period: " + startDate + " to " + endDate);

            Cursor cursor = db.rawQuery(
                    "SELECT SUM(" + COLUMN_PROGRESS_VALUE + ") FROM " + TABLE_HABIT_PROGRESS +
                            " WHERE " + COLUMN_PROGRESS_HABIT_ID + " = ? AND " + COLUMN_PROGRESS_DATE +
                            " BETWEEN ? AND ?",
                    new String[]{String.valueOf(habitId), startDate, endDate}
            );

            int progress = 0;
            boolean metGoal = false;

            if (cursor.moveToFirst()) {
                if (!cursor.isNull(0)) {
                    progress = cursor.getInt(0);
                    metGoal = habitType.equals("Build") ? progress >= goal : progress <= goal;
                    Log.d("LongestStreak", "Progress: " + progress + ", Goal: " + goal);
                } else {
                    metGoal = habitType.equals("Quit");
                    Log.d("LongestStreak", "No data. Met goal by default for Quit habit? " + metGoal);
                }
            } else {
                Log.d("LongestStreak", "Cursor returned no rows.");
            }

            cursor.close();
            Log.d("LongestStreak", "Goal Met: " + metGoal);

            if (metGoal) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
                Log.d("LongestStreak", "Current streak increased: " + currentStreak + ", Longest so far: " + longestStreak);
            } else {
                currentStreak = 0;
                Log.d("LongestStreak", "Streak broken. Reset current streak.");
            }

            // Go to previous period
            switch (goalPeriod) {
                case "Daily": calendar.add(Calendar.DAY_OF_YEAR, -1); break;
                case "Weekly": calendar.add(Calendar.WEEK_OF_YEAR, -1); break;
                case "Monthly": calendar.add(Calendar.MONTH, -1); break;
            }
        }

        Log.d("LongestStreak", "Final Longest Streak = " + longestStreak);
        return longestStreak;
    }

    public String getHabitStrengthInfo(int habitId, String language) {
        SQLiteDatabase db = this.getReadableDatabase();
        Habit habit = getHabitById(habitId);
        if (habit == null) return "";

        String goalPeriod = habit.getGoalPeriod();
        String habitType = habit.getHabitType();
        int goal = habit.getGoal();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar createdAtCal = Calendar.getInstance();
        try {
            createdAtCal.setTime(sdf.parse(habit.getCreatedAt()));
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }

        int totalPeriods = 0;
        int completedPeriods = 0;

        while (true) {
            String startDate = "", endDate = "";

            switch (goalPeriod) {
                case "Daily":
                    startDate = sdf.format(calendar.getTime());
                    endDate = startDate;
                    break;

                case "Weekly":
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    int daysBackToMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - Calendar.MONDAY);
                    calendar.add(Calendar.DAY_OF_YEAR, -daysBackToMonday);
                    startDate = sdf.format(calendar.getTime());

                    calendar.add(Calendar.DAY_OF_YEAR, 6);
                    endDate = sdf.format(calendar.getTime());

                    calendar.add(Calendar.DAY_OF_YEAR, -6);
                    break;

                case "Monthly":
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    startDate = sdf.format(calendar.getTime());
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    endDate = sdf.format(calendar.getTime());
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    break;
            }

            try {
                Date periodEnd = sdf.parse(endDate);
                if (periodEnd.before(createdAtCal.getTime())) {
                    Log.d("HabitStrength", "Reached before habit creation. Stopping.");
                    break;
                }
            } catch (ParseException e) {
                e.printStackTrace();
                break;
            }

            Log.d("HabitStrength", "Including period: " + startDate + " to " + endDate);
            totalPeriods++;
            Log.d("HabitStrength", "Checking period: " + startDate + " to " + endDate);

            Cursor cursor = db.rawQuery(
                    "SELECT SUM(" + COLUMN_PROGRESS_VALUE + ") FROM " + TABLE_HABIT_PROGRESS +
                            " WHERE " + COLUMN_PROGRESS_HABIT_ID + " = ? AND " + COLUMN_PROGRESS_DATE +
                            " BETWEEN ? AND ?",
                    new String[]{String.valueOf(habitId), startDate, endDate}
            );

            int progress = 0;
            boolean metGoal = false;

            if (cursor.moveToFirst()) {
                if (!cursor.isNull(0)) {
                    progress = cursor.getInt(0);
                    Log.d("HabitStrength", "Progress: " + progress + ", Goal: " + goal);

                    if (habitType.equals("Build")) {
                        metGoal = progress >= goal;
                    } else {
                        metGoal = progress <= goal;
                    }
                } else if (habitType.equals("Quit")) {
                    Log.d("HabitStrength", "No data for Quit habit. Marking as successful.");
                    metGoal = true;
                }
            }
            cursor.close();

            Log.d("HabitStrength", "Goal Met: " + metGoal);

            if (metGoal) completedPeriods++;

            switch (goalPeriod) {
                case "Daily": calendar.add(Calendar.DAY_OF_YEAR, -1); break;
                case "Weekly": calendar.add(Calendar.WEEK_OF_YEAR, -1); break;
                case "Monthly": calendar.add(Calendar.MONTH, -1); break;
            }
        }

        int percentage = (totalPeriods == 0) ? 0 : Math.round((completedPeriods * 100f) / totalPeriods);
        Log.d("HabitStrength", "Habit ID: " + habitId + ", Completed: " + completedPeriods + ", Total: " + totalPeriods + ", %: " + percentage);

        String phrase;
        if (language.equals("sk")) {
            if (percentage >= 90) phrase = "🔥 Skvelá práca!";
            else if (percentage >= 70) phrase = "💪 Výborne, len tak ďalej!";
            else if (percentage >= 50) phrase = "🙂 Dobré úsilie!";
            else if (percentage >= 30) phrase = "🌀 Stále je čo zlepšovať.";
            else phrase = "💤 Potrebuješ motiváciu.";
        } else {
            if (percentage >= 90) phrase = "🔥 Amazing work!";
            else if (percentage >= 70) phrase = "💪 Great effort!";
            else if (percentage >= 50) phrase = "🙂 Keep pushing!";
            else if (percentage >= 30) phrase = "🌀 Room to grow.";
            else phrase = "💤 Stay motivated!";
        }
        return percentage + "% " + phrase;
    }

    private int getProgressInRange(SQLiteDatabase db, int habitId, String startDate, String endDate) {
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COLUMN_PROGRESS_VALUE + ") FROM " + TABLE_HABIT_PROGRESS +
                        " WHERE " + COLUMN_PROGRESS_HABIT_ID + " = ? AND " + COLUMN_PROGRESS_DATE +
                        " BETWEEN ? AND ?",
                new String[]{String.valueOf(habitId), startDate, endDate}
        );
        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
    }

    private int getProgressForDate(SQLiteDatabase db, int habitId, String date) {
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COLUMN_PROGRESS_VALUE + ") FROM " + TABLE_HABIT_PROGRESS +
                        " WHERE " + COLUMN_PROGRESS_HABIT_ID + " = ? AND " + COLUMN_PROGRESS_DATE + " = ?",
                new String[]{String.valueOf(habitId), date}
        );
        int progress = 0;
        if (cursor.moveToFirst()) {
            progress = cursor.getInt(0);
        }
        cursor.close();
        return progress;
    }

    public int getCurrentStreak(int habitId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Habit habit = getHabitById(habitId);
        if (habit == null) return 0;

        String goalPeriod = habit.getGoalPeriod();
        String habitType = habit.getHabitType();
        int goal = habit.getGoal();
        int streak = 0;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar createdAtCal = Calendar.getInstance();
        try {
            createdAtCal.setTime(sdf.parse(habit.getCreatedAt()));
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }

        if (habitType.equals("Build")) {
            while (true) {
                String startDate = "", endDate = "";

                switch (goalPeriod) {
                    case "Daily":
                        startDate = sdf.format(calendar.getTime());
                        endDate = startDate;
                        break;

                    case "Weekly":
                        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                        int daysBackToMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - Calendar.MONDAY);
                        calendar.add(Calendar.DAY_OF_YEAR, -daysBackToMonday);
                        startDate = sdf.format(calendar.getTime());

                        calendar.add(Calendar.DAY_OF_YEAR, 6);
                        endDate = sdf.format(calendar.getTime());

                        calendar.add(Calendar.DAY_OF_YEAR, -6);
                        break;

                    case "Monthly":
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        startDate = sdf.format(calendar.getTime());
                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                        endDate = sdf.format(calendar.getTime());
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        break;
                }

                try {
                    Date currentEndDate = sdf.parse(endDate);
                    if (currentEndDate.before(createdAtCal.getTime())) {
                        Log.d("StreakDebug", "Entire period ends before habit creation. Stopping.");
                        break;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    break;
                }

                Log.d("StreakDebug", "Checking period: " + startDate + " to " + endDate);

                Cursor cursor = db.rawQuery(
                        "SELECT SUM(" + COLUMN_PROGRESS_VALUE + ") FROM " + TABLE_HABIT_PROGRESS +
                                " WHERE " + COLUMN_PROGRESS_HABIT_ID + " = ? AND " + COLUMN_PROGRESS_DATE +
                                " BETWEEN ? AND ?",
                        new String[]{String.valueOf(habitId), startDate, endDate}
                );

                int progress = 0;
                boolean metGoal = false;

                if (cursor.moveToFirst() && !cursor.isNull(0)) {
                    progress = cursor.getInt(0);
                    Log.d("StreakDebug", "Progress: " + progress + ", Goal: " + goal);
                    metGoal = progress >= goal;
                } else {
                    Log.d("StreakDebug", "No data. Streak ends for build habit.");
                }

                cursor.close();
                Log.d("StreakDebug", "Goal Met: " + metGoal);

                if (metGoal) {
                    streak++;
                    switch (goalPeriod) {
                        case "Daily": calendar.add(Calendar.DAY_OF_YEAR, -1); break;
                        case "Weekly": calendar.add(Calendar.WEEK_OF_YEAR, -1); break;
                        case "Monthly": calendar.add(Calendar.MONTH, -1); break;
                    }
                } else {
                    break;
                }
            }

        } else if (habitType.equals("Quit")) {
            while (true) {
                String startDate = "", endDate = "";

                switch (goalPeriod) {
                    case "Daily":
                        startDate = sdf.format(calendar.getTime());
                        endDate = startDate;
                        break;

                    case "Weekly":
                        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                        int daysBackToMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - Calendar.MONDAY);
                        calendar.add(Calendar.DAY_OF_YEAR, -daysBackToMonday);
                        startDate = sdf.format(calendar.getTime());

                        calendar.add(Calendar.DAY_OF_YEAR, 6);
                        endDate = sdf.format(calendar.getTime());

                        calendar.add(Calendar.DAY_OF_YEAR, -6);
                        break;

                    case "Monthly":
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        startDate = sdf.format(calendar.getTime());
                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                        endDate = sdf.format(calendar.getTime());
                        calendar.set(Calendar.DAY_OF_MONTH, 1);
                        break;
                }

                try {
                    Date currentEndDate = sdf.parse(endDate);
                    if (currentEndDate.before(createdAtCal.getTime())) {
                        Log.d("StreakDebug", "Entire period ends before habit creation. Stopping.");
                        break;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    break;
                }

                Log.d("StreakDebug", "Checking period: " + startDate + " to " + endDate);

                Cursor cursor = db.rawQuery(
                        "SELECT SUM(" + COLUMN_PROGRESS_VALUE + ") FROM " + TABLE_HABIT_PROGRESS +
                                " WHERE " + COLUMN_PROGRESS_HABIT_ID + " = ? AND " + COLUMN_PROGRESS_DATE +
                                " BETWEEN ? AND ?",
                        new String[]{String.valueOf(habitId), startDate, endDate}
                );

                int progress = 0;
                boolean metGoal = true;

                if (cursor.moveToFirst()) {
                    if (!cursor.isNull(0)) {
                        progress = cursor.getInt(0);
                        Log.d("StreakDebug", "Progress: " + progress + ", Goal: " + goal);
                        metGoal = progress <= goal;
                    } else {
                        Log.d("StreakDebug", "No data. Assuming success for quit habit.");
                    }
                }

                cursor.close();
                Log.d("StreakDebug", "Goal Met: " + metGoal);

                if (metGoal) {
                    streak++;
                    switch (goalPeriod) {
                        case "Daily": calendar.add(Calendar.DAY_OF_YEAR, -1); break;
                        case "Weekly": calendar.add(Calendar.WEEK_OF_YEAR, -1); break;
                        case "Monthly": calendar.add(Calendar.MONTH, -1); break;
                    }
                } else {
                    break;
                }
            }
        }

        Log.d("StreakDebug", "Final Streak = " + streak);
        return streak;
    }

    public int[] getMonthlyCompletionRatio(int habitId, Calendar month) {
        SQLiteDatabase db = this.getReadableDatabase();

        int completed = 0;
        int missed = 0;
        Habit habit = getHabitById(habitId);
        if (habit == null) return new int[]{0, 0};

        String habitType = habit.getHabitType();
        String goalPeriod = habit.getGoalPeriod();
        int goal = habit.getGoal();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar start = (Calendar) month.clone();
        start.set(Calendar.DAY_OF_MONTH, 1);

        Calendar end = (Calendar) month.clone();
        end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));

        if (goalPeriod.equalsIgnoreCase("Daily") || goalPeriod.equalsIgnoreCase("Denne")) {
            while (!start.after(end)) {
                String date = sdf.format(start.getTime());
                int progress = getProgressForDate(db, habitId, date);

                boolean isCompleted = habitType.equalsIgnoreCase("Quit")
                        ? progress <= goal
                        : progress >= goal;

                if (isCompleted) completed++;
                else missed++;

                start.add(Calendar.DAY_OF_MONTH, 1);
            }

        } else if (goalPeriod.equalsIgnoreCase("Weekly") || goalPeriod.equalsIgnoreCase("Týždenne")) {
            Calendar cursor = (Calendar) start.clone();
            cursor.setFirstDayOfWeek(Calendar.MONDAY);
            cursor.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

            while (cursor.getTimeInMillis() <= end.getTimeInMillis()) {
                Calendar weekStart = (Calendar) cursor.clone();
                Calendar weekEnd = (Calendar) weekStart.clone();
                weekEnd.add(Calendar.DAY_OF_MONTH, 6);

                boolean isInMonth = false;
                List<Calendar> weekDays = new ArrayList<>();
                Calendar temp = (Calendar) weekStart.clone();

                int weekProgress = 0;

                for (int i = 0; i < 7; i++) {
                    weekDays.add((Calendar) temp.clone());

                    if (temp.get(Calendar.MONTH) == month.get(Calendar.MONTH)) {
                        isInMonth = true;
                    }

                    String dateStr = sdf.format(temp.getTime());
                    weekProgress += getProgressForDate(db, habitId, dateStr);

                    temp.add(Calendar.DAY_OF_MONTH, 1);
                }

                if (isInMonth) {
                    boolean isCompleted = habitType.equalsIgnoreCase("Quit")
                            ? weekProgress <= goal
                            : weekProgress >= goal;

                    for (Calendar day : weekDays) {
                        if (day.get(Calendar.MONTH) == month.get(Calendar.MONTH)) {
                            if (isCompleted) completed++;
                            else missed++;
                        }
                    }
                }
                cursor.add(Calendar.DAY_OF_MONTH, 7);
            }

        } else if (goalPeriod.equalsIgnoreCase("Monthly") || goalPeriod.equalsIgnoreCase("Mesačne")) {
            int totalProgress = getProgressInRange(
                    db,
                    habitId,
                    sdf.format(start.getTime()),
                    sdf.format(end.getTime())
            );

            boolean isCompleted = habitType.equalsIgnoreCase("Quit")
                    ? totalProgress <= goal
                    : totalProgress >= goal;

            int daysInMonth = end.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (isCompleted) completed = daysInMonth;
            else missed = daysInMonth;
        }

        db.close();
        return new int[]{completed, missed};
    }
}
