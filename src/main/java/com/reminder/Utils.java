package com.reminder;

import java.time.Instant;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

public class Utils {
    protected JSONObject responseBuilder(String key, Object value) {
        JSONObject responseObject = new JSONObject();
        responseObject.put(key, value);

        return responseObject;
    }

    protected UUID generateUUID() {
        return UUID.randomUUID();
    }

    protected int adjustParamlengthIfRequired(int listSize, int paramValue) {
        if (paramValue > listSize) return listSize;
        else if (paramValue < 0) return 0;
        
        return paramValue;
    }

    protected boolean checkReminderNameExists(JSONArray reminderList, String reminderName) {
        for (int i = 0; i < reminderList.length(); i++) {
            JSONObject reminder = reminderList.getJSONObject(i);
            if (reminder.getString("name").equals(reminderName))
                return true;
        }

        return false;
    }

    protected int checkReminderIdExists(JSONArray reminderList, String reminderId) {
        for (int i = 0; i < reminderList.length(); i++) {
            JSONObject reminder = reminderList.getJSONObject(i);
            if (reminder.getString("_id").equals(reminderId))
                return i;
        }

        return -1;
    }

    protected Instant getUtcTime(String reminderUtcInString) {
        try {
            Instant time = Instant.parse(reminderUtcInString);
            return time;
        } catch (Exception err) {
            return null;
        }
    }

    protected void storeDataInList(JSONArray reminderList, String reminderId, String reminderName, String tagColor,
            boolean isCompleted, boolean isImportant, String reminderUtc, Frequency frequency, String reminderNote) {
        JSONObject reminder = new JSONObject();
        reminder.put("_id", reminderId);
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
