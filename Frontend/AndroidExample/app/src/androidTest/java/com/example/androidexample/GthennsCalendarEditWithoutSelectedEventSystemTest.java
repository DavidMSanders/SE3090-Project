package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GthennsCalendarEditWithoutSelectedEventSystemTest {

    @Test
    public void calendarEditWithoutSelectedEvent_keepsDescriptionText() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                EditText description = activity.findViewById(R.id.etEventDescription);
                description.setText("Leg day at 6 PM");

                // No event is selected by default (selectedEventId == -1), so edit must be rejected.
                activity.findViewById(R.id.btnEditEvent).performClick();

                assertEquals("Description should remain unchanged when edit is rejected",
                        "Leg day at 6 PM", description.getText().toString());
                assertTrue("Edit button should remain visible", activity.findViewById(R.id.btnEditEvent).isShown());
            });
        }
    }
}