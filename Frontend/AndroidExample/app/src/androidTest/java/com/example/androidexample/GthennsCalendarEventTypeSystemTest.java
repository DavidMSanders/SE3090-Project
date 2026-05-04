package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GthennsCalendarEventTypeSystemTest {

    @Test
    public void calendarEventTypeSpinner_hasExpectedDefaultAndOptions() {
        try (ActivityScenario<CalendarActivity> scenario = ActivityScenario.launch(CalendarActivity.class)) {
            scenario.onActivity(activity -> {
                Spinner spinner = activity.findViewById(R.id.eventTypeSpinner);
                assertTrue("Event type spinner should be visible", spinner.isShown());

                ArrayAdapter<?> adapter = (ArrayAdapter<?>) spinner.getAdapter();
                assertEquals("Spinner should include all recurring options", 8, adapter.getCount());
                assertEquals("Default event type should be Single", "Single", String.valueOf(spinner.getSelectedItem()));
                assertEquals("Last option should be Every Saturday", "Every Saturday", String.valueOf(adapter.getItem(7)));
            });
        }
    }
}