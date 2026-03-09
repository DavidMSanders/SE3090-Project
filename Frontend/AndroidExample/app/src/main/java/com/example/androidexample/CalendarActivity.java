package com.example.androidexample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import android.graphics.Color;

public class CalendarActivity extends Activity {

    private CompactCalendarView calendarView;
    private EditText etEventDescription;
    private Button btnSaveEvent;
    private Button btnEditEvent;
    private Button btnDeleteEvent;
    private String selectedDate;
    private Date selectedDateObj;
    // Store events to display
    private List<Event> events = new ArrayList<>();

    // api url
    private String url = "https://0816ce3d-eb23-49ce-997e-9ec777f127c6.mock.pstmn.io/events";

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = (CompactCalendarView) findViewById(R.id.calendarView);
        etEventDescription = findViewById(R.id.etEventDescription);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);
        btnEditEvent = findViewById(R.id.btnEditEvent);
        btnDeleteEvent = findViewById(R.id.btnDeleteEvent);

        // Initialize selectedDate with today's date
        selectedDateObj = new Date();
        LocalDate localDate = selectedDateObj.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        selectedDate = localDate.format(formatter);
        
        // Set up calendar
        calendarView.setUseThreeLetterAbbreviation(true);

        // Get existing events for the month (Dots)
        getRequest();
        
        // Fetch details for the initial date (Today)
        getDayEvent(selectedDate);

        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                selectedDateObj = dateClicked;
                LocalDate localDate = dateClicked.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                selectedDate = localDate.format(formatter);
                Toast.makeText(CalendarActivity.this, "Selected: " + selectedDate, Toast.LENGTH_SHORT).show();

                // Fetch details from backend for this specific day
                getDayEvent(selectedDate);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                // Month scrolled, maybe refresh month dots?
                Log.d("Calendar", "Month was scrolled to: " + firstDayOfNewMonth);
                getRequest(); // Refresh dots for the new month view
            }
        });

        btnSaveEvent.setOnClickListener(v -> postRequest());
        btnEditEvent.setOnClickListener(v -> editRequest());
        btnDeleteEvent.setOnClickListener(v -> deleteRequest());
    }

    // Method to handle GET request (Fetch events for month to show dots)
    private void getRequest() {
        // The backend endpoint is /months
        String getUrl = url + "/months";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getUrl, null,
                response -> {
                    Log.d("CalendarGet", "Month dots response: " + response.toString());
                    calendarView.removeAllEvents();
                    events.clear();
                    
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject eventObj = response.getJSONObject(i);
                            String dateStr = eventObj.optString("date");
                            String notes = eventObj.optString("notes");
                            String workoutType = eventObj.optString("workoutType");
                            
                            if (dateStr == null || dateStr.isEmpty()) continue;
                            
                            try {
                                LocalDate date = LocalDate.parse(dateStr); 
                                long millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                                
                                // For dots, the description isn't critical but we store it anyway
                                String desc = (notes != null && !notes.isEmpty()) ? notes : workoutType;
                                Event ev = new Event(Color.RED, millis, desc);
                                calendarView.addEvent(ev);
                                events.add(ev);
                            } catch (Exception e) {
                                Log.e("CalendarGet", "Date Parse Error: " + dateStr, e);
                            }
                        }
                    } catch (JSONException e) {
                         Log.e("CalendarGet", "JSON Parse Error", e);
                    }
                },
                error -> {
                    Log.e("CalendarGet", "Error fetching month events", error);
                    // Toast removed to avoid spamming user on scroll
                });
        Volley.newRequestQueue(this).add(request);
    }

    // New Method: Fetch specific details for a selected day
    private void getDayEvent(String dateString) {
        String backendDate = formatDateForBackend(dateString);
        // Endpoint: /api/calendar/date/{userId}/{date}
        String dayUrl = url + "/date/1/" + backendDate;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, dayUrl, null,
                response -> {
                    Log.d("CalendarDay", "Day details response: " + response.toString());
                    etEventDescription.setText(""); // Clear first
                    
                    try {
                        if (response.length() > 0) {
                            // Backend returns a List<CalendarEvent>, which comes as a JSON Array
                            // We will take the first event for now, or combine descriptions
                            JSONObject eventObj = response.getJSONObject(0);
                            
                            String notes = eventObj.optString("notes");
                            String workoutType = eventObj.optString("workoutType");
                            
                            // Display logic: prioritize notes
                            String display = "";
                            if (notes != null && !notes.isEmpty()) {
                                display = notes;
                            } else if (workoutType != null && !workoutType.isEmpty()) {
                                display = workoutType;
                            }
                            etEventDescription.setText(display);
                        } else {
                            etEventDescription.setText(""); // No events for this day
                        }
                    } catch (JSONException e) {
                         Log.e("CalendarDay", "JSON Parse Error", e);
                         etEventDescription.setText("Error loading details");
                    }
                },
                error -> {
                    // On 404 or empty list, it means no event
                    Log.e("CalendarDay", "Error or No Event found: " + error.getMessage());
                    etEventDescription.setText(""); 
                });
        Volley.newRequestQueue(this).add(request);
    }

    // Helper method to convert date to backend format
    private String formatDateForBackend(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString, formatter);
            return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
            Log.e("Calendar", "Date conversion error", e);
            return dateString;
        }
    } //format date to match backend 

    // Method to handle POST request
    private void postRequest() {
        Log.d("CalendarPost", "Preparing request...");
        final String finalDate = formatDateForBackend(selectedDate);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("CalendarPost", "Response received: " + response);
                    Toast.makeText(CalendarActivity.this, "Event Saved", Toast.LENGTH_LONG).show();
                    etEventDescription.setText(""); // Clear input
                    getRequest(); // Refresh calendar
                },
                error -> {
                    Log.e("CalendarPost", "Error response received", error);
                    String errorMsg = (error.getMessage() != null ? error.getMessage() : "Unknown error");
                    Toast.makeText(CalendarActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userId", "1"); // Hardcoded userId fort t=esting
                params.put("date", finalDate);
                params.put("workoutType", "General"); // Default value
                params.put("durationMinutes", "60"); // Default value
                params.put("notes", etEventDescription.getText().toString());
                return params;
            }
        };
        
        Log.d("CalendarPost", "Sending request...");
        Volley.newRequestQueue(this).add(request);
    }

    // Method to handle PUT request (Edit event)
    private void editRequest() {
        Log.d("CalendarPut", "Preparing request...");
        
        final String finalDate = formatDateForBackend(selectedDate);

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> {
                    Log.d("CalendarPut", "Response received: " + response);
                    Toast.makeText(CalendarActivity.this, "Event Updated", Toast.LENGTH_LONG).show();
                    etEventDescription.setText(""); 
                    getRequest(); // Refresh calendar events because bad things happen
                },
                error -> {
                    Log.e("CalendarPut", "Error response received", error);
                    String errorMsg = (error.getMessage() != null ? error.getMessage() : "Unknown error");
                    Toast.makeText(CalendarActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userId", "1"); // Hardcoded userId
                params.put("date", finalDate);
                params.put("workoutType", "General"); 
                params.put("durationMinutes", "60");
                params.put("notes", etEventDescription.getText().toString());
                return params;
            } //updated send to match the backend format
        };
        Log.d("CalendarPut", "Sending request...");
        Volley.newRequestQueue(this).add(request);
    }

    // Method to handle DELETE request
    private void deleteRequest() {
        Log.d("CalendarDelete", "Preparing request...");
        
        final String finalDate = formatDateForBackend(selectedDate);
        
        // i think this is what the delete should look like, will change if no
        String deleteUrl = url + "?userId=1&date=" + finalDate;

        StringRequest request = new StringRequest(Request.Method.DELETE, deleteUrl,
                response -> {
                    Log.d("CalendarDelete", "Response received: " + response);
                    Toast.makeText(CalendarActivity.this, "Event Deleted", Toast.LENGTH_LONG).show();
                    etEventDescription.setText(""); // Clear input
                    getRequest(); // Refresh calendar events
                },
                error -> {
                    Log.e("CalendarDelete", "Error response received", error);
                    String errorMsg = (error.getMessage() != null ? error.getMessage() : "Unknown error");
                    Toast.makeText(CalendarActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                });
        
        Log.d("CalendarDelete", "Sending request to: " + deleteUrl);
        Volley.newRequestQueue(this).add(request);
    }
}
