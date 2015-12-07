package com.futurice.scampiclient.items;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;

/**
 * Type sent and received by VideoService.
 * <p>
 * VO = "Value Object", a data structure
 *
 * @author teemuk
 */
public final class VideoCardVO extends ScampiCard {
    public final static String TAG = VideoCardVO.class.getName();
    @NonNull

    public final String topic;    // Topic to which this card is attached
    public final long topicUid;    // Unique id of the topic to which this card is attached
    @NonNull

    public final File videoFile;    // File that contains the video
    @NonNull

    public final String videoType;    // Type of the video (file extension)
    @NonNull

    public final File thumbnailFile;    // File that contains thumbnail for the video (can be null)
    @NonNull

    public final String thumbnailType;    // Type of the thumbnail image (file extension)
    @NonNull

    public final String title;    // Title of this card.
    public final long creationTime;    // Time when this card was created
    @NonNull

    public final String author;    // Author of this card.
    @NonNull

    public final String authorId;    // Author of this card.


    public VideoCardVO(@NonNull final String topic,
                       final long topicUid,
                       @NonNull final File videoFile,
                       @NonNull final String videoType,
                       @NonNull final File thumbnailFile,
                       @NonNull final String thumbnailType,
                       @NonNull final String title,
                       final long creationTime,
                       final long uid,
                       @NonNull final String author,
                       @NonNull final String authorId,
                       @NonNull final String eventId) {
        super(eventId);

        this.topic = topic;
        this.topicUid = topicUid;
        this.videoFile = videoFile;
        this.videoType = videoType;
        this.thumbnailFile = thumbnailFile;
        this.thumbnailType = thumbnailType;
        this.title = title;
        this.creationTime = creationTime;
        this.uid = uid;
        this.author = author;
        this.authorId = authorId;
        Log.d(TAG, "Created a video card with author " + author + ", " + authorId);
    }

    /**
     * Returns a aboutMe that should be used for a card that displays this message.
     *
     * @return card aboutMe to use for this message
     */
    @NonNull

    public final String getCardName() {
        return this.topic
                + "-" + this.topicUid
                + "-" + this.author
                + "-" + this.creationTime
                + "-" + this.uid;
    }

    @Override
    @NonNull

    public final String toString() {
        return "(topic:" + this.topic
                + ", topicUid:" + this.topicUid
                + ", path:" + this.videoFile.getAbsolutePath()
                + ", videoType:" + this.videoType
                + ", creationTime:" + creationTime
                + ", uid:" + uid
                + ", author:" + author + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final VideoCardVO that = (VideoCardVO) o;

        return creationTime == that.creationTime
                && topicUid == that.topicUid
                && uid == that.uid
                && author.equals(that.author)
                && authorId.equals(that.authorId)
                && topic.equals(that.topic);

    }

    @Override
    public int hashCode() {
        int result = topic.hashCode();
        result *= 31 + (int) (creationTime ^ (creationTime >>> 32));
        result *= 31 + (int) (uid ^ (uid >>> 32));
        result *= 31 + (int) (topicUid ^ (topicUid >>> 32));
        result *= 31 + author.hashCode();
        return result;
    }
}
