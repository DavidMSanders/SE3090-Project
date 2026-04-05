
package com.example.androidexample;

import android.util.Log;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatWebSocketManager {

    private WebSocket webSocket;
    private final OkHttpClient client = new OkHttpClient();

    private GroupEventListener eventListener;
    private Integer activeGroupId = null;

    public interface SocketConnectListener {
        void onConnected();
        void onError(String errorMessage);
    }

    public interface GroupEventListener {
        void onEventReceived(ChatSocketEvent event);
    }

    public void connect(int userId, SocketConnectListener listener) {
        //confirmed uses userId not username
        String url = "ws://coms-3090-035.class.las.iastate.edu:8080/chat/"
                + userId;
        Log.d("CHAT_TRACE", "connect() called. url=" + url);

        Request request = new Request.Builder().url(url).build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d("CHAT_TRACE", "WebSocket onOpen()");
                listener.onConnected();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("WS_IN", "RAW: " + text);
                try {
                    JSONObject json = new JSONObject(text);
                    ChatSocketEvent event = ChatSocketEvent.fromJson(json);

                    if (eventListener != null) {
                        boolean isGlobalEvent =
                                "CONNECTED".equals(event.getEventType()) ||
                                        "ERROR".equals(event.getEventType());

                        boolean passesFilter =
                                isGlobalEvent ||
                                        event.getGroupId() == null ||
                                        activeGroupId == null ||
                                        event.getGroupId().equals(activeGroupId);

                        Log.d("CHAT_TRACE", "eventListener != null, passesFilter=" + passesFilter);

                        if (passesFilter) {
                            eventListener.onEventReceived(event);
                        }
                    } else {
                        Log.d("CHAT_TRACE", "eventListener is null when message arrived");
                    }
                } catch (Exception e) {
                    //logged "Invalid JSON" for all errors
                    // logs the actual error message now for debugging
                    Log.e("CHAT_TRACE", "Error parsing socket message: " + text, e);
                    Log.e("WS", "Error parsing event: " + text +
                            " — " + e.getMessage());
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t,
                                  Response response) {
                Log.e("CHAT_TRACE", "WebSocket onFailure()", t);
                listener.onError(t.getMessage());
            }
        });
    }

    public void setGroupEventListener(int groupId,
                                      GroupEventListener listener) {
        this.activeGroupId = groupId;
        this.eventListener = listener;
        Log.d("CHAT_TRACE", "setGroupEventListener(): activeGroupId=" + activeGroupId);
    }

    public void sendGroupMessage(SendMessageRequest request) {
        if (webSocket != null) {
            //log what is being sent for debugging
            String json = request.toJson().toString();
            Log.d("WS", "Sending: " + json);
            webSocket.send(json);
        } else {
            //log if webSocket is null
            Log.e("WS", "Cannot send — webSocket is null");
        }
    }

    public void sendPermissionUpdate(JSONObject payload) {
        if (webSocket != null) {
            String json = payload.toString();
            Log.d("WS", "Sending permission update: " + json);
            webSocket.send(json);
        } else {
            Log.e("WS", "Cannot send permission update — webSocket is null");
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Closed");
            // clear after disconnect
            webSocket = null;
        }
    }
}