package com.example.androidexample;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import android.graphics.Color;

public class CalendarActivity extends Activity {

    private CompactCalendarView calendarView;
    private TextView tvCurrentMonth;
    private EditText etEventTitle;
    private EditText etEventDescription;
    private EditText etStartTime;
    private EditText etEndTime;
    private Button btnSaveEvent;
    private Button btnEditEvent;
    private Button btnDeleteEvent;
    private Button btnShareEvent;
    private EditText etShareUsername;
    private TextView tvParticipantsBox;
    private Spinner eventTypeSpinner;

    private String selectedDate;
    private Date selectedDateObj;
    // Store events to display
    private List<Event> events = new ArrayList<>();
    private long selectedEventId = -1;

    // api url
    private static final String BASE_URL = "http://coms-3090-035.class.las.iastate.edu:8080";
    private long CURRENT_USER_ID;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());
    private final DateTimeFormatter uiTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter apiTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        AppTopBarController.setup(this);

        String userIdStr = new KeepUser(this).getUserIdString();
        if (userIdStr.isEmpty()) {
            userIdStr = SessionManager.getUserId(this);
        }
        try {
            CURRENT_USER_ID = userIdStr.isEmpty() ? 1 : Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            Log.e("Calendar", "Invalid user id, using fallback", e);
            CURRENT_USER_ID = 1;
            Toast.makeText(this, "Invalid user session, using default account", Toast.LENGTH_SHORT).show();
        }

        calendarView = (CompactCalendarView) findViewById(R.id.calendarView);
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth);
        etEventTitle = findViewById(R.id.etEventTitle);
        etEventDescription = findViewById(R.id.etEventDescription);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);
        btnEditEvent = findViewById(R.id.btnEditEvent);
        btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
        btnShareEvent = findViewById(R.id.btnShareEvent);
        etShareUsername = findViewById(R.id.etShareUsername);
        tvParticipantsBox = findViewById(R.id.tvParticipantsBox);
        eventTypeSpinner = findViewById(R.id.eventTypeSpinner);

        String[] eventTypes = {"Single", "Every Sunday", "Every Monday", "Every Tuesday", "Every Wednesday", "Every Thursday", "Every Friday", "Every Saturday"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, eventTypes);
        eventTypeSpinner.setAdapter(adapter);

        // Initialize selectedDate with today's date
        selectedDateObj = new Date();
        LocalDate localDate = selectedDateObj.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        selectedDate = localDate.format(formatter);
        updateCurrentMonthLabel(selectedDateObj);
        
        // Set up calendar
        calendarView.setUseThreeLetterAbbreviation(true);

        // Get existing events for current month
        getRequest(selectedDateObj);
        
        // Show details for the initial date (Today)
        showEventsForDate(selectedDateObj);

        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                selectedDateObj = dateClicked;
                LocalDate localDate = dateClicked.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                selectedDate = localDate.format(formatter);
                Toast.makeText(CalendarActivity.this, "Selected: " + selectedDate, Toast.LENGTH_SHORT).show();

                // Show details from loaded events
                showEventsForDate(dateClicked);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                Log.d("Calendar", "Month was scrolled to: " + firstDayOfNewMonth);
                updateCurrentMonthLabel(firstDayOfNewMonth);
                getRequest(firstDayOfNewMonth); // Refresh events for visible month
            }
        });

        btnSaveEvent.setOnClickListener(v -> postRequest());
        btnEditEvent.setOnClickListener(v -> editRequest());
        btnDeleteEvent.setOnClickListener(v -> deleteRequest());
          btnShareEvent.setOnClickListener(v -> shareRequest());
    }

    // Fetch events for the month shown by the calendar
    // GET /api/v1/users/{userId}/events/month?year=YYYY&month=M
    private void getRequest(Date monthDate) {
        Date targetDate = (monthDate != null) ? monthDate : new Date();
        LocalDate targetLocalDate = targetDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year = targetLocalDate.getYear();
        int month = targetLocalDate.getMonthValue();

        String getUrl = BASE_URL + "/api/v1/users/" + CURRENT_USER_ID + "/events/month?year=" + year + "&month=" + month;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, getUrl, null,
                response -> {
                    Log.d("CalendarGet", "Response: " + response.toString());
                    calendarView.removeAllEvents();
                    events.clear();
                    
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject eventObj = response.getJSONObject(i);
                            String dateStr = eventObj.optString("date");
                            
                            if (dateStr == null || dateStr.isEmpty()) continue;
                            
                            try {
                                LocalDate date = LocalDate.parse(dateStr); 
                                long millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                                
                                // Store the whole JSONObject as data in the Event
                                Event ev = new Event(Color.RED, millis, eventObj);
                                calendarView.addEvent(ev);
                                events.add(ev);
                            } catch (Exception e) {
                                Log.e("CalendarGet", "Date Parse Error: " + dateStr, e);
                            }
                        }
                        // Refresh UI for currently selected date after loading
                        showEventsForDate(selectedDateObj);
                    } catch (JSONException e) {
                         Log.e("CalendarGet", "JSON Parse Error", e);
                    }
                },
                error -> {
                    Log.e("CalendarGet", "Error fetching events", error);
                    Toast.makeText(CalendarActivity.this,
                        extractServerErrorMessage(error, "Error fetching events"),
                        Toast.LENGTH_LONG).show();
                });
            enqueue(request);
    }

    // Helper to show events for a selected day from local list
    private void showEventsForDate(Date date) {
        List<Event> dayEvents = calendarView.getEvents(date);
        etEventTitle.setText("");
        etEventDescription.setText("");
        etStartTime.setText("");
        etEndTime.setText("");
        tvParticipantsBox.setText("Select an event to view participants.");
        selectedEventId = -1; // Reset selection

        if (dayEvents != null && !dayEvents.isEmpty()) {
            Event ev = dayEvents.get(0); // Just showing the first one for now
            if (ev.getData() instanceof JSONObject) {
                JSONObject data = (JSONObject) ev.getData();
                String title = data.optString("title");
                String notes = data.optString("notes", data.optString("note", ""));
                String startTime = data.optString("startTime");
                String endTime = data.optString("endTime");

                etEventTitle.setText(title);
                etEventDescription.setText(notes);
                etStartTime.setText(formatTimeForUi(startTime));
                etEndTime.setText(formatTimeForUi(endTime));

                selectedEventId = parseEventId(data);
                fetchParticipants(selectedEventId);
            }
        }
    }

    private void fetchParticipants(long eventId) {
        String url = BASE_URL + "/api/v1/events/" + eventId + "/participants";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> renderParticipants(response),
                error -> {
                    Log.e("CalendarParticipants", "Error fetching participants", error);
                    tvParticipantsBox.setText("Unable to load participants.");
                });
        enqueue(request);
    }

    private void renderParticipants(JSONArray response) {
        if (response == null || response.length() == 0) {
            tvParticipantsBox.setText("No participants yet.");
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < response.length(); i++) {
            JSONObject participant = response.optJSONObject(i);
            if (participant == null) {
                continue;
            }

            String username = participant.optString("username", "").trim();
            String status = participant.optString("status", "INVITED").trim();
            long userId = participant.optLong("userId", -1);

            String label = username.isEmpty() ? (userId == -1 ? "Unknown user" : "User " + userId) : username;
            builder.append("- ").append(label);
            if (!status.isEmpty()) {
                builder.append(" (").append(status).append(")");
            }
            if (i < response.length() - 1) {
                builder.append("\n");
            }
        }

        tvParticipantsBox.setText(builder.toString());
    }

    private String formatTimeForUi(String apiValue) {
        if (apiValue == null || apiValue.trim().isEmpty()) {
            return "";
        }
        try {
            return LocalTime.parse(apiValue.trim()).format(uiTimeFormatter);
        } catch (Exception ignored) {
            return apiValue;
        }
    }

    private String normalizeTimeForApi(EditText input, String label) {
        String raw = input.getText().toString().trim();
        if (raw.isEmpty()) {
            return "";
        }

        try {
            LocalTime parsed;
            if (raw.length() == 5) {
                parsed = LocalTime.parse(raw, uiTimeFormatter);
            } else {
                parsed = LocalTime.parse(raw);
            }
            return parsed.format(apiTimeFormatter);
        } catch (Exception e) {
            Toast.makeText(this, label + " must be HH:mm", Toast.LENGTH_SHORT).show();
            input.requestFocus();
            return null;
        }
    }

    private boolean validateTimeRange(String startTime, String endTime) {
        if (startTime.isEmpty() || endTime.isEmpty()) {
            return true;
        }

        LocalTime start = LocalTime.parse(startTime, apiTimeFormatter);
        LocalTime end = LocalTime.parse(endTime, apiTimeFormatter);
        if (end.isBefore(start)) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
            etEndTime.requestFocus();
            return false;
        }
        return true;
    }

    private long parseEventId(JSONObject eventObj) {
        long id = eventObj.optLong("id", -1);
        if (id != -1) {
            return id;
        }
        return eventObj.optLong("eventId", -1);
    }

    private void updateCurrentMonthLabel(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        tvCurrentMonth.setText(localDate.format(monthFormatter));
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
    } 

    // Method to handle POST request (Create single or recurring event)
    private void postRequest() {
        Log.d("CalendarPost", "Preparing request...");
        final String finalDate = formatDateForBackend(selectedDate);
        String eventType = eventTypeSpinner.getSelectedItem().toString();
        String eventTitle = etEventTitle.getText().toString().trim();
        String eventNotes = etEventDescription.getText().toString().trim();
        String startTime = normalizeTimeForApi(etStartTime, "Start time");
        String endTime = normalizeTimeForApi(etEndTime, "End time");

        if (startTime == null || endTime == null) {
            return;
        }

        if (eventTitle.isEmpty()) {
            Toast.makeText(this, "Enter a workout title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validateTimeRange(startTime, endTime)) {
            return;
        }
        
        String url;
        if (eventType.equals("Single")) {
            url = BASE_URL + "/api/v1/events";
        } else {
            url = BASE_URL + "/api/v1/events/recurring";
        }

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("userId", CURRENT_USER_ID);
            requestBody.put("date", finalDate);
            requestBody.put("title", eventTitle);
            requestBody.put("notes", eventNotes);
            requestBody.put("note", eventNotes);
            if (!startTime.isEmpty()) {
                requestBody.put("startTime", startTime);
            }
            if (!endTime.isEmpty()) {
                requestBody.put("endTime", endTime);
            }
            
            if (!eventType.equals("Single")) {
                 String dayOfWeek = eventType.replace("Every ", "").toUpperCase();
                 requestBody.put("dayOfWeek", dayOfWeek);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    Log.d("CalendarPost", "Response received: " + response);
                    Toast.makeText(CalendarActivity.this, "Event Saved (" + eventType + ")", Toast.LENGTH_LONG).show();
                    etEventTitle.setText("");
                    etEventDescription.setText(""); 
                    etStartTime.setText("");
                    etEndTime.setText("");
                    getRequest(selectedDateObj); // Refresh current month
                },
                error -> {
                    Log.e("CalendarPost", "Error response received", error);
                    Toast.makeText(CalendarActivity.this,
                        extractServerErrorMessage(error, "Error Saving Event"),
                        Toast.LENGTH_LONG).show();
                });
        
        Log.d("CalendarPost", "Sending request...");
            enqueue(request);
    }

    // Method to handle PUT request (Edit event)
    // PUT /api/v1/events/{eventId}
    private void editRequest() {
        if (selectedEventId == -1) {
            Toast.makeText(this, "No event selected to edit", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d("CalendarPut", "Preparing request...");
        String putUrl = BASE_URL + "/api/v1/events/" + selectedEventId;
        
        final String finalDate = formatDateForBackend(selectedDate);
        String eventTitle = etEventTitle.getText().toString().trim();
        String eventNotes = etEventDescription.getText().toString().trim();
        String startTime = normalizeTimeForApi(etStartTime, "Start time");
        String endTime = normalizeTimeForApi(etEndTime, "End time");

        if (startTime == null || endTime == null) {
            return;
        }

        if (eventTitle.isEmpty()) {
            Toast.makeText(this, "Enter a workout title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validateTimeRange(startTime, endTime)) {
            return;
        }

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("userId", CURRENT_USER_ID);
            requestBody.put("date", finalDate);
            requestBody.put("title", eventTitle);
            requestBody.put("notes", eventNotes);
            requestBody.put("note", eventNotes);
            if (!startTime.isEmpty()) {
                requestBody.put("startTime", startTime);
            }
            if (!endTime.isEmpty()) {
                requestBody.put("endTime", endTime);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, putUrl, requestBody,
                response -> {
                    Log.d("CalendarPut", "Response received: " + response);
                    Toast.makeText(CalendarActivity.this, "Event Updated", Toast.LENGTH_LONG).show();
                    etEventTitle.setText("");
                    etEventDescription.setText(""); 
                    etStartTime.setText("");
                    etEndTime.setText("");
                    getRequest(selectedDateObj);
                },
                error -> {
                    Log.e("CalendarPut", "Error response received", error);
                    Toast.makeText(CalendarActivity.this,
                        extractServerErrorMessage(error, "Error Updating Event"),
                        Toast.LENGTH_LONG).show();
                });

        Log.d("CalendarPut", "Sending request...");
            enqueue(request);
    }

    // Method to handle DELETE request
    // DELETE /api/v1/events/{eventId}
    private void deleteRequest() {
        if (selectedEventId == -1) {
            Toast.makeText(this, "No event selected to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("CalendarDelete", "Preparing request...");
        String deleteUrl = BASE_URL + "/api/v1/events/" + selectedEventId;

        StringRequest request = new StringRequest(Request.Method.DELETE, deleteUrl,
                response -> {
                    Log.d("CalendarDelete", "Response received: " + response);
                    Toast.makeText(CalendarActivity.this, "Event Deleted", Toast.LENGTH_LONG).show();
                    etEventTitle.setText("");
                    etEventDescription.setText(""); 
                    etStartTime.setText("");
                    etEndTime.setText("");
                    getRequest(selectedDateObj);
                },
                error -> {
                    Log.e("CalendarDelete", "Error response received", error);
                    Toast.makeText(CalendarActivity.this,
                        extractServerErrorMessage(error, "Error Deleting Event"),
                        Toast.LENGTH_LONG).show();
                });
        
        Log.d("CalendarDelete", "Sending request to: " + deleteUrl);
            enqueue(request);
    }

    // Method to add a participant to the selected event by userId
    // POST /api/v1/events/{eventId}/participants
    private void shareRequest() {
        if (selectedEventId == -1) {
            Toast.makeText(this, "No event selected to share", Toast.LENGTH_SHORT).show();
            return;
        }

        String inviteUserIdText = etShareUsername.getText().toString().trim();
        if (inviteUserIdText.isEmpty()) {
            Toast.makeText(this, "Enter a user ID to invite", Toast.LENGTH_SHORT).show();
            return;
        }

        long inviteUserId;
        try {
            inviteUserId = Long.parseLong(inviteUserIdText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "User ID must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (inviteUserId == CURRENT_USER_ID) {
            Toast.makeText(this, "You are already the event owner", Toast.LENGTH_SHORT).show();
            return;
        }

        String shareUrl = BASE_URL + "/api/v1/events/" + selectedEventId + "/participants";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("userId", inviteUserId);
        } catch (JSONException e) {
            Log.e("CalendarShare", "Failed to build share payload", e);
            Toast.makeText(this, "Unable to share event", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, shareUrl, requestBody,
                response -> {
                    Log.d("CalendarShare", "Response received: " + response);
                String username = response.optString("username", "").trim();
                String status = response.optString("status", "INVITED").trim();
                String label = username.isEmpty() ? ("userId=" + inviteUserId) : username;
                Toast.makeText(
                    CalendarActivity.this,
                    "Participant invited: " + label + " (" + status + ")",
                    Toast.LENGTH_LONG
                ).show();
                    etShareUsername.setText("");
                    fetchParticipants(selectedEventId);
                },
                error -> {
                    Log.e("CalendarShare", "Error response received", error);
                    Toast.makeText(CalendarActivity.this,
                            extractServerErrorMessage(error, "Error sharing event"),
                            Toast.LENGTH_LONG).show();
                });

        Log.d("CalendarShare", "Sending request to: " + shareUrl);
        enqueue(request);
    }

    private void enqueue(Request<?> request) {
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private String extractServerErrorMessage(VolleyError error, String fallbackMessage) {
        if (error == null || error.networkResponse == null || error.networkResponse.data == null) {
            return fallbackMessage;
        }

        try {
            String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
            JSONObject obj = new JSONObject(responseBody);
            String message = obj.optString("message", "").trim();
            if (!message.isEmpty()) {
                return message;
            }
        } catch (Exception ignored) {
            // Fallback to generic message if server response is not JSON.
        }
        return fallbackMessage;
    }

}
