package com.reminder;

import java.time.Instant;

import org.json.JSONArray;
import org.json.JSONObject;

public class Utils {
    protected int addRemindersAgainstParams(JSONObject reminderObject, JSONArray reminders, Boolean completedParam,
            Boolean importantParam, Boolean isCompleted,
            Boolean isImportant, int offsetParam, int limitParam) {
        // check for <completed> param and <important> param
        // if both are null, skip checking fields in JSONObject
        if (completedParam != null && importantParam != null) {
            if (completedParam == isCompleted && importantParam == isImportant) {
                reminders.put(reminderObject);
                return --limitParam;
            }
        } else if (completedParam == isCompleted) {
            reminders.put(reminderObject);
            return --limitParam;
        } else if (importantParam == isImportant) {
            reminders.put(reminderObject);
            return --limitParam;
        } else if (completedParam == null && importantParam == null) {
            reminders.put(reminderObject);
            return --limitParam;
        }

        return limitParam;
    }

    protected void addExistingData(JSONArray reminderList, int getIndex, String tagColor, Boolean isCompleted,
            Boolean isImportant, Instant reminderUtc, Frequency frequency, Object reminderNote) {
        reminderList.getJSONObject(getIndex).put("tag_color", tagColor);
        reminderList.getJSONObject(getIndex).put("is_completed", isCompleted);
        reminderList.getJSONObject(getIndex).put("is_important", isImportant);
        reminderList.getJSONObject(getIndex).put("reminder_utc", reminderUtc);
        reminderList.getJSONObject(getIndex).put("frequency", frequency);
        reminderList.getJSONObject(getIndex).put("note", reminderNote);
    }

    protected int adjustParamlengthIfRequired(int listSize, int paramValue) {
        if (paramValue > listSize)
            return listSize;
        else if (paramValue < 0)
            return 0;

        return paramValue;
    }

    protected int checkReminderNameExists(JSONArray reminderList, String reminderName) {
        for (int i = 0; i < reminderList.length(); i++) {
            JSONObject reminder = reminderList.getJSONObject(i);
            if (reminder.getString("name").equals(reminderName))
                return i;
        }

        return -1;
    }

    protected int getIndexFromList(JSONArray reminderList, int reminderId) {
        for (int i = 0; i < reminderList.length(); i++) {
            JSONObject reminder = reminderList.getJSONObject(i);
            if (reminder.getInt("id") == reminderId)
                return i;
        }

        return -1;
    }

    protected JSONObject checkReminderIdExists(JSONArray reminderList, int reminderId) {
        for (int i = 0; i < reminderList.length(); i++) {
            JSONObject reminder = reminderList.getJSONObject(i);
            if (reminder.getInt("id") == reminderId)
                return reminder;
        }

        return null;
    }

    protected Instant getUtcTime(String reminderUtcInString) {
        try {
            Instant time = Instant.parse(reminderUtcInString);
            return time;
        } catch (Exception err) {
            return null;
        }
    }

    protected void storeDataInList(JSONArray reminderList, int reminderId, String reminderName, String tagColor,
            boolean isCompleted, boolean isImportant, String reminderUtc, Frequency frequency, Object reminderNote) {
        JSONObject reminder = new JSONObject();
        reminder.put("id", reminderId);
        reminder.put("name", reminderName);
        reminder.put("tag_color", tagColor);
        reminder.put("is_completed", isCompleted);
        reminder.put("is_important", isImportant);
        reminder.put("reminder_utc", reminderUtc);
        reminder.put("frequency", frequency.toString());
        reminder.put("note", reminderNote);

        reminderList.put(reminder);
    }
}
