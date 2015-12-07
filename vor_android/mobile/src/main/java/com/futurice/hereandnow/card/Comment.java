package com.futurice.hereandnow.card;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by vizr on 02/10/15.
 */
public class Comment implements Serializable {

    private long cardId;
    private String userTag;
    private String userIdTag;
    private String text;
    private long timestamp;

    private static final String TAG = Comment.class.getName();

    public Comment(long cardId, String userTag, String userIdTag, String text) {
        if (userTag == null)
            userTag = "";
        if (userIdTag == null)
            userIdTag = "";
        if (text == null)
            text = "";
        this.cardId = cardId;
        this.userTag = userTag;
        this.userIdTag = userIdTag;
        this.text = text;
        this.timestamp = new Date().getTime();
    }


    public long getCardId() {
        return cardId;
    }

    public void setCardId(long cardId) {
        this.cardId = cardId;
    }

    public String getUserTag() {
        return userTag;
    }

    public void setUserTag(String userTag) {
        this.userTag = userTag;
    }

    public String getUserIdTag() {
        return userIdTag;
    }

    public void setUserIdTag(String userIdTag) {
        this.userIdTag = userIdTag;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String toJSONString() {
        return (new Gson()).toJson(this);
    }

    public static Comment fromJSONString(String jsonString) {
        Gson gson = new Gson();
        Comment comment;
        try {
            comment = gson.fromJson(jsonString, Comment.class);
        } catch (JsonSyntaxException e) {
            Log.d(TAG, "Could not parse the comment: " + jsonString);
            return null;
        }
        return comment;
    }
}
