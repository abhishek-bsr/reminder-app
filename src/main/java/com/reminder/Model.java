package com.reminder;

import java.time.Instant;

enum Frequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

public class Model {
    private int reminderId;
    private String reminderName;
    private String tagColor;
    private boolean isCompleted;
    private boolean isImportant;
    private Instant reminderUtc;
    private Frequency frequency;
    private String reminderNote;

    public Model(int reminderId, String reminderName, String tagColor, boolean isCompleted, boolean isImportant,
            Instant reminderUtc, Frequency frequency, String reminderNote) {
        this.reminderId = reminderId;
        this.reminderName = reminderName;
        this.tagColor = tagColor;
        this.isCompleted = isCompleted;
        this.isImportant = isImportant;
        this.reminderUtc = reminderUtc;
        this.frequency = frequency;
        this.reminderNote = reminderNote;
    }

    public int getUuid() {
        return this.reminderId;
    }

    public String getReminderName() {
        return this.reminderName;
    }

    public String getTagColor() {
        return this.tagColor;
    }

    public boolean getIsCompleted() {
        return this.isCompleted;
    }

    public boolean getIsImportant() {
        return this.isImportant;
    }

    public Instant getReminderUtc() {
        return this.reminderUtc;
    }

    public Frequency getFrequency() {
        return this.frequency;
    }

    public String getReminderNote() {
        return this.reminderNote;
    }
}
