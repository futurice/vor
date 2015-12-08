package com.futurice.hereandnow.card;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.futurice.cascade.util.Origin;
import com.futurice.cascade.util.RCLog;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by vizr on 02/10/15.
 */
public class Comment extends Origin implements Serializable {

    public final long cardId;
    public final String userTag;
    public final String userIdTag;
    private volatile String text;
    private volatile long timestamp;

    public Comment(final long cardId,
                   @NonNull final String userTag,
                   @NonNull final String userIdTag,
                   @NonNull final String text) {
        this.cardId = cardId;
        this.userTag = userTag;
        this.userIdTag = userIdTag;
        this.text = text;
        this.timestamp = new Date().getTime();
    }

    public String getText() {
        return this.text;
    }

    public void setText(@NonNull final String text) {
        this.timestamp = new Date().getTime();
        this.text = text;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String toJSONString() {
        return (new Gson()).toJson(this);
    }

    @Nullable
    public static Comment fromJSONString(@NonNull final String jsonString) {
        final Gson gson = new Gson();
        final Comment comment;

        try {
            comment = gson.fromJson(jsonString, Comment.class);
            RCLog.v(comment, "Parse comment: " + jsonString);
        } catch (JsonSyntaxException e) {
            RCLog.d(Comment.class.getSimpleName(), "Could not parse the comment: " + jsonString);

            return null;
        }

        return comment;
    }
}
