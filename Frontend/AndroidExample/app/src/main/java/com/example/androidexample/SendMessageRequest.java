
package com.example.androidexample;

import org.json.JSONException;
import org.json.JSONObject;


public class SendMessageRequest {

    private final int groupId;
    private final String messageType;
    private final Integer targetUserId;
    private final String textBody;

    public SendMessageRequest(int groupId, String messageType,
                              Integer targetUserId, String textBody) {
        this.groupId = groupId;
        this.messageType = messageType;
        this.targetUserId = targetUserId;
        this.textBody = textBody;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            // was missing eventType — backend switch requires it
            json.put("eventType", "SEND");

            //groupId must always be present — backend calls
            // requiredInt() which throws if missing
            json.put("groupId", groupId);

            // messageType must always be present
            json.put("messageType",
                    messageType != null ? messageType : "NORMAL");

            //targetUserId can be null — send as null not omit
            if (targetUserId != null) {
                json.put("targetUserId", targetUserId);
            } else {
                json.put("targetUserId", JSONObject.NULL);
            }

            // textBody must always be present — backend checks
            // if empty and returns error
            json.put("textBody",
                    textBody != null ? textBody : "");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
