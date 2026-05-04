package com.example.androidexample;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public final class NotificationSyncManager {

    private static final String TAG = "NotificationSync";

    private static final String HTTP_BASE_URL = "http://coms-3090-035.class.las.iastate.edu:8080";
    private static final String WS_BASE_URL = "ws://coms-3090-035.class.las.iastate.edu:8080";
    private static final long WORKOUT_CHECK_INTERVAL_MS = 60_000L;
    private static final String WORKOUT_ALERT_PREFS = "workout_start_alerts";
    private static final String WORKOUT_ALERT_KEY_PREFIX = "sent_";

    private static NotificationSyncManager instance;

    private final Context appContext;
    private final RequestQueue requestQueue;
    private final OkHttpClient webSocketClient;
    private final Handler mainHandler;

    private WebSocket webSocket;
    private String activeUserId = "";
    private boolean closedByUser = false;
    private int reconnectAttempts = 0;

    private final Runnable workoutReminderRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (NotificationSyncManager.this) {
                if (closedByUser || activeUserId.isEmpty()) {
                    return;
                }
            }

            checkWorkoutStartAlerts(activeUserId);
            mainHandler.postDelayed(this, WORKOUT_CHECK_INTERVAL_MS);
        }
    };

    public interface NotificationListCallback {
        void onSuccess(List<String> notifications);
        void onError(String errorMessage);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    private NotificationSyncManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.requestQueue = VolleySingleton.getInstance(this.appContext).getRequestQueue();
        this.webSocketClient = new OkHttpClient.Builder().retryOnConnectionFailure(true).build();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized NotificationSyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationSyncManager(context);
        }
        return instance;
    }

    public synchronized void startForCurrentUser() {
        KeepUser keepUser = new KeepUser(appContext);
        if (!keepUser.isLoggedIn()) {
            return;
        }

        String userId = keepUser.getUserIdString();
        if (userId.isEmpty()) {
            return;
        }

        if (userId.equals(activeUserId) && webSocket != null) {
            return;
        }

        stopInternal(false);
        activeUserId = userId;
        closedByUser = false;

        fetchNotificationsForUser(userId, null);
        connectWebSocket(userId);
        startWorkoutReminderChecks(userId);
    }

    public synchronized void stop() {
        stopInternal(true);
    }

    public void fetchNotificationsForCurrentUser(NotificationListCallback callback) {
        String userId = new KeepUser(appContext).getUserIdString();
        if (userId.isEmpty()) {
            if (callback != null) {
                callback.onError("User id is not available");
            }
            return;
        }
        fetchNotificationsForUser(userId, callback);
    }

    public void fetchNotificationsForUser(String userId, NotificationListCallback callback) {
        String url = HTTP_BASE_URL + "/notifications/" + Uri.encode(userId);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<String> formatted = formatHistory(response);
                    AppNotificationManager.replaceNotifications(appContext, formatted, containsUnread(response));
                    if (callback != null) {
                        callback.onSuccess(formatted);
                    }
                },
                error -> {
                    String message = readableVolleyError(error);
                    if (callback != null) {
                        callback.onError(message);
                    }
                    Log.w(TAG, "fetchNotificationsForUser failed: " + message);
                });

        requestQueue.add(request);
    }

    public void fetchNotificationsByReadStatus(String userId, boolean isRead, NotificationListCallback callback) {
        String url = HTTP_BASE_URL + "/notifications/" + Uri.encode(userId) + "/filter?isRead=" + isRead;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<String> formatted = formatHistory(response);
                    if (callback != null) {
                        callback.onSuccess(formatted);
                    }
                },
                error -> {
                    String message = readableVolleyError(error);
                    if (callback != null) {
                        callback.onError(message);
                    }
                });

        requestQueue.add(request);
    }

    public void markAsRead(long notificationId, OperationCallback callback) {
        sendNoBodyRequest(Request.Method.PUT, HTTP_BASE_URL + "/notifications/" + notificationId + "/read", callback);
    }

    public void markAsUnread(long notificationId, OperationCallback callback) {
        sendNoBodyRequest(Request.Method.PUT, HTTP_BASE_URL + "/notifications/" + notificationId + "/unread", callback);
    }

    public void deleteNotification(long notificationId, OperationCallback callback) {
        sendNoBodyRequest(Request.Method.DELETE, HTTP_BASE_URL + "/notifications/" + notificationId, callback);
    }

    public void sendTestNotification(String senderId, String receiverId, String category, String message, OperationCallback callback) {
        String url = HTTP_BASE_URL + "/notifications/send"
                + "?senderId=" + Uri.encode(senderId)
                + "&receiverId=" + Uri.encode(receiverId)
                + "&category=" + Uri.encode(category)
                + "&message=" + Uri.encode(message);

        sendNoBodyRequest(Request.Method.GET, url, callback);
    }

    private synchronized void connectWebSocket(String userId) {
        String wsUrl = WS_BASE_URL + "/notifications/" + Uri.encode(userId);

        webSocket = webSocketClient.newWebSocket(new Builder().url(wsUrl).build(), new WebSocketListener() {
            @Override
            public void onOpen(WebSocket socket, Response response) {
                reconnectAttempts = 0;
                Log.d(TAG, "WebSocket connected for user=" + userId);
            }

            @Override
            public void onMessage(WebSocket socket, String text) {
                handleIncomingMessage(text);
            }

            @Override
            public void onMessage(WebSocket socket, ByteString bytes) {
                handleIncomingMessage(bytes.utf8());
            }

            @Override
            public void onClosing(WebSocket socket, int code, String reason) {
                socket.close(code, reason);
            }

            @Override
            public void onClosed(WebSocket socket, int code, String reason) {
                if (!closedByUser) {
                    scheduleReconnect(userId);
                }
            }

            @Override
            public void onFailure(WebSocket socket, Throwable t, Response response) {
                Log.w(TAG, "WebSocket failure: " + t.getMessage());
                if (!closedByUser) {
                    scheduleReconnect(userId);
                }
            }
        });
    }

    private synchronized void stopInternal(boolean clearUser) {
        closedByUser = true;
        reconnectAttempts = 0;
        mainHandler.removeCallbacksAndMessages(null);

        if (webSocket != null) {
            webSocket.close(1000, "Client closing");
            webSocket = null;
        }

        if (clearUser) {
            activeUserId = "";
        }
    }

    private synchronized void startWorkoutReminderChecks(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }

        mainHandler.removeCallbacks(workoutReminderRunnable);
        mainHandler.post(workoutReminderRunnable);
    }

    private void checkWorkoutStartAlerts(String userId) {
        LocalDate today = LocalDate.now();
        String url = HTTP_BASE_URL + "/api/v1/users/" + Uri.encode(userId)
                + "/events/day?year=" + today.getYear()
                + "&month=" + today.getMonthValue()
                + "&day=" + today.getDayOfMonth();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> processWorkoutAlerts(response),
                error -> Log.w(TAG, "Workout alert check failed: " + readableVolleyError(error)));

        requestQueue.add(request);
    }

    private void processWorkoutAlerts(JSONArray events) {
        if (events == null || events.length() == 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.optJSONObject(i);
            if (event == null) {
                continue;
            }

            String dateValue = event.optString("date", "").trim();
            String startValue = event.optString("startTime", "").trim();
            if (dateValue.isEmpty() || startValue.isEmpty()) {
                continue;
            }

            LocalDate eventDate;
            LocalTime startTime;
            try {
                eventDate = LocalDate.parse(dateValue);
                startTime = LocalTime.parse(startValue);
            } catch (Exception ignored) {
                continue;
            }

            LocalDateTime startDateTime = LocalDateTime.of(eventDate, startTime);
            if (now.isBefore(startDateTime) || !now.isBefore(startDateTime.plusMinutes(2))) {
                continue;
            }

            long eventId = extractEventId(event);
            String alertKey = eventId + "|" + dateValue + "|" + startValue;
            if (isWorkoutAlertSent(alertKey)) {
                continue;
            }

            String title = event.optString("title", "Workout").trim();
            if (title.isEmpty()) {
                title = "Workout";
            }

            AppNotificationManager.addNotification(appContext,
                    "\u0001Workout starting now: " + title + " at " + startTime.toString());
            markWorkoutAlertSent(alertKey);
        }
    }

    private long extractEventId(JSONObject event) {
        long id = event.optLong("id", -1);
        if (id != -1) {
            return id;
        }
        return event.optLong("eventId", -1);
    }

    private SharedPreferences getWorkoutAlertPrefs() {
        return appContext.getSharedPreferences(WORKOUT_ALERT_PREFS, Context.MODE_PRIVATE);
    }

    private String sentWorkoutAlertKey() {
        return WORKOUT_ALERT_KEY_PREFIX + activeUserId;
    }

    private boolean isWorkoutAlertSent(String alertKey) {
        Set<String> sent = getWorkoutAlertPrefs().getStringSet(sentWorkoutAlertKey(), new HashSet<>());
        return sent != null && sent.contains(alertKey);
    }

    private void markWorkoutAlertSent(String alertKey) {
        SharedPreferences prefs = getWorkoutAlertPrefs();
        Set<String> current = prefs.getStringSet(sentWorkoutAlertKey(), new HashSet<>());
        Set<String> updated = new HashSet<>(current != null ? current : new HashSet<>());
        updated.add(alertKey);
        prefs.edit().putStringSet(sentWorkoutAlertKey(), updated).apply();
    }

    private void scheduleReconnect(String userId) {
        reconnectAttempts++;
        long delayMs = Math.min(30000L, 1000L * reconnectAttempts);

        mainHandler.postDelayed(() -> {
            synchronized (NotificationSyncManager.this) {
                if (closedByUser) {
                    return;
                }
                if (!userId.equals(activeUserId)) {
                    return;
                }
                connectWebSocket(userId);
            }
        }, delayMs);
    }

    private void handleIncomingMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            return;
        }

        String formatted = parseIncomingNotification(rawMessage.trim());
        if (formatted.isEmpty()) {
            return;
        }

        AppNotificationManager.addNotification(appContext, formatted);
    }

    private String parseIncomingNotification(String raw) {
        try {
            JSONObject notification = new JSONObject(raw);
            return coreNotificationText(notification);
        } catch (Exception ignored) {
            return raw;
        }
    }

    private List<String> formatHistory(JSONArray response) {
        List<String> formatted = new ArrayList<>();
        if (response == null) {
            return formatted;
        }

        for (int i = 0; i < response.length(); i++) {
            JSONObject item = response.optJSONObject(i);
            if (item == null) {
                continue;
            }

            String timestamp = extractTimestamp(item);
            String content = coreNotificationText(item);
            if (content.isEmpty()) {
                continue;
            }

            if (timestamp.isEmpty()) {
                formatted.add(content);
            } else {
                // Keep style marker at front of the full stored string
                if (!content.isEmpty() && content.charAt(0) < '\u0020') {
                    formatted.add(content.charAt(0) + timestamp + " - " + content.substring(1));
                } else {
                    formatted.add(timestamp + " - " + content);
                }
            }
        }

        return formatted;
    }

    private boolean containsUnread(JSONArray response) {
        if (response == null) {
            return false;
        }

        for (int i = 0; i < response.length(); i++) {
            JSONObject item = response.optJSONObject(i);
            if (item == null) {
                continue;
            }

            if (item.has("isRead") && !item.optBoolean("isRead", true)) {
                return true;
            }
        }

        return false;
    }

    private String coreNotificationText(JSONObject item) {
        String category = extractCategoryName(item);
        String message = item.optString("message", "").trim();
        if (message.isEmpty()) {
            message = item.optString("content", "").trim();
        }

        String sender = extractSenderLabel(item);

        if (message.isEmpty()) {
            return "";
        }

        String text;
        if (!sender.isEmpty()) {
            text = String.format(Locale.US, "from %s: %s", sender, message);
        } else {
            text = message;
        }

        // Prefix special categories with hidden markers so the UI can style them
        if ("ALERT".equalsIgnoreCase(category)) {
            text = "\u0001" + text;
        } else if ("TEST".equalsIgnoreCase(category)) {
            text = "\u0002" + text;
        }
        return text;
    }

    private String extractCategoryName(JSONObject item) {
        JSONObject categoryObject = item.optJSONObject("category");
        if (categoryObject != null) {
            String categoryName = categoryObject.optString("name", "").trim();
            if (!categoryName.isEmpty()) {
                return categoryName;
            }
        }

        String category = item.optString("category", "").trim();
        return category.isEmpty() ? "NOTICE" : category;
    }

    private String extractSenderLabel(JSONObject item) {
        JSONObject userObject = item.optJSONObject("user");
        if (userObject != null) {
            String accountName = userObject.optString("accountName", "").trim();
            if (!accountName.isEmpty()) {
                return accountName;
            }

            Object userId = userObject.opt("userId");
            if (userId != null) {
                String userIdValue = String.valueOf(userId).trim();
                if (!userIdValue.isEmpty() && !"null".equalsIgnoreCase(userIdValue)) {
                    return userIdValue;
                }
            }
        }

        Object senderId = item.opt("senderId");
        if (senderId == null) {
            return "";
        }

        String sender = String.valueOf(senderId).trim();
        return "null".equalsIgnoreCase(sender) ? "" : sender;
    }

    private String extractTimestamp(JSONObject item) {
        String value = item.optString("createdAt", "").trim();
        if (value.isEmpty()) {
            value = item.optString("timestamp", "").trim();
        }
        if (value.isEmpty()) {
            return "";
        }

        // Shorten ISO timestamp (e.g. 2026-04-09T22:58:52.374...) → "04/09 22:58"
        try {
            int tIndex = value.indexOf('T');
            if (tIndex >= 8) {
                String datePart = value.substring(5, 10).replace('-', '/'); // MM/dd
                String timePart = value.substring(tIndex + 1, tIndex + 6);  // HH:mm
                return datePart + " " + timePart;
            }
        } catch (Exception ignored) {
            // fall through to plain value
        }
        return value.replace('T', ' ').replace("Z", "");
    }

    private void sendNoBodyRequest(int method, String url, OperationCallback callback) {
        StringRequest request = new StringRequest(method, url,
                response -> {
                    if (callback != null) {
                        callback.onSuccess();
                    }
                },
                error -> {
                    if (callback != null) {
                        callback.onError(readableVolleyError(error));
                    }
                });

        requestQueue.add(request);
    }

    private String readableVolleyError(com.android.volley.VolleyError error) {
        if (error == null) {
            return "Unknown error";
        }

        if (error.networkResponse != null) {
            return "HTTP " + error.networkResponse.statusCode;
        }

        if (error.getMessage() != null && !error.getMessage().trim().isEmpty()) {
            return error.getMessage().trim();
        }

        return error.toString();
    }
}
