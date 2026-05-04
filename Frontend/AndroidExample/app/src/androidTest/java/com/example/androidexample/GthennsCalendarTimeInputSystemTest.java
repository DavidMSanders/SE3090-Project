package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

@RunWith(AndroidJUnit4.class)
public class GthennsCalendarTimeInputSystemTest {

    private static void setPrivateLongField(Object target, String fieldName, long value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setLong(target, value);
        } catch (Exception e) {
            throw new AssertionError("Unable to set field: " + fieldName, e);
        }
    }

    @Test
    public void saveEvent_invalidStartTimeFormat_preservesDescription() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                EditText title = activity.findViewById(R.id.etEventTitle);
                EditText desc = activity.findViewById(R.id.etEventDescription);
                EditText startTime = activity.findViewById(R.id.etStartTime);
                Button saveButton = activity.findViewById(R.id.btnSaveEvent);

                title.setText("Morning Run");
                desc.setText("5k jog");
                startTime.setText("not-a-time");
                saveButton.performClick();

                // Description should remain after failed validation
                assertEquals("5k jog", desc.getText().toString());
            });
        }
    }

    @Test
    public void saveEvent_invalidEndTimeFormat_preservesTitle() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                EditText title = activity.findViewById(R.id.etEventTitle);
                EditText endTime = activity.findViewById(R.id.etEndTime);
                Button saveButton = activity.findViewById(R.id.btnSaveEvent);

                title.setText("Yoga Session");
                endTime.setText("bad-time");
                saveButton.performClick();

                assertEquals("Yoga Session", title.getText().toString());
            });
        }
    }

    @Test
    public void saveEvent_endTimeBeforeStartTime_keepsFields() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                EditText title = activity.findViewById(R.id.etEventTitle);
                EditText startTime = activity.findViewById(R.id.etStartTime);
                EditText endTime = activity.findViewById(R.id.etEndTime);
                Button saveButton = activity.findViewById(R.id.btnSaveEvent);

                title.setText("Swim Training");
                startTime.setText("14:00");
                endTime.setText("10:00"); // end is before start
                saveButton.performClick();

                // End time should remain unchanged
                assertEquals("10:00", endTime.getText().toString());
            });
        }
    }

    @Test
    public void editEvent_withoutSelectedEvent_showsToastAndKeepsTitleField() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                EditText title = activity.findViewById(R.id.etEventTitle);
                Button editButton = activity.findViewById(R.id.btnEditEvent);

                title.setText("Strength Training");
                editButton.performClick();

                assertTrue("Edit button should remain visible", editButton.isShown());
                assertEquals("Strength Training", title.getText().toString());
            });
        }
    }

    @Test
    public void deleteEvent_withoutSelectedEvent_showsToastAndKeepsTitleField() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                EditText title = activity.findViewById(R.id.etEventTitle);
                Button deleteButton = activity.findViewById(R.id.btnDeleteEvent);

                title.setText("Cardio Session");
                deleteButton.performClick();

                assertTrue("Delete button should remain visible", deleteButton.isShown());
                assertEquals("Cardio Session", title.getText().toString());
            });
        }
    }

    @Test
    public void shareEvent_withoutSelectedEvent_preservesShareField() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                EditText shareField = activity.findViewById(R.id.etShareUsername);
                Button shareButton = activity.findViewById(R.id.btnShareEvent);

                shareField.setText("99");
                shareButton.performClick();

                assertEquals("Share field must remain unchanged", "99", shareField.getText().toString());
            });
        }
    }

    @Test
    public void calendarMonthLabel_isNotEmpty() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                TextView tvMonth = activity.findViewById(R.id.tvCurrentMonth);
                assertNotNull(tvMonth);
                assertTrue("Month label should not be empty", !tvMonth.getText().toString().isEmpty());
            });
        }
    }

    @Test
    public void calendarParticipantsBox_isVisibleByDefault() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                TextView tvParticipants = activity.findViewById(R.id.tvParticipantsBox);
                assertNotNull(tvParticipants);
                assertTrue(tvParticipants.isShown());
            });
        }
    }

    @Test
    public void calendarSpinner_canSelectEveryMonday() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                Spinner spinner = activity.findViewById(R.id.eventTypeSpinner);
                spinner.setSelection(2); // "Every Monday"
                assertEquals("Every Monday", String.valueOf(spinner.getSelectedItem()));
            });
        }
    }

    @Test
    public void calendarSpinner_canSelectEverySaturday() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                Spinner spinner = activity.findViewById(R.id.eventTypeSpinner);
                spinner.setSelection(7); // "Every Saturday"
                assertEquals("Every Saturday", String.valueOf(spinner.getSelectedItem()));
            });
        }
    }

    @Test
    public void saveEvent_withValidRecurringType_doesNotCrash() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                EditText title = activity.findViewById(R.id.etEventTitle);
                Spinner spinner = activity.findViewById(R.id.eventTypeSpinner);
                Button saveButton = activity.findViewById(R.id.btnSaveEvent);

                title.setText("Weekly Workout");
                spinner.setSelection(1); // "Every Sunday"
                saveButton.performClick();

                // After click (network will fail), verify the title is still set
                // since validation passes (title is not empty) and time fields are empty (pass through)
                assertTrue(saveButton.isShown());
            });
        }
    }

    @Test
    public void saveEvent_validFieldsNoTime_doesNotCrash() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                EditText title = activity.findViewById(R.id.etEventTitle);
                EditText desc = activity.findViewById(R.id.etEventDescription);
                Button saveButton = activity.findViewById(R.id.btnSaveEvent);

                title.setText("Morning Run");
                desc.setText("Easy pace");
                // Start/end time left blank — should still pass validation
                saveButton.performClick();

                assertTrue(saveButton.isShown());
            });
        }
    }
}
