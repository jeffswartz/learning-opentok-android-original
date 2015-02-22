package com.tokbox.android.demo.learningopentok;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatMessage {

    public static final String KEY_TEXT = "text";

    private String mMessageText;
    private Boolean mRemote;

    public ChatMessage(String messageText) {
        mMessageText = messageText;
        mRemote = false;
    }

    public static ChatMessage fromData(String messageData) {
        JSONObject messageJson;
        String messageText;
        ChatMessage message;
        try {
            messageJson = new JSONObject(messageData);
            messageText = messageJson.getString(KEY_TEXT);
            message = new ChatMessage(messageText);
        } catch (JSONException e) {
            message = null;
        }
        return message;
    }

    public String getMessageText() {
        return mMessageText;
    }

    public Boolean getRemote() {
        return mRemote;
    }

    public void setRemote(Boolean remote) {
        mRemote = remote;
    }

    @Override
    public String toString() {
        JSONObject messageJson = new JSONObject();
        String messageData;
        try {
            messageJson.put(KEY_TEXT, mMessageText);
            messageData = messageJson.toString();
        } catch (JSONException e) {
            messageData = null;
        }
        return messageData;
    }
}
