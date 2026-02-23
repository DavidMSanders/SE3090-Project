package com.example.androidexample;
import android.widget.CalendarView;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class Calendar extends Activity {

    CalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener(
                new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(CalendarView view,
                                                    int year,
                                                    int month,
                                                    int dayOfMonth) {

                        // month is 0-based
                        String date = (month + 1) + "/" + dayOfMonth + "/" + year;

                        Toast.makeText(Calendar.this,
                                "Selected: " + date,
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}
//todo: decide wether to make separate page for adding calendar info
//todo: add ability to add dates/stuff
//todo: add info pulled from said dates (coordinate with backend)
