package com.futurice.scampiclient.items;

import android.support.annotation.NonNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Model object for a sub-card that contains a picture and an associated
 * textual title.
 *
 * @author teemuk
 */
public final class PictureCardVO extends ScampiCard {
    // TODO:
    // - Use numerical topic IDs? This means different topics could have same title.

    @NonNull

    private static final String datePattern = "yyyy-MM-dd HH:mm:ss.SSS";
    @NonNull

    private static final SimpleDateFormat timeformatter = new SimpleDateFormat(datePattern, Locale.getDefault());

    @NonNull

    public final String topic;    // Topic to which this card is attached.
    @NonNull

    public final File pictureFile;    // File that contains the picture.
    @NonNull

    public final String pictureType;    // Type of the picture (file extension).
    @NonNull

    public final String title;    // Title for the picture.
    public final long creationTime;    // Time when this card was created.
    @NonNull

    public final String author;    // Author of this card.
    @NonNull

    public final String authorId;    // Author of this card.

    /**
     * Creates a new picture card.
     *
     * @param topic        topic to which this card is attached
     * @param pictureFile  path to the file containing the picture
     * @param title        title for the picture
     * @param creationTime timestamp for the creation time
     * @param unique       random uid value, (creationTime,topic,uid)
     *                     tuple needs to be "probably" globally uid for
     *                     every card
     * @param author       author of the card
     */
    public PictureCardVO(
            @NonNull final String topic,
            @NonNull final File pictureFile,
            @NonNull final String pictureType,
            @NonNull final String title,
            final long creationTime,
            final long unique,
            @NonNull final String author,
            @NonNull final String authorId,
            @NonNull final String eventId) {
        super(eventId);
        this.topic = topic;
        this.pictureFile = pictureFile;
        this.pictureType = pictureType;
        this.title = title;
        this.creationTime = creationTime;
        this.uid = unique;
        this.author = author;
        this.authorId = authorId;
    }

    /**
     * Returns a aboutMe that should be used for a card that displays this message.
     *
     * @return card aboutMe to use for this message
     */
    @NonNull

    public final String getCardName() {
        return "picture-" + this.topic + "-" + this.author + "-" + this.creationTime + "-"
                + this.uid;
    }

    @NonNull

    public final String getDescription() {
        return this.author + " on " + timeformatter.format(new Date(this.creationTime));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PictureCardVO that = (PictureCardVO) o;

        return creationTime == that.creationTime
                && uid == that.uid
                && author.equals(that.author)
                && topic.equals(that.topic);

    }

    @Override
    public int hashCode() {
        int result = topic.hashCode();

        result = 31 * result + (int) (creationTime ^ (creationTime >>> 32));
        result = 31 * result + (int) (uid ^ (uid >>> 32));
        result = 31 * result + author.hashCode();

        return result;
    }
}
