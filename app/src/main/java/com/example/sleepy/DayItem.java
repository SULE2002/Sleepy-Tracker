package com.example.sleepy;

public class DayItem {

    private final int dayNumber;
    private final int dotType;
    private boolean isSelected;
    private final String fullDate;
    private final boolean hasSavedEntry;
    private final boolean isStreakDay;

    public DayItem(int dayNumber, int dotType, boolean isSelected, String fullDate, boolean hasSavedEntry, boolean isStreakDay) {
        this.dayNumber = dayNumber;
        this.dotType = dotType;
        this.isSelected = isSelected;
        this.fullDate = fullDate;
        this.hasSavedEntry = hasSavedEntry;
        this.isStreakDay = isStreakDay;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public int getDotType() {
        return dotType;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getFullDate() {
        return fullDate;
    }

    public boolean hasSavedEntry() {
        return hasSavedEntry;
    }

    public boolean isStreakDay() {
        return isStreakDay;
    }
}