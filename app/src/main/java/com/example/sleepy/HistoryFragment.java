package com.example.sleepy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private static final String PREFS_NAME = "sleepy_prefs";
    private static final String ENTRIES_KEY = "sleep_entries";

    private String selectedDate;
    private RecyclerView rvCalendar;
    private TextView tvCurrentMonth;
    private TextView tvTodayDate;
    private TextView tvDate;
    private TextView tvHistorySummary;
    private TextView tvDuration;
    private MaterialButton btnViewDetails;
    private MaterialButton btnPrevMonth;
    private MaterialButton btnNextMonth;

    private int displayYear;
    private int displayMonth; // 1..12

    public HistoryFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_sleep_history, container, false);

        rvCalendar = view.findViewById(R.id.rvCalendar);
        tvCurrentMonth = view.findViewById(R.id.tvCurrentMonth);
        tvTodayDate = view.findViewById(R.id.tvTodayDate);
        tvDate = view.findViewById(R.id.tvDate);
        tvHistorySummary = view.findViewById(R.id.tvHistorySummary);
        tvDuration = view.findViewById(R.id.tvDuration);
        btnViewDetails = view.findViewById(R.id.btnViewDetails);
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);

        Calendar calendar = Calendar.getInstance();
        displayYear = calendar.get(Calendar.YEAR);
        displayMonth = calendar.get(Calendar.MONTH) + 1;

        if (selectedDate == null) {
            selectedDate = getTodayDate();
        }

        rvCalendar.setLayoutManager(new GridLayoutManager(requireContext(), 7));

        btnPrevMonth.setOnClickListener(v -> {
            displayMonth--;
            if (displayMonth < 1) {
                displayMonth = 12;
                displayYear--;
            }
            bindCalendar();
            updateMonthHeader();
            updateSummaryCard();
        });

        btnNextMonth.setOnClickListener(v -> {
            displayMonth++;
            if (displayMonth > 12) {
                displayMonth = 1;
                displayYear++;
            }
            bindCalendar();
            updateMonthHeader();
            updateSummaryCard();
        });

        btnViewDetails.setOnClickListener(v -> openEntryPage());

        updateMonthHeader();
        bindCalendar();
        updateSummaryCard();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateMonthHeader();
        bindCalendar();
        updateSummaryCard();
    }

    private void onDaySelected(DayItem dayItem) {
        if (dayItem.getDayNumber() <= 0 || dayItem.getFullDate() == null || dayItem.getFullDate().isEmpty()) {
            return;
        }

        selectedDate = dayItem.getFullDate();
        bindCalendar();
        updateSummaryCard();
    }

    private void updateMonthHeader() {
        if (tvCurrentMonth != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                YearMonth ym = YearMonth.of(displayYear, displayMonth);
                String monthName = ym.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
                tvCurrentMonth.setText(monthName + " " + displayYear);
            } else {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, displayYear);
                cal.set(Calendar.MONTH, displayMonth - 1);
                String monthName = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.getTime());
                tvCurrentMonth.setText(monthName);
            }
        }

        if (tvTodayDate != null) {
            tvTodayDate.setText("Today: " + getPrettyTodayDate());
        }
    }

    private void bindCalendar() {
        if (rvCalendar != null) {
            rvCalendar.setAdapter(new CalendarAdapter(buildMonthData(), this::onDaySelected));
        }
    }

    private void updateSummaryCard() {
        if (tvDate == null || tvHistorySummary == null || tvDuration == null) {
            return;
        }

        tvDate.setText(selectedDate == null ? "Select a date" : selectedDate);

        if (selectedDate != null && isFutureDate(selectedDate)) {
            tvHistorySummary.setText("Future dates cannot be edited.");
            tvDuration.setText("--");
            if (btnViewDetails != null) {
                btnViewDetails.setEnabled(false);
                btnViewDetails.setAlpha(0.5f);
            }
            return;
        }

        if (btnViewDetails != null) {
            btnViewDetails.setEnabled(true);
            btnViewDetails.setAlpha(1f);
        }

        JSONObject entry = getEntryForDate(selectedDate);
        if (entry == null) {
            tvHistorySummary.setText("No saved entry for this date.");
            tvDuration.setText("--");
            return;
        }

        double duration = entry.optDouble("duration", 0);
        String screen = entry.optString("screen", "-");
        boolean caffeine = entry.optBoolean("caffeine", false);
        int score = getDurationScore(duration);

        String screenText;
        if ("none".equals(screen)) {
            screenText = "No";
        } else if ("lt1".equals(screen)) {
            screenText = "Less than 1 hour";
        } else {
            screenText = "More than 1 hour";
        }

        String scoreText;
        if (score == 2) {
            scoreText = "Green";
        } else if (score == 1) {
            scoreText = "Yellow";
        } else {
            scoreText = "Red";
        }

        tvHistorySummary.setText(
                "Screen before bed: " + screenText
                        + "\nCaffeine late: " + (caffeine ? "Yes" : "No")
                        + "\nStatus: " + scoreText
        );
        tvDuration.setText(formatHours(duration));
    }

    private void openEntryPage() {
        if (selectedDate == null) {
            Toast.makeText(requireContext(), "Select a date first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isFutureDate(selectedDate)) {
            Toast.makeText(requireContext(), "You can only edit today or past dates", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireContext(), AddEntryActivity.class);
        intent.putExtra("entry_date", selectedDate);
        startActivity(intent);
    }

    private JSONObject getEntryForDate(String date) {
        if (date == null) return null;

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        String savedEntries = prefs.getString(ENTRIES_KEY, "{}");

        try {
            JSONObject allEntries = new JSONObject(savedEntries);
            return allEntries.optJSONObject(date);
        } catch (JSONException e) {
            return null;
        }
    }

    private List<DayItem> buildMonthData() {
        List<DayItem> days = new ArrayList<>();

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        String savedEntries = prefs.getString(ENTRIES_KEY, "{}");

        JSONObject allEntries;
        try {
            allEntries = new JSONObject(savedEntries);
        } catch (JSONException e) {
            allEntries = new JSONObject();
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, displayYear);
        cal.set(Calendar.MONTH, displayMonth - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // Sunday = 1
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int blanks = firstDayOfWeek - 1;
        for (int i = 0; i < blanks; i++) {
            days.add(new DayItem(0, 1, false, "", false, false));
        }

        List<String> fullDates = new ArrayList<>();
        List<Integer> scores = new ArrayList<>();
        List<Boolean> hasEntries = new ArrayList<>();
        List<Boolean> greenDays = new ArrayList<>();

        for (int day = 1; day <= daysInMonth; day++) {
            String fullDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", displayYear, displayMonth, day);
            JSONObject entry = allEntries.optJSONObject(fullDate);

            int score = entry != null ? getDurationScore(entry.optDouble("duration", 0)) : 1;
            boolean hasSavedEntry = entry != null;
            boolean isGreen = hasSavedEntry && score == 2;

            fullDates.add(fullDate);
            scores.add(score);
            hasEntries.add(hasSavedEntry);
            greenDays.add(isGreen);
        }

        boolean[] streakFlags = new boolean[daysInMonth];
        int streakStart = -1;
        int streakLength = 0;

        for (int i = 0; i < greenDays.size(); i++) {
            if (greenDays.get(i)) {
                if (streakStart == -1) {
                    streakStart = i;
                }
                streakLength++;
            } else {
                if (streakLength >= 2) {
                    for (int j = streakStart; j < streakStart + streakLength; j++) {
                        streakFlags[j] = true;
                    }
                }
                streakStart = -1;
                streakLength = 0;
            }
        }

        if (streakLength >= 2) {
            for (int j = streakStart; j < streakStart + streakLength; j++) {
                streakFlags[j] = true;
            }
        }

        for (int i = 0; i < daysInMonth; i++) {
            int dayNumber = i + 1;
            String fullDate = fullDates.get(i);
            int score = scores.get(i);
            boolean hasSavedEntry = hasEntries.get(i);
            boolean isSelected = fullDate.equals(selectedDate);
            boolean isStreakDay = streakFlags[i];

            days.add(new DayItem(dayNumber, score, isSelected, fullDate, hasSavedEntry, isStreakDay));
        }

        return days;
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

    private String formatHours(double hours) {
        int wholeHours = (int) hours;
        int minutes = (int) Math.round((hours - wholeHours) * 60);
        if (minutes == 60) {
            wholeHours += 1;
            minutes = 0;
        }
        return wholeHours + "h " + minutes + "m";
    }

    private boolean isFutureDate(String date) {
        if (date == null || date.isEmpty()) return false;
        return date.compareTo(getTodayDate()) > 0;
    }

    private String getTodayDate() {
        Calendar calendar = Calendar.getInstance();
        return String.format(Locale.getDefault(), "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    private String getPrettyTodayDate() {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.getTime());
    }
}