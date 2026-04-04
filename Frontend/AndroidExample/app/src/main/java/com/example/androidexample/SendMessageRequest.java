package com.example.androidexample;

import org.json.JSONException;
import org.json.JSONObject;

public class SendMessageRequest {

    //private final int originatorId;
    private final int groupId;
    private final String messageType;
    private final Integer targetUserId;
    private final String textBody;
    //no originatorId
    public SendMessageRequest( int groupId, String messageType,
                              Integer targetUserId, String textBody) {
        //this.originatorId = originatorId;
        this.groupId = groupId;
        this.messageType = messageType;
        this.targetUserId = targetUserId;
        this.textBody = textBody;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            //json.put("originatorId", originatorId);
            json.put("groupId", groupId);
            json.put("messageType", messageType);

            if (targetUserId != null) {
                json.put("targetUserId", targetUserId);
            } else {
                json.put("targetUserId", JSONObject.NULL);
            }

            json.put("textBody", textBody);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }
}
