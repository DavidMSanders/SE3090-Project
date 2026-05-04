package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

@RunWith(AndroidJUnit4.class)
public class GthennsCalendarValidationSystemTest {

    private static void setPrivateLongField(Object target, String fieldName, long value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setLong(target, value);
        } catch (Exception e) {
            throw new AssertionError("Unable to set field: " + fieldName, e);
        }
    }

//    @Test
//    public void calendarDefaultState_hasExpectedActionLabelsAndEmptyInputs() {
//        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
//            scenario.onActivity(activity -> {
//                Button saveButton = activity.findViewById(R.id.btnSaveEvent);
//                Button editButton = activity.findViewById(R.id.btnEditEvent);
//                Button deleteButton = activity.findViewById(R.id.btnDeleteEvent);
//                Button shareButton = activity.findViewById(R.id.btnShareEvent);
//                EditText description = activity.findViewById(R.id.etEventDescription);
//                EditText shareUserId = activity.findViewById(R.id.etShareUsername);
//                Spinner eventType = activity.findViewById(R.id.eventTypeSpinner);
//
//                assertEquals("Save", saveButton.getText().toString());
//                assertEquals("Edit", editButton.getText().toString());
//                assertEquals("Delete", deleteButton.getText().toString());
//                assertEquals("Invite to Workout", shareButton.getText().toString());
//                assertEquals("", description.getText().toString());
//                assertEquals("", shareUserId.getText().toString());
//                assertEquals("Single", String.valueOf(eventType.getSelectedItem()));
//            });
//        }
//    }

    @Test
    public void shareWithNonNumericUserId_keepsTypedInviteValue() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                EditText shareUserId = activity.findViewById(R.id.etShareUsername);
                Button shareButton = activity.findViewById(R.id.btnShareEvent);

                shareUserId.setText("not-a-number");
                shareButton.performClick();

                assertEquals("non numeric value should remain for user correction",
                        "not-a-number", shareUserId.getText().toString());
            });
        }
    }

    @Test
    public void deleteWithoutSelectedEvent_doesNotEraseDraftDescription() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                EditText description = activity.findViewById(R.id.etEventDescription);
                Button deleteButton = activity.findViewById(R.id.btnDeleteEvent);

                description.setText("Bike interval workout");
                deleteButton.performClick();

                assertTrue("Delete button should stay visible", deleteButton.isShown());
                assertEquals("Bike interval workout", description.getText().toString());
            });
        }
    }

    @Test
    public void saveWithEmptyTitle_preservesTypedDescriptionAndTimes() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                EditText title = activity.findViewById(R.id.etEventTitle);
                EditText description = activity.findViewById(R.id.etEventDescription);
                EditText startTime = activity.findViewById(R.id.etStartTime);
                EditText endTime = activity.findViewById(R.id.etEndTime);
                Button saveButton = activity.findViewById(R.id.btnSaveEvent);

                title.setText("");
                description.setText("Core workout");
                startTime.setText("09:00");
                endTime.setText("10:00");
                saveButton.performClick();

                assertEquals("Core workout", description.getText().toString());
                assertEquals("09:00", startTime.getText().toString());
                assertEquals("10:00", endTime.getText().toString());
            });
        }
    }

    @Test
    public void saveWithInvalidStartTime_preservesInvalidInputForCorrection() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                EditText title = activity.findViewById(R.id.etEventTitle);
                EditText startTime = activity.findViewById(R.id.etStartTime);
                EditText endTime = activity.findViewById(R.id.etEndTime);
                Button saveButton = activity.findViewById(R.id.btnSaveEvent);

                title.setText("Upper body");
                startTime.setText("9am");
                endTime.setText("10:00");
                saveButton.performClick();

                assertEquals("9am", startTime.getText().toString());
                assertEquals("10:00", endTime.getText().toString());
            });
        }
    }

    @Test
    public void saveWithEndBeforeStart_preservesBothTimeInputs() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                EditText title = activity.findViewById(R.id.etEventTitle);
                EditText startTime = activity.findViewById(R.id.etStartTime);
                EditText endTime = activity.findViewById(R.id.etEndTime);
                Button saveButton = activity.findViewById(R.id.btnSaveEvent);

                title.setText("Intervals");
                startTime.setText("14:00");
                endTime.setText("13:00");
                saveButton.performClick();

                assertEquals("14:00", startTime.getText().toString());
                assertEquals("13:00", endTime.getText().toString());
            });
        }
    }

    @Test
    public void shareWithEmptyUserId_whenEventSelected_keepsInviteFieldEmpty() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                setPrivateLongField(activity, "selectedEventId", 99L);

                EditText shareUserId = activity.findViewById(R.id.etShareUsername);
                Button shareButton = activity.findViewById(R.id.btnShareEvent);

                shareUserId.setText("");
                shareButton.performClick();

                assertEquals("", shareUserId.getText().toString());
                assertTrue(shareButton.isShown());
            });
        }
    }

    @Test
    public void shareWithCurrentUserId_whenEventSelected_preservesTypedUserId() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                setPrivateLongField(activity, "selectedEventId", 99L);
                setPrivateLongField(activity, "CURRENT_USER_ID", 1L);

                EditText shareUserId = activity.findViewById(R.id.etShareUsername);
                Button shareButton = activity.findViewById(R.id.btnShareEvent);

                shareUserId.setText("1");
                shareButton.performClick();

                assertEquals("1", shareUserId.getText().toString());
            });
        }
    }

//    @Test
//    public void calendarDefaultState_hasEmptyTitleTimesAndParticipantPlaceholder() {
//        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
//            scenario.onActivity(activity -> {
//                EditText title = activity.findViewById(R.id.etEventTitle);
//                EditText startTime = activity.findViewById(R.id.etStartTime);
//                EditText endTime = activity.findViewById(R.id.etEndTime);
//                TextView month = activity.findViewById(R.id.tvCurrentMonth);
//                TextView participants = activity.findViewById(R.id.tvParticipantsBox);
//
//                assertEquals("", title.getText().toString());
//                assertEquals("", startTime.getText().toString());
//                assertEquals("", endTime.getText().toString());
//                assertTrue(!month.getText().toString().trim().isEmpty());
//                assertEquals("Select an event to view participants.", participants.getText().toString());
//            });
//        }
//    }
}
