package com.example.sleepy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final String PREFS_NAME = "sleepy_prefs";
    private static final String ENTRIES_KEY = "sleep_entries";
    private static final String WEEKLY_GOAL_KEY = "weekly_goal_hours";

    private TextView tvLastSleep;
    private TextView tvLastSleepNote;
    private TextView tvFeedbackTitle;
    private TextView tvFeedbackMessage;

    private TextView tvWeeklyGoalMessage;
    private TextView tvWeeklyGoalTarget;
    private TextView tvWeeklyGoalVeryGood;
    private TextView tvWeeklyGoalGood;
    private TextView tvWeeklyGoalMissed;

    private View layoutWeeklyGoalStats;
    private View viewStatusCircle;
    private View day1;
    private View day2;
    private View day3;
    private View day4;
    private View day5;
    private View day6;
    private View day7;
    private TextView tvDayDate1, tvDayDate2, tvDayDate3, tvDayDate4, tvDayDate5, tvDayDate6, tvDayDate7;
    private TextView tvDayName1, tvDayName2, tvDayName3, tvDayName4, tvDayName5, tvDayName6, tvDayName7;

    private MaterialButton btnSetWeeklyGoal;
    private MaterialButton btnWeeklyDetails;
    private MaterialButton btnAddEntry;
    private static final String REMINDER_ENABLED_KEY = "daily_reminder_enabled";
    private static final String REMINDER_HOUR_KEY = "daily_reminder_hour";
    private static final String REMINDER_MINUTE_KEY = "daily_reminder_minute";
    private TextView tvReminderMessage;
    private TextView tvReminderTime;
    private View layoutReminderInfo;
    private MaterialButton btnSetReminder;
    private MaterialButton btnTurnOffReminder;

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvLastSleep = view.findViewById(R.id.tvLastSleep);
        tvLastSleepNote = view.findViewById(R.id.tvLastSleepNote);
        tvFeedbackTitle = view.findViewById(R.id.tvFeedbackTitle);
        tvFeedbackMessage = view.findViewById(R.id.tvFeedbackMessage);

        tvWeeklyGoalMessage = view.findViewById(R.id.tvWeeklyGoalMessage);
        tvWeeklyGoalTarget = view.findViewById(R.id.tvWeeklyGoalTarget);
        tvWeeklyGoalVeryGood = view.findViewById(R.id.tvWeeklyGoalVeryGood);
        tvWeeklyGoalGood = view.findViewById(R.id.tvWeeklyGoalGood);
        tvWeeklyGoalMissed = view.findViewById(R.id.tvWeeklyGoalMissed);

        layoutWeeklyGoalStats = view.findViewById(R.id.layoutWeeklyGoalStats);

        viewStatusCircle = view.findViewById(R.id.viewStatusCircle);
        day1 = view.findViewById(R.id.day1);
        day2 = view.findViewById(R.id.day2);
        day3 = view.findViewById(R.id.day3);
        day4 = view.findViewById(R.id.day4);
        day5 = view.findViewById(R.id.day5);
        day6 = view.findViewById(R.id.day6);
        day7 = view.findViewById(R.id.day7);
        tvDayDate1 = view.findViewById(R.id.tvDayDate1);
        tvDayDate2 = view.findViewById(R.id.tvDayDate2);
        tvDayDate3 = view.findViewById(R.id.tvDayDate3);
        tvDayDate4 = view.findViewById(R.id.tvDayDate4);
        tvDayDate5 = view.findViewById(R.id.tvDayDate5);
        tvDayDate6 = view.findViewById(R.id.tvDayDate6);
        tvDayDate7 = view.findViewById(R.id.tvDayDate7);

        tvDayName1 = view.findViewById(R.id.tvDayName1);
        tvDayName2 = view.findViewById(R.id.tvDayName2);
        tvDayName3 = view.findViewById(R.id.tvDayName3);
        tvDayName4 = view.findViewById(R.id.tvDayName4);
        tvDayName5 = view.findViewById(R.id.tvDayName5);
        tvDayName6 = view.findViewById(R.id.tvDayName6);
        tvDayName7 = view.findViewById(R.id.tvDayName7);

        btnSetWeeklyGoal = view.findViewById(R.id.btnSetWeeklyGoal);
        btnWeeklyDetails = view.findViewById(R.id.btnWeeklyDetails);
        btnAddEntry = view.findViewById(R.id.btnAddEntry);

        tvReminderMessage = view.findViewById(R.id.tvReminderMessage);
        tvReminderTime = view.findViewById(R.id.tvReminderTime);
        layoutReminderInfo = view.findViewById(R.id.layoutReminderInfo);
        btnSetReminder = view.findViewById(R.id.btnSetReminder);
        btnTurnOffReminder = view.findViewById(R.id.btnTurnOffReminder);

        btnAddEntry.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEntryActivity.class);
            startActivity(intent);
        });

        btnSetWeeklyGoal.setOnClickListener(v -> openWeeklyGoalPage());
        btnWeeklyDetails.setOnClickListener(v -> openWeeklyGoalPage());

        btnSetReminder.setOnClickListener(v -> openReminderTimePicker());
        btnTurnOffReminder.setOnClickListener(v -> turnOffReminder());

        updateLastSleepCard();
        updateHomeStatusDots();
        updateWeeklyGoalCard();
        updateSleepFeedback();

        updateReminderCard();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLastSleepCard();
        updateHomeStatusDots();
        updateWeeklyGoalCard();
        updateSleepFeedback();
        updateReminderCard();
    }

    private void openWeeklyGoalPage() {
        Intent intent = new Intent(requireContext(), WeeklyGoalActivity.class);
        startActivity(intent);
    }



    private void updateLastSleepCard() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        String savedEntries = prefs.getString(ENTRIES_KEY, "{}");

        try {
            JSONObject allEntries = new JSONObject(savedEntries);
            String latestDate = getLatestDate(allEntries);

            if (latestDate == null) {
                tvLastSleep.setText("No data");
                tvLastSleepNote.setText("Add your first sleep entry");
                return;
            }

            JSONObject latestEntry = allEntries.optJSONObject(latestDate);
            if (latestEntry == null) {
                tvLastSleep.setText("No data");
                tvLastSleepNote.setText("Add your first sleep entry");
                return;
            }

            double duration = latestEntry.optDouble("duration", 0);

            tvLastSleep.setText(formatHours(duration));
            tvLastSleepNote.setText("Latest saved sleep entry");

        } catch (JSONException e) {
            tvLastSleep.setText("No data");
            tvLastSleepNote.setText("Add your first sleep entry");
        }
    }

    private void updateHomeStatusDots() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        String savedEntries = prefs.getString(ENTRIES_KEY, "{}");

        try {
            JSONObject allEntries = new JSONObject(savedEntries);

            String todayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Calendar.getInstance().getTime());
            JSONObject todayEntry = allEntries.optJSONObject(todayKey);
            if (todayEntry != null) {
                double todayDuration = todayEntry.optDouble("duration", 0);
                int todayScore = getDurationScore(todayDuration);
                setCircleColor(viewStatusCircle, todayScore);
            } else {
                setCircleGray(viewStatusCircle);
            }

            View[] last7Views = {day1, day2, day3, day4, day5, day6, day7};
            TextView[] dateLabels = {tvDayDate1, tvDayDate2, tvDayDate3, tvDayDate4, tvDayDate5, tvDayDate6, tvDayDate7};
            TextView[] nameLabels = {tvDayName1, tvDayName2, tvDayName3, tvDayName4, tvDayName5, tvDayName6, tvDayName7};

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -6);

            SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM", Locale.getDefault());
            SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());

            for (int i = 0; i < 7; i++) {
                String dateKey = keyFormat.format(calendar.getTime());
                JSONObject entry = allEntries.optJSONObject(dateKey);

                if (entry != null) {
                    double duration = entry.optDouble("duration", 0);
                    int score = getDurationScore(duration);
                    setCircleColor(last7Views[i], score);
                } else {
                    setCircleGray(last7Views[i]);
                }

                if (dateLabels[i] != null) {
                    dateLabels[i].setText(dateFormat.format(calendar.getTime()));
                }

                if (nameLabels[i] != null) {
                    nameLabels[i].setText(dayNameFormat.format(calendar.getTime()));
                }

                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

        } catch (JSONException e) {
            setCircleGray(viewStatusCircle);
            setCircleGray(day1);
            setCircleGray(day2);
            setCircleGray(day3);
            setCircleGray(day4);
            setCircleGray(day5);
            setCircleGray(day6);
            setCircleGray(day7);
        }
    }

    private void updateWeeklyGoalCard() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        int weeklyGoal = prefs.getInt(WEEKLY_GOAL_KEY, -1);

        if (weeklyGoal == -1) {
            tvWeeklyGoalMessage.setText("Want to improve your sleep? Try tracking your weekly sleep.");
            layoutWeeklyGoalStats.setVisibility(View.GONE);
            btnSetWeeklyGoal.setText("Set Goal");
            btnSetWeeklyGoal.setVisibility(View.VISIBLE);
            btnWeeklyDetails.setVisibility(View.GONE);
            return;
        }


        WeekStats stats = calculateCurrentWeekStats(weeklyGoal);

        tvWeeklyGoalMessage.setText("Your weekly target is active.");
        layoutWeeklyGoalStats.setVisibility(View.VISIBLE);
        tvWeeklyGoalTarget.setText("Target: " + weeklyGoal + "h per night");
        tvWeeklyGoalVeryGood.setText("Very Good: " + stats.veryGood + "/7");
        tvWeeklyGoalGood.setText("Good: " + stats.good + "/7");
        tvWeeklyGoalMissed.setText("Missed: " + stats.missed + "/7");

        btnSetWeeklyGoal.setText("Edit Goal");
        btnSetWeeklyGoal.setVisibility(View.VISIBLE);
        btnWeeklyDetails.setVisibility(View.VISIBLE);
    }

    private WeekStats calculateCurrentWeekStats(int targetHours) {
        WeekStats stats = new WeekStats();

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        String savedEntries = prefs.getString(ENTRIES_KEY, "{}");

        JSONObject allEntries;
        try {
            allEntries = new JSONObject(savedEntries);
        } catch (JSONException e) {
            allEntries = new JSONObject();
        }

        Map<String, String> weekDates = getCurrentWeekDates();

        for (Map.Entry<String, String> item : weekDates.entrySet()) {
            String dateKey = item.getValue();
            JSONObject entry = allEntries.optJSONObject(dateKey);

            if (entry == null) {
                stats.missed++;
                continue;
            }

            double duration = entry.optDouble("duration", 0);
            boolean caffeineLate = entry.optBoolean("caffeine", false);
            String screen = entry.optString("screen", "none");

            boolean hitGoal = duration >= targetHours;
            boolean noCaffeine = !caffeineLate;
            boolean noBlueLight = "none".equals(screen);

            if (hitGoal && noCaffeine && noBlueLight) {
                stats.veryGood++;
            } else if (hitGoal) {
                stats.good++;
            } else {
                stats.missed++;
            }
        }

        return stats;
    }

    private Map<String, String> getCurrentWeekDates() {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);

        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        for (String dayName : dayNames) {
            result.put(dayName, keyFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return result;
    }

    private String getLatestDate(JSONObject allEntries) {
        if (allEntries.names() == null || allEntries.names().length() == 0) {
            return null;
        }

        String latest = null;
        for (int i = 0; i < allEntries.names().length(); i++) {
            String date = allEntries.names().optString(i);
            if (latest == null || date.compareTo(latest) > 0) {
                latest = date;
            }
        }
        return latest;
    }

    private int getDurationScore(double durationHours) {
        if (durationHours >= 7) {
            return 2;
        }

        if (durationHours >= 4) {
            return 1;
        }

        if (durationHours >= 1) {
            return 0;
        }

        return 0;
    }

    private void setCircleColor(View view, int score) {
        if (view == null) return;

        int color;
        if (score == 0) {
            color = 0xFFE74C3C;
        } else if (score == 1) {
            color = 0xFFF1C40F;
        } else {
            color = 0xFF2ECC71;
        }

        Drawable bg = view.getBackground();
        if (bg != null) {
            bg = DrawableCompat.wrap(bg.mutate());
            DrawableCompat.setTint(bg, color);
            view.setBackground(bg);
        }
    }

    private void setCircleGray(View view) {
        if (view == null) return;

        Drawable bg = view.getBackground();
        if (bg != null) {
            bg = DrawableCompat.wrap(bg.mutate());
            DrawableCompat.setTint(bg, 0xFFD6D6D6);
            view.setBackground(bg);
        }
    }

    private void updateSleepFeedback() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);

        String savedEntries = prefs.getString(ENTRIES_KEY, "{}");

        try {
            JSONObject allEntries = new JSONObject(savedEntries);
            String latestDate = getLatestDate(allEntries);

            if (latestDate == null) {
                tvFeedbackTitle.setText("No sleep entry yet");
                tvFeedbackMessage.setText("Add your latest sleep entry to get personalised feedback.");
                return;
            }

            JSONObject entry = allEntries.optJSONObject(latestDate);
            if (entry == null) return;

            double duration = entry.optDouble("duration", 0);
            boolean caffeine = entry.optBoolean("caffeine", false);
            String screen = entry.optString("screen", "none");

            boolean caffeineIssue = caffeine;
            boolean screenIssue = screen.equals("gt1");
            boolean badHabits = caffeineIssue || screenIssue;
            String habitAdvice = getHabitAdvice(caffeineIssue, screenIssue);

            if (duration >= 7 && !badHabits) {
                tvFeedbackTitle.setText("Excellent sleep");
                tvFeedbackMessage.setText("You reached the recommended healthy minimum of 7 hours and kept your bedtime habits clean. Keep this routine going.");
            }
            else if (duration >= 7) {
                tvFeedbackTitle.setText("Good sleep, but improve habits");
                tvFeedbackMessage.setText("You reached the recommended healthy minimum of 7 hours, but " + habitAdvice + " Try improving this habit tonight.");
            }
            else if (duration >= 5 && !badHabits) {
                tvFeedbackTitle.setText("Short sleep");
                tvFeedbackMessage.setText("You slept less than the recommended healthy minimum of 7 hours. Your habits looked okay, but try giving yourself more time to sleep tonight.");
            }
            else if (duration >= 5) {
                tvFeedbackTitle.setText("Short sleep with poor habits");
                tvFeedbackMessage.setText("You slept less than the recommended healthy minimum of 7 hours, and " + habitAdvice + " Try sleeping earlier and improving this habit tonight.");
            }
            else if (duration > 0 && !badHabits) {
                tvFeedbackTitle.setText("Very low sleep");
                tvFeedbackMessage.setText("Your sleep was far below the recommended healthy minimum of 7 hours. Even with good habits, this amount of sleep is not enough for proper recovery. Try prioritising a longer sleep window tonight.");
            }
            else if (duration > 0) {
                tvFeedbackTitle.setText("Very poor sleep pattern");
                tvFeedbackMessage.setText("Your sleep was far below the recommended healthy minimum of 7 hours, and " + habitAdvice + " Focus tonight on improving this habit and giving yourself a longer sleep window.");
            }

        } catch (JSONException e) {
            tvFeedbackTitle.setText("Error");
            tvFeedbackMessage.setText("Unable to load feedback.");
        }
    }

    private String getHabitAdvice(boolean caffeineIssue, boolean screenIssue) {
        if (caffeineIssue && screenIssue) {
            return "caffeine and high screen time before bed may have affected your sleep.";
        }

        if (caffeineIssue) {
            return "caffeine before bed may have affected your sleep.";
        }

        if (screenIssue) {
            return "high screen time before bed may have affected your sleep.";
        }

        return "your bedtime habits looked fine.";
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

    private void openReminderTimePicker() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);

        int savedHour = prefs.getInt(REMINDER_HOUR_KEY, 22);
        int savedMinute = prefs.getInt(REMINDER_MINUTE_KEY, 0);

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_reminder_time, null);
        dialog.setContentView(sheetView);

        NumberPicker pickerHour = sheetView.findViewById(R.id.pickerHour);
        NumberPicker pickerMinute = sheetView.findViewById(R.id.pickerMinute);
        NumberPicker pickerAmPm = sheetView.findViewById(R.id.pickerAmPm);
        MaterialButton btnCancel = sheetView.findViewById(R.id.btnCancelReminderSheet);
        MaterialButton btnSave = sheetView.findViewById(R.id.btnSaveReminderSheet);

        pickerHour.setMinValue(1);
        pickerHour.setMaxValue(12);
        pickerHour.setWrapSelectorWheel(true);

        pickerMinute.setMinValue(0);
        pickerMinute.setMaxValue(59);
        pickerMinute.setFormatter(value -> String.format(Locale.getDefault(), "%02d", value));
        pickerMinute.setWrapSelectorWheel(true);

        pickerAmPm.setMinValue(0);
        pickerAmPm.setMaxValue(1);
        pickerAmPm.setDisplayedValues(new String[]{"AM", "PM"});
        pickerAmPm.setWrapSelectorWheel(false);


        int displayHour = savedHour % 12;
        if (displayHour == 0) displayHour = 12;

        pickerHour.setValue(displayHour);
        pickerMinute.setValue(savedMinute);
        pickerAmPm.setValue(savedHour >= 12 ? 1 : 0);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            int selectedHour = pickerHour.getValue();
            int selectedMinute = pickerMinute.getValue();
            boolean isPm = pickerAmPm.getValue() == 1;

            int hour24 = selectedHour % 12;
            if (isPm) {
                hour24 += 12;
            }

            prefs.edit()
                    .putBoolean(REMINDER_ENABLED_KEY, true)
                    .putInt(REMINDER_HOUR_KEY, hour24)
                    .putInt(REMINDER_MINUTE_KEY, selectedMinute)
                    .apply();

            ReminderScheduler.scheduleDailyReminder(requireContext(), hour24, selectedMinute);
            updateReminderCard();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void turnOffReminder() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        prefs.edit().putBoolean(REMINDER_ENABLED_KEY, false).apply();

        ReminderScheduler.cancelDailyReminder(requireContext());

        updateReminderCard();
    }

    private void updateReminderCard() {
        if (tvReminderMessage == null || tvReminderTime == null || layoutReminderInfo == null || btnSetReminder == null || btnTurnOffReminder == null) {
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);

        boolean enabled = prefs.getBoolean(REMINDER_ENABLED_KEY, false);
        int hour = prefs.getInt(REMINDER_HOUR_KEY, 22);
        int minute = prefs.getInt(REMINDER_MINUTE_KEY, 0);

        if (!enabled) {
            tvReminderMessage.setText("Want to stay consistent with your sleep? Set a daily reminder to check in.");
            layoutReminderInfo.setVisibility(View.GONE);
            btnSetReminder.setText("Set Reminder");
            btnTurnOffReminder.setVisibility(View.GONE);
            return;
        }

        tvReminderMessage.setText("Your daily check-in reminder is active.");
        layoutReminderInfo.setVisibility(View.VISIBLE);
        tvReminderTime.setText("Reminder Time: " + formatReminderTime(hour, minute));
        btnSetReminder.setText("Edit Reminder");
        btnTurnOffReminder.setVisibility(View.VISIBLE);
    }

    private String formatReminderTime(int hour, int minute) {
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        String amPm = hour >= 12 ? "PM" : "AM";
        return String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm);
    }


    private static class WeekStats {
        int veryGood = 0;
        int good = 0;
        int missed = 0;
    }
}