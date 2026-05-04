package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GthennsCalendarShareWithoutSelectedEventSystemTest {

    @Test
    public void calendarShareWithoutSelectedEvent_keepsInviteFieldText() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                EditText shareField = activity.findViewById(R.id.etShareUsername);
                shareField.setText("42");

                // No event is selected by default (selectedEventId == -1), so share must be rejected.
                activity.findViewById(R.id.btnShareEvent).performClick();

                assertEquals("Share field should remain unchanged when share is rejected",
                        "42", shareField.getText().toString());
                assertTrue("Share button should remain visible", activity.findViewById(R.id.btnShareEvent).isShown());
            });
        }
    }
}