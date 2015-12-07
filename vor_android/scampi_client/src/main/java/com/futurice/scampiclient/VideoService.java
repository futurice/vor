package com.futurice.scampiclient;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.util.Log;

import com.futurice.cascade.i.IAltFuture;
import com.futurice.cascade.util.RCLog;
import com.futurice.scampiclient.items.VideoCardVO;
import com.futurice.scampiclient.utils.UriUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import fi.tkk.netlab.dtn.scampi.applib.SCAMPIMessage;

/**
 * Service for publishing and receiving videos.
 *
 * @author teemuk
 */
public class VideoService extends HereAndNowService<VideoCardVO> {

    //======================================================================//
    // Constants
    //======================================================================//
    private static final String TAG = VideoService.class.getSimpleName();
    /**
     * Scampi service aboutMe to use for created messages.
     */
    private static final String SERVICE_NAME = "com.futurice.hereandnow.VideoService";
    private static final String TOPIC_FIELD_LABEL = "Topic";
    private static final String TOPIC_UID_FIELD_LABEL = "TopicUid";
    private static final String AUTHOR_FIELD_LABEL = "Author";
    private static final String AUTHOR_ID_FIELD_LABEL = "AuthorId";
    private static final String VIDEO_DATA_FIELD_LABEL = "VideoData";
    private static final String VIDEO_TYPE_FIELD_LABEL = "VideoType";
    private static final String MESSAGE_FIELD_LABEL = "Message";
    private static final String TIMESTAMP_FIELD_LABEL = "Timestamp";
    private static final String ID_FIELD_LABEL = "CardUid";
    private static final int MESSAGE_LIFETIME_MINUTES = 2 * 24 * 60;

    private final File storageDir;
    private final Context context;


    public VideoService(@NonNull final ScampiHandler scampiHandler,
                        @NonNull final File storageDir,
                        @NonNull final Context context) {
        super(SERVICE_NAME, MESSAGE_LIFETIME_MINUTES, TimeUnit.MINUTES, false, scampiHandler);
        this.storageDir = storageDir;
        this.context = context;

        // Make sure storageDir exists
        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                RCLog.d(this, "Failed to create storage directory.");
                throw new IllegalArgumentException("Storage directory doesn't exit and cannot be created.");
            }
        }
    }

    @NonNull
    public IAltFuture<?, SCAMPIMessage> sendMessageAsync(
            @NonNull final Uri video,
            @NonNull final String topic,
            final long topicUid,
            @NonNull final String author,
            @NonNull final String authorId,
            @NonNull final String eventId) {
        // Get file path
        final String videoPath = UriUtils.getPathForUri(this.context, video);
        if (videoPath == null) {
            throw new IllegalArgumentException("Cannot get path to Uri: " + video);
        }

        // Get file
        final File videoFile = new File(videoPath);
        if (!videoFile.isFile()) {
            throw new IllegalArgumentException("Path is not to an existing file '"
                    + videoPath + "'.");
        }

        // Create VideoMessage to sendEventMessage
        final String fileName = videoFile.getName();
        final String type = fileName.substring(fileName.lastIndexOf(".") + 1);
        final long timestamp = System.currentTimeMillis();
        final long uid = generateUid();
        final VideoCardVO videoCard =
                new VideoCardVO(topic, topicUid, videoFile, type, null, null, null,
                        timestamp, uid, author, authorId, eventId);

        // Send
        return this.sendMessageAsync(videoCard);
    }
    //======================================================================//


    //======================================================================//
    // Super class implementations
    //======================================================================//
    @NonNull
    @Override
    protected VideoCardVO getValueFieldFromIncomingMessage(@NonNull final SCAMPIMessage scampiMessage) throws Exception {
        this.checkMessagePreconditions(scampiMessage);

        // Get fields
        final String type = scampiMessage.getString(VIDEO_TYPE_FIELD_LABEL);
        final String topic = scampiMessage.getString(TOPIC_FIELD_LABEL);
        final long topicUid = scampiMessage.getInteger(TOPIC_UID_FIELD_LABEL);
        final long timestamp = scampiMessage.getInteger(TIMESTAMP_FIELD_LABEL);
        final long uid = scampiMessage.getInteger(ID_FIELD_LABEL);
        final String message = scampiMessage.hasString(MESSAGE_FIELD_LABEL)
                ? scampiMessage.getString(MESSAGE_FIELD_LABEL)
                : null;
        final String author = scampiMessage.hasString(AUTHOR_FIELD_LABEL)
                ? scampiMessage.getString(AUTHOR_FIELD_LABEL)
                : "anonymous";
        final String authorId = scampiMessage.hasString(AUTHOR_ID_FIELD_LABEL)
                ? scampiMessage.getString(AUTHOR_ID_FIELD_LABEL)
                : "anonymous";

        String eventId = scampiMessage.getMetadataString(METADATA_NAMESPACE, EVENT_METADATA_KEY);
        if (eventId == null) eventId = "none";

        // Construct filename
        final String filename = topic + "-" + topicUid + "-" + author + "-" + timestamp
                + "-" + uid + "." + type;
        final File video = new File(this.storageDir, filename);

        // Move binary to storage
        // Continue even if video exists (i.e., has been received previously).
        // This means that the receiver has to check for duplicates using the
        // VideoMessage.equals() method, but also means that videos don't need to be
        // copied/moved again when restarting the application or Scampi.
        if (!video.exists()) {
            scampiMessage.moveBinary(VIDEO_DATA_FIELD_LABEL, video);
        }

        // Construct new message
        Log.d(TAG, "Received a video card with author " + author + ", " + authorId);
        return new VideoCardVO(topic, topicUid, video, type, null, null, message, timestamp,
                uid, author, authorId, eventId);
    }

    @Override
    protected void addValueFieldToOutgoingMessage(
            @NonNull final SCAMPIMessage scampiMessage,
            @NonNull final VideoCardVO value) {
        scampiMessage.putBinary(VIDEO_DATA_FIELD_LABEL, value.videoFile, false);
        scampiMessage.putString(VIDEO_TYPE_FIELD_LABEL, value.videoType);
        scampiMessage.putString(TOPIC_FIELD_LABEL, value.topic);
        scampiMessage.putInteger(TOPIC_UID_FIELD_LABEL, value.topicUid);
        scampiMessage.putInteger(TIMESTAMP_FIELD_LABEL, value.creationTime);
        scampiMessage.putInteger(ID_FIELD_LABEL, value.uid);
        scampiMessage.putString(MESSAGE_FIELD_LABEL, value.title);

        scampiMessage.setMetadata(METADATA_NAMESPACE, EVENT_METADATA_KEY, value.eventId);

        if (value.author != null) {
            scampiMessage.putString(AUTHOR_FIELD_LABEL, value.author);
        }
        if (value.authorId != null) {
            scampiMessage.putString(AUTHOR_ID_FIELD_LABEL, value.authorId);
        }

        Log.d(TAG, "Sending a video card with author " + value.author + ", " + value.authorId);
    }

    @Override
    protected void notifyMessageExpired(@NonNull String key) {
        //TODO
    }

    @NonNull
    @Override
    @CheckResult(suggest = "IAltFuture#fork()")
    public IAltFuture<?, SCAMPIMessage> sendMessageAsync(@NonNull final VideoCardVO val) {
        final SCAMPIMessage.Builder builder = SCAMPIMessage.builder();
        builder.lifetime(MESSAGE_LIFETIME_MINUTES, TimeUnit.MINUTES);
        final SCAMPIMessage scampiMessage = builder.build();
        this.addValueFieldToOutgoingMessage(scampiMessage, val);

        return scampiHandler.sendMessageAsync(getName(), scampiMessage);
    }

    private void checkMessagePreconditions(@NonNull final SCAMPIMessage scampiMessage)
            throws IOException {
        if (!scampiMessage.hasString(TOPIC_FIELD_LABEL)) {
            throw new IOException("No topic in message.");
        }
        if (!scampiMessage.hasBinary(VIDEO_DATA_FIELD_LABEL)) {
            throw new IOException("No image data in message.");
        }
        if (!scampiMessage.hasString(VIDEO_TYPE_FIELD_LABEL)) {
            throw new IOException("No image type in message.");
        }
        if (!scampiMessage.hasInteger(TIMESTAMP_FIELD_LABEL)) {
            throw new IOException("No creation timestamp in message.");
        }
        if (!scampiMessage.hasInteger(ID_FIELD_LABEL)) {
            throw new IOException("No id in the message.");
        }
    }
}
