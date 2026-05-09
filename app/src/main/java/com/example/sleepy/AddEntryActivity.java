package com.example.sleepy;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;

public class AddEntryActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "sleepy_prefs";
    private static final String ENTRIES_KEY = "sleep_entries";

    private EditText etSleepDuration;
    private ChipGroup rgScreenBeforeBed, rgCaffeine;
    private MaterialButton btnSaveEntry;
    private TextView tvDailyCheckStatus;

    private String editingDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);

        etSleepDuration = findViewById(R.id.etSleepDuration);
        rgScreenBeforeBed = findViewById(R.id.rgScreenBeforeBed);
        rgCaffeine = findViewById(R.id.rgCaffeine);
        btnSaveEntry = findViewById(R.id.btnSaveEntry);
        tvDailyCheckStatus = findViewById(R.id.tvDailyCheckStatus);

        etSleepDuration.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        editingDate = getIntent().getStringExtra("entry_date");
        if (editingDate == null || editingDate.trim().isEmpty()) {
            editingDate = getTodayDate();
        }

        if (isFutureDate(editingDate)) {
            Toast.makeText(this, "You can only edit today or past dates", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadEntryIfExists(editingDate);
        updateDailyCheckStatus();

        btnSaveEntry.setOnClickListener(v -> saveEntry());
    }

    private void loadEntryIfExists(String date) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedEntries = prefs.getString(ENTRIES_KEY, "{}");

        try {
            JSONObject allEntries = new JSONObject(savedEntries);
            JSONObject entry = allEntries.optJSONObject(date);

            if (entry == null) {
                btnSaveEntry.setText("Save Entry");
                return;
            }

            double duration = entry.optDouble("duration", 0);
            if (duration > 0) {
                if (duration == (int) duration) {
                    etSleepDuration.setText(String.valueOf((int) duration));
                } else {
                    etSleepDuration.setText(String.valueOf(duration));
                }
            }

            String screen = entry.optString("screen", "none");
            rgScreenBeforeBed.check(getScreenChipId(screen));

            boolean caffeine = entry.optBoolean("caffeine", false);
            rgCaffeine.check(caffeine ? R.id.rbCaffeineYes : R.id.rbCaffeineNo);

            btnSaveEntry.setText("Update Entry");

        } catch (JSONException e) {
            btnSaveEntry.setText("Save Entry");
        }
    }

    private void saveEntry() {
        if (isFutureDate(editingDate)) {
            Toast.makeText(this, "Future dates cannot be edited", Toast.LENGTH_SHORT).show();
            return;
        }

        String durationText = etSleepDuration.getText().toString().trim();

        if (durationText.isEmpty()) {
            Toast.makeText(this, "Please enter sleep duration", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rgScreenBeforeBed.getCheckedChipId() == android.view.View.NO_ID) {
            Toast.makeText(this, "Please select screen before bed", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rgCaffeine.getCheckedChipId() == android.view.View.NO_ID) {
            Toast.makeText(this, "Please select caffeine option", Toast.LENGTH_SHORT).show();
            return;
        }

        double durationHours;
        try {
            durationHours = Double.parseDouble(durationText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid sleep duration", Toast.LENGTH_SHORT).show();
            return;
        }

        if (durationHours < 1 || durationHours > 12) {
            Toast.makeText(this, "Enter sleep between 1 and 12 hours", Toast.LENGTH_SHORT).show();
            return;
        }

        String screenBeforeBed = getScreenValue(rgScreenBeforeBed.getCheckedChipId());
        boolean caffeineLate = rgCaffeine.getCheckedChipId() == R.id.rbCaffeineYes;
        int score = calculateScore(durationHours, screenBeforeBed, caffeineLate);

        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String savedEntries = prefs.getString(ENTRIES_KEY, "{}");
            JSONObject allEntries = new JSONObject(savedEntries);

            JSONObject entry = new JSONObject();
            entry.put("date", editingDate);
            entry.put("duration", durationHours);
            entry.put("screen", screenBeforeBed);
            entry.put("caffeine", caffeineLate);
            entry.put("score", score);

            allEntries.put(editingDate, entry);

            prefs.edit().putString(ENTRIES_KEY, allEntries.toString()).apply();

            updateDailyCheckStatus();
            Toast.makeText(this, "Entry saved", Toast.LENGTH_SHORT).show();
            finish();

        } catch (JSONException e) {
            Toast.makeText(this, "Failed to save entry", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDailyCheckStatus() {
        if (tvDailyCheckStatus == null) return;
        tvDailyCheckStatus.setText(entryExistsForDate(editingDate) ? "1/1 check up" : "1/0 check up");
    }

    private boolean entryExistsForDate(String date) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedEntries = prefs.getString(ENTRIES_KEY, "{}");

        try {
            JSONObject allEntries = new JSONObject(savedEntries);
            return allEntries.has(date);
        } catch (JSONException e) {
            return false;
        }
    }

    private String getScreenValue(int checkedId) {
        if (checkedId == R.id.rbScreenNo) return "none";
        if (checkedId == R.id.rbScreenLessThan1) return "lt1";
        return "gt1";
    }

    private int getScreenChipId(String screen) {
        if ("none".equals(screen)) return R.id.rbScreenNo;
        if ("lt1".equals(screen)) return R.id.rbScreenLessThan1;
        return R.id.rbScreenMoreThan1;
    }

    private int calculateScore(double durationHours, String screenBeforeBed, boolean caffeineLate) {
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

    private boolean isFutureDate(String date) {
        return date.compareTo(getTodayDate()) > 0;
    }

    private String getTodayDate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return LocalDate.now().toString();
        } else {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        }
    }
}