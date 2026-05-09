package com.example.sleepy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class WeeklyGoalActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "sleepy_prefs";
    private static final String ENTRIES_KEY = "sleep_entries";
    private static final String WEEKLY_GOAL_KEY = "weekly_goal_hours";
    private static final String WEEKLY_GOAL_START_KEY = "weekly_goal_start_date";

    private TextView tvGoalTitle;
    private TextView tvGoalSubtitle;
    private TextView tvTargetValue;
    private TextView tvVeryGoodCount;
    private TextView tvGoodCount;
    private TextView tvMissedCount;
    private TextView tvWeekDetails;
    private MaterialButton btnSaveGoal;
    private ChipGroup chipGroupGoal;

    private int selectedGoal = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_goal);

        tvGoalTitle = findViewById(R.id.tvGoalTitle);
        tvGoalSubtitle = findViewById(R.id.tvGoalSubtitle);
        tvTargetValue = findViewById(R.id.tvTargetValue);
        tvVeryGoodCount = findViewById(R.id.tvVeryGoodCount);
        tvGoodCount = findViewById(R.id.tvGoodCount);
        tvMissedCount = findViewById(R.id.tvMissedCount);
        tvWeekDetails = findViewById(R.id.tvWeekDetails);
        btnSaveGoal = findViewById(R.id.btnSaveGoal);
        chipGroupGoal = findViewById(R.id.chipGroupGoal);

        loadWeeklyGoal();
        setupGoalSelection();
        updateUi();

        btnSaveGoal.setOnClickListener(v -> {
            saveWeeklyGoal();
            updateUi();
            Toast.makeText(this, "Weekly goal updated", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupGoalSelection() {
        checkGoalChip(selectedGoal);

        chipGroupGoal.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds == null || checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipGoal7) {
                selectedGoal = 7;
            } else if (checkedId == R.id.chipGoal8) {
                selectedGoal = 8;
            } else if (checkedId == R.id.chipGoal9) {
                selectedGoal = 9;
            }
        });
    }

    private void checkGoalChip(int goal) {
        Chip chip7 = findViewById(R.id.chipGoal7);
        Chip chip8 = findViewById(R.id.chipGoal8);
        Chip chip9 = findViewById(R.id.chipGoal9);

        if (goal == 7 && chip7 != null) chip7.setChecked(true);
        if (goal == 8 && chip8 != null) chip8.setChecked(true);
        if (goal == 9 && chip9 != null) chip9.setChecked(true);
    }

    private void loadWeeklyGoal() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        selectedGoal = prefs.getInt(WEEKLY_GOAL_KEY, 8);
    }

    private void saveWeeklyGoal() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(WEEKLY_GOAL_KEY, selectedGoal);

        String currentStart = prefs.getString(WEEKLY_GOAL_START_KEY, null);
        if (currentStart == null || hasGoalWeekEnded(currentStart)) {
            editor.putString(WEEKLY_GOAL_START_KEY, getTodayKey());
        }

        editor.apply();
    }

    private void updateUi() {
        tvTargetValue.setText(selectedGoal + "h per night • " + getGoalWeekRangeText());

        WeekStats stats = calculateCurrentWeekStats();

        tvVeryGoodCount.setText(String.valueOf(stats.veryGood));
        tvGoodCount.setText(String.valueOf(stats.good));
        tvMissedCount.setText(String.valueOf(stats.missed));
        tvWeekDetails.setText(stats.detailsText);

        tvGoalTitle.setText("Weekly Goal");
        if (hasGoalWeekEnded(getGoalStartDateKey())) {
            tvGoalSubtitle.setText("This weekly goal ended. Start a new week to continue tracking your sleep habits.");
        } else {
            tvGoalSubtitle.setText("Track your sleep this week and improve your bedtime habits. " + getGoalStartEndNote());
        }
        if (hasGoalWeekEnded(getGoalStartDateKey())) {
            btnSaveGoal.setText("Start New Week");
        } else {
            btnSaveGoal.setText("Save Goal");
        }
    }

    private WeekStats calculateCurrentWeekStats() {
        WeekStats stats = new WeekStats();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedEntries = prefs.getString(ENTRIES_KEY, "{}");

        JSONObject allEntries;
        try {
            allEntries = new JSONObject(savedEntries);
        } catch (JSONException e) {
            allEntries = new JSONObject();
        }

        Map<String, String> weekDates = getCurrentWeekDates();

        StringBuilder details = new StringBuilder();

        for (Map.Entry<String, String> item : weekDates.entrySet()) {
            String dayName = item.getKey();
            String displayDate = formatDisplayDate(item.getValue());
            String dateKey = item.getValue();

            JSONObject entry = allEntries.optJSONObject(dateKey);

            if (entry == null) {
                details.append(dayName)
                        .append(" ").append(displayDate);

                if (isFutureDateKey(dateKey)) {
                    details.append(" — upcoming");
                } else {
                    stats.missed++;
                    details.append(" — no entry ❌");
                }

                details.append("\n");
                continue;
            }

            double duration = entry.optDouble("duration", 0);
            boolean caffeineLate = entry.optBoolean("caffeine", false);
            String screen = entry.optString("screen", "none");

            boolean hitGoal = duration >= selectedGoal;
            boolean noCaffeine = !caffeineLate;
            boolean noBlueLight = "none".equals(screen);

            String durationText = formatHours(duration);
            String caffeineText = noCaffeine ? "No" : "Yes";
            String screenText;
            if ("none".equals(screen)) {
                screenText = "No";
            } else if ("lt1".equals(screen)) {
                screenText = "Less than 1 hour";
            } else {
                screenText = "More than 1 hour";
            }

            if (hitGoal && noCaffeine && noBlueLight) {
                stats.veryGood++;
            } else if (hitGoal) {
                stats.good++;
            } else {
                stats.missed++;
            }

            details.append(dayName)
                    .append(" ").append(displayDate)
                    .append(" — 🛌 ").append(durationText).append(hitGoal ? " ✅" : " ❌")
                    .append("   ☕ ").append(caffeineText).append(noCaffeine ? " ✅" : " ❌")
                    .append("   📱 ").append(screenText).append(noBlueLight ? " ✅" : " ❌")
                    .append("\n");
        }

        stats.detailsText = details.toString().trim();
        return stats;
    }

    private Map<String, String> getCurrentWeekDates() {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();

        Calendar calendar = parseDateKey(getGoalStartDateKey());

        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            result.put(dayNameFormat.format(calendar.getTime()), keyFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return result;
    }

    private String formatDisplayDate(String dateKey) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("d MMM", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(dateKey));
        } catch (Exception e) {
            return dateKey;
        }
    }



    private String getGoalStartDateKey() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String startDate = prefs.getString(WEEKLY_GOAL_START_KEY, null);

        if (startDate == null) {
            startDate = getTodayKey();
            prefs.edit().putString(WEEKLY_GOAL_START_KEY, startDate).apply();
        }

        return startDate;
    }

    private String getGoalWeekRangeText() {
        Calendar start = parseDateKey(getGoalStartDateKey());
        Calendar end = parseDateKey(getGoalStartDateKey());
        end.add(Calendar.DAY_OF_MONTH, 6);

        SimpleDateFormat format = new SimpleDateFormat("d MMM", Locale.getDefault());
        return format.format(start.getTime()) + " - " + format.format(end.getTime());
    }

    private String getGoalStartEndNote() {
        Calendar start = parseDateKey(getGoalStartDateKey());
        Calendar end = parseDateKey(getGoalStartDateKey());
        end.add(Calendar.DAY_OF_MONTH, 6);

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM", Locale.getDefault());

        return "Start: " + dayFormat.format(start.getTime()) + " " + dateFormat.format(start.getTime())
                + " • End: " + dayFormat.format(end.getTime()) + " " + dateFormat.format(end.getTime());
    }

    private boolean hasGoalWeekEnded(String startDateKey) {
        Calendar end = parseDateKey(startDateKey);
        end.add(Calendar.DAY_OF_MONTH, 6);

        String endKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(end.getTime());
        return getTodayKey().compareTo(endKey) > 0;
    }

    private boolean isFutureDateKey(String dateKey) {
        return dateKey.compareTo(getTodayKey()) > 0;
    }

    private String getTodayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
    }

    private Calendar parseDateKey(String dateKey) {
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            calendar.setTime(format.parse(dateKey));
        } catch (Exception ignored) {
        }
        return calendar;
    }

    private String formatHours(double hours) {
        int wholeHours = (int) hours;
        int minutes = (int) Math.round((hours - wholeHours) * 60);

        if (minutes == 60) {
            wholeHours += 1;
            minutes = 0;
        }

        return wholeHours + "h " + minutes + "m";
    }

    private static class WeekStats {
        int veryGood = 0;
        int good = 0;
        int missed = 0;
        String detailsText = "";
    }
}