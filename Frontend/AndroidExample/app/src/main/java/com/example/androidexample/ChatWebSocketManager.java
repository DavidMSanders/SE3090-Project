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

    public void connect(String username, SocketConnectListener listener) {

        String url = "ws://10.0.2.2:8080/chat/" + username;

        Request request = new Request.Builder().url(url).build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                listener.onConnected();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JSONObject json = new JSONObject(text);
                    ChatSocketEvent event = ChatSocketEvent.fromJson(json);

                    if (eventListener != null) {
                        if (event.getGroupId() == null || activeGroupId == null || event.getGroupId().equals(activeGroupId)) {
                            eventListener.onEventReceived(event);
                        }

                    }

                } catch (Exception e) {
                    Log.e("WS", "Invalid JSON: " + text);
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                listener.onError(t.getMessage());
            }
        });
    }

    public void setGroupEventListener(int groupId, GroupEventListener listener) {
        this.activeGroupId = groupId;
        this.eventListener = listener;
    }

    public void sendGroupMessage(SendMessageRequest request) {
        if (webSocket != null) {
            webSocket.send(request.toJson().toString());
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Closed");
        }
    }
}
