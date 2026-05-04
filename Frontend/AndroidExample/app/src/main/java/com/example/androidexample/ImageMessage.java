package com.example.androidexample;

import org.json.JSONException;
import org.json.JSONObject;

public class ImageMessage {

    private long id;
    private int senderId;
    private int groupId;
    private String imageUrl;
    private String caption;
    private String sentAt;

    public ImageMessage() {}

    public static ImageMessage fromJson(JSONObject json) throws JSONException {
        ImageMessage img = new ImageMessage();
        img.id = json.optLong("id", -1);
        img.senderId = json.optInt("senderId", -1);
        img.groupId = json.optInt("groupId", -1);
        img.imageUrl = json.optString("imageUrl", "");
        img.caption = json.optString("caption", "");
        img.sentAt = json.optString("sentAt", "");
        return img;
    }

    public long getId() { return id; }
    public int getSenderId() { return senderId; }
    public int getGroupId() { return groupId; }
    public String getImageUrl() { return imageUrl; }
    public String getCaption() { return caption; }
    public String getSentAt() { return sentAt; }
}
