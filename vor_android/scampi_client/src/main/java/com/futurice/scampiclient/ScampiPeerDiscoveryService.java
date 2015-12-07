package com.futurice.scampiclient;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.futurice.cascade.i.IAltFuture;
import com.futurice.cascade.util.RCLog;
import com.futurice.scampiclient.items.Peer;
import com.futurice.scampiclient.utils.ArrayUtils;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fi.tkk.netlab.dtn.scampi.applib.SCAMPIMessage;

/**
 * Peer discovery service implemented on Scampi.
 *
 * @author teemuk
 */
public final class ScampiPeerDiscoveryService extends HereAndNowService<Peer> {
    public static final int MESSAGE_LIFETIME_SECONDS = 25; // Lifetime for the generated ScampiMessages
    public static final int MESSAGE_BROADCAST_INTERVAL_SECONDS = 20;
    private static final String SERVICE_NAME = "com.futurice.hereandnow.profiles";
    private static final String TAG_FIELD_LABEL = "Tag"; // Key for the ScampiMessage content item that contains the user's chosen anonymous identity id
    private static final String ID_TAG_FIELD_LABEL = "IDTag"; // Key for the ScampiMessage content item that contains the a unique user ID
    private static final String ABOUT_ME_FIELD_LABEL = "AboutMe"; // Key for the ScampiMessage content item that contains the aboutMe
    private static final String TAG_FIELD_LIKES = "Likes"; // A newline-delimited set of uid Ids which the user has liked. The maximum length is FIFO limited
    private static final String TAG_FIELD_DELETIONS = "Deletions"; // A newline-delimited set of uid Ids which the user has deleted. The maximum length is FIFO limited
    private static final String TAG_FIELD_FLAGS = "Flags"; // A newline-delimited set of uid Ids which the user has flagged as inappropriate. The maximum length is FIFO limited
    private static final String TAG_FIELD_COMMENTS = "Comments"; // A newline-delimited set of user's comments. The maximum length is FIFO limited
    private static final String TIMESTAMP_FIELD_LABEL = "Timestamp"; // Key for the creation timestamp for the message
    private static final String APPTAG = "com.futurice.hereandnow.localprofile"; // App tag for local advertisement message
    private static final long[] EMPTY_LONG_ARRAY = new long[0];
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
    private volatile ScheduledFuture<?> scheduledAdvert;
    private volatile Peer localUser;

    /**
     * Creates a new discovery service that will receive peer updates and
     * advertise the local user.
     *
     * @param scampiHandler handler to the Scampi instance to use for networking
     * @param localUser     local user to advertise
     */
    public ScampiPeerDiscoveryService(@NonNull final ScampiHandler scampiHandler,
                                      @NonNull final Peer localUser) {
        super(SERVICE_NAME, MESSAGE_LIFETIME_SECONDS, TimeUnit.MINUTES,
                false, scampiHandler);

        this.localUser = localUser;
    }

    /**
     * Starts advertising the local user over Scampi.
     */
    public final void startAdvertisingLocalUser() {
        // Do nothing if we're already advertising
        if (this.scheduledAdvert != null) {
            RCLog.d(this, "startAdvertisingLocalUser() called multiple times.");
            return;
        }

        // Schedule and advert at the message lifetime rate. This way a new
        // advertisement is created every time the old one expires from the
        // network.
        this.scheduledAdvert = this.timer.scheduleAtFixedRate(
                () -> broadcastUserAdvertAsync().fork(),
                0,
                MESSAGE_BROADCAST_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
    }

    @CheckResult(suggest = "IAltFuture#fork()")
    private IAltFuture<?, SCAMPIMessage> broadcastUserAdvertAsync() {
        Log.d("Scampi", "broadcasting user advert");
        return scampiHandler.sendMessageAsync(SERVICE_NAME, getLocalAdvert())
                .then(() -> {
                    scampiHandler.sendMessageAsync(SERVICE_NAME, getLocalAdvert());
                });
    }

    /**
     * The list of card UIDs liked by this user (length is limited, FIFO expire when length is too long)
     *
     * @return
     */
    @NonNull
    public long[] getLocalUserLikes() {
        return localUser.cardLikeUniqueIds;
    }

    /**
     * The list of comments by this user (length is limited, FIFO expire when length is too long)
     *
     * @return
     */
    @NonNull
    public String[] getLocalUserComments() {
        return localUser.comments;
    }

    /**
     * The list of card UIDs deleted by all user (length is limited, FIFO expire when length is too long)
     *
     * @return
     */
    @NonNull
    public long[] getLocalUserDeletions() {
        return localUser.cardDeletionUniqueIds;
    }

    /**
     * The list of card UIDs flagged by all user (length is limited, FIFO expire when length is too long)
     *
     * @return
     */
    @NonNull
    public long[] getLocalUserFlags() {
        return localUser.cardDeletionUniqueIds;
    }

    /**
     * The user just pressed "like" on a card with this UID
     *
     * @param cardUniqueId
     * @return
     */
    @NonNull
    @CheckResult(suggest = "IAltFuture#fork()")
    public IAltFuture<?, SCAMPIMessage> localUserLikesACardAsync(final long cardUniqueId) {
        final Peer user = localUser;

        localUser = new Peer(user.tag, user.idTag, user.aboutMe,
                ArrayUtils.prepend(user.cardLikeUniqueIds, cardUniqueId),
                user.cardDeletionUniqueIds,
                user.cardFlagUniqueIds,
                user.comments,
                System.currentTimeMillis());

        return broadcastUserAdvertAsync();
    }

    /**
     * The user just pressed "unlike" on a card with this UID
     *
     * @param cardUniqueId
     * @return
     */
    @NonNull
    @CheckResult(suggest = "IAltFuture#fork()")
    public IAltFuture<?, SCAMPIMessage> localUserUnlikesACardAsync(final long cardUniqueId) {
        final Peer user = localUser;

        localUser = new Peer(user.tag,
                user.idTag,
                user.aboutMe,
                ArrayUtils.remove(user.cardLikeUniqueIds, cardUniqueId),
                user.cardDeletionUniqueIds,
                user.cardFlagUniqueIds,
                user.comments,
                System.currentTimeMillis());

        return broadcastUserAdvertAsync();
    }

    /**
     * The user commented on a card
     *
     * @param commentJSON
     * @return
     */
    @NonNull
    @CheckResult(suggest = "IAltFuture#fork()")
    public IAltFuture<?, SCAMPIMessage> localUserCommentsACardAsync(String commentJSON) {
        final Peer user = localUser;

        localUser = new Peer(user.tag,
                user.idTag,
                user.aboutMe,
                user.cardLikeUniqueIds,
                user.cardDeletionUniqueIds,
                user.cardFlagUniqueIds,
                ArrayUtils.prepend(user.comments, commentJSON),
                System.currentTimeMillis());

        return broadcastUserAdvertAsync();
    }

    /**
     * The user removes a comment
     *
     * @param commentJSON
     * @return
     */
    @NonNull
    @CheckResult(suggest = "IAltFuture#fork()")
    public IAltFuture<?, SCAMPIMessage> localUserRemovesCommentAsync(@NonNull final String commentJSON) {
        final Peer user = localUser;

        localUser = new Peer(user.tag,
                user.idTag,
                user.aboutMe,
                user.cardLikeUniqueIds,
                user.cardDeletionUniqueIds,
                user.cardFlagUniqueIds,
                ArrayUtils.remove(user.comments, commentJSON),
                System.currentTimeMillis());

        return broadcastUserAdvertAsync();
    }

    /**
     * The user just pressed "delete" on a card with this UID
     *
     * @param cardUniqueId
     * @return
     */
    @NonNull
    @CheckResult(suggest = "IAltFuture#fork()")
    public IAltFuture<?, SCAMPIMessage> localUserDeletesACardAsync(final long cardUniqueId) {
        final Peer user = localUser;

        localUser = new Peer(user.tag, user.idTag, user.aboutMe,
                user.cardLikeUniqueIds,
                ArrayUtils.prepend(user.cardDeletionUniqueIds, cardUniqueId),
                user.cardFlagUniqueIds,
                user.comments,
                System.currentTimeMillis());

        return broadcastUserAdvertAsync();
    }

    /**
     * The user just pressed "flag" on a card with this UID
     *
     * @param cardUniqueId
     * @return
     */
    @NonNull
    @CheckResult(suggest = "IAltFuture#fork()")
    public IAltFuture<?, SCAMPIMessage> localUserFlagsACardAsync(final long cardUniqueId) {
        final Peer user = localUser;

        localUser = new Peer(user.tag, user.idTag, user.aboutMe,
                user.cardLikeUniqueIds,
                user.cardDeletionUniqueIds,
                ArrayUtils.prepend(user.cardFlagUniqueIds, cardUniqueId),
                user.comments,
                System.currentTimeMillis());

        return broadcastUserAdvertAsync();
    }

    /**
     * Stops advertising the local user over Scampi.
     */
    public final void stopAdvertisingLocalUser() {
        if (this.scheduledAdvert != null) {
            this.scheduledAdvert.cancel(false);
            this.scheduledAdvert = null;
        }
    }

    /**
     * Stop the service, for example when the app is shutting down
     */
    @Override // AbstractScampiService
    public void stop() {
        stopAdvertisingLocalUser();
        super.stop();
    }

    /**
     * Refreshes the local advert message. Can be called after updating
     * the local user's data in order to push the update into the network
     * immediately
     */
    public final void refreshLocalAdvert() {
        this.stopAdvertisingLocalUser();
        this.startAdvertisingLocalUser();
    }

    /**
     * Updates the local user's data that is advertised to other users.
     * The updated data will be reflected in the next message refresh,
     * if you want to immediately sendEventMessage out the new data, call
     * {@link #refreshLocalAdvert()}.
     *
     * @param localUser local user to advertise
     */
    public final void updateLocalUser(@NonNull final Peer localUser) {
        this.localUser = localUser;
    }

    @NonNull
    @Override // HereAndNowService
    protected Peer getValueFieldFromIncomingMessage(@NonNull final SCAMPIMessage scampiMessage)
            throws IOException {
        // Precondition check
        if (!scampiMessage.hasString(ABOUT_ME_FIELD_LABEL)) {
            throw new IOException("No username in incoming peer advert.");
        }

        if (!scampiMessage.hasString(ID_TAG_FIELD_LABEL)) {
            throw new IOException("No id tag in incoming peer advert.");
        }

        final String name = scampiMessage.getString(ABOUT_ME_FIELD_LABEL);
        final String tag = this.getStringOrNull(scampiMessage, TAG_FIELD_LABEL);
        final String idTag = this.getStringOrNull(scampiMessage, ID_TAG_FIELD_LABEL);
        final String l = this.getStringOrNull(scampiMessage, TAG_FIELD_LIKES);
        final long[] likes = l != null ? ArrayUtils.parseArray(l) : EMPTY_LONG_ARRAY;
        final long timestamp = scampiMessage.getInteger(TIMESTAMP_FIELD_LABEL);
        final String d = this.getStringOrNull(scampiMessage, TAG_FIELD_DELETIONS);
        final long[] deletions = d != null ? ArrayUtils.parseArray(d) : EMPTY_LONG_ARRAY;
        final String f = this.getStringOrNull(scampiMessage, TAG_FIELD_FLAGS);
        final long[] flags = f != null ? ArrayUtils.parseArray(f) : EMPTY_LONG_ARRAY;
        final String c = this.getStringOrNull(scampiMessage, TAG_FIELD_COMMENTS);
        final String[] comments = c != null ? ArrayUtils.parseStringArray(c) : EMPTY_STRING_ARRAY;

        return new Peer(tag, idTag, name, likes, deletions, flags, comments, timestamp);
    }

    @Override // HereAndNowService
    protected void addValueFieldToOutgoingMessage(
            @NonNull final SCAMPIMessage scampiMessage,
            @NonNull final Peer value) {
        Log.d("ScampiPeerDiscovery", "outgoing tag=" + value.tag);
        scampiMessage.putString(ID_TAG_FIELD_LABEL, value.idTag);
        scampiMessage.putString(ABOUT_ME_FIELD_LABEL, value.aboutMe);
        scampiMessage.putInteger(TIMESTAMP_FIELD_LABEL, System.currentTimeMillis());
        scampiMessage.putString(TAG_FIELD_LIKES, ArrayUtils.musterArray(value.cardLikeUniqueIds));
        scampiMessage.putString(TAG_FIELD_DELETIONS, ArrayUtils.musterArray(value.cardDeletionUniqueIds));
        scampiMessage.putString(TAG_FIELD_FLAGS, ArrayUtils.musterArray(value.cardFlagUniqueIds));
        scampiMessage.putString(TAG_FIELD_COMMENTS, ArrayUtils.musterArray(value.comments));
        this.putStringIfNotNull(scampiMessage, TAG_FIELD_LABEL, value.tag);
    }

    @Override
    protected void notifyMessageExpired(@NonNull String key) {
        //TODO notifyMessageExpired action
    }

    @NonNull
    @CheckResult(suggest = "IAltFuture#fork()")
    @Override // HereAndNowService
    public IAltFuture<?, SCAMPIMessage> sendMessageAsync(@NonNull final Peer val) {
        throw new UnsupportedOperationException("Peer discovery service " +
                "cannot sendEventMessage messages.");
    }

    @Nullable
    private String getStringOrNull(
            @NonNull final SCAMPIMessage message,
            @NonNull final String field) {
        if (!message.hasString(field)) {
            return null;
        }

        return message.getString(field);
    }

    private void putStringIfNotNull(
            @NonNull final SCAMPIMessage message,
            @NonNull final String field,
            @Nullable final String value) {
        if (value == null) {
            return;
        }
        message.putString(field, value);
    }

    @NonNull
    private SCAMPIMessage getLocalAdvert() {
        final SCAMPIMessage.Builder builder = SCAMPIMessage.builder();

        builder.lifetime(MESSAGE_LIFETIME_SECONDS, TimeUnit.SECONDS);
        builder.appTag(APPTAG);
        final SCAMPIMessage scampiMessage = builder.build();
        this.addValueFieldToOutgoingMessage(scampiMessage, this.localUser);

        return scampiMessage;
    }
}
