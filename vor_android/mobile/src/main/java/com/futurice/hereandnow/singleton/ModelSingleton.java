package com.futurice.hereandnow.singleton;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.futurice.cascade.Async;
import com.futurice.cascade.i.CallOrigin;
import com.futurice.cascade.i.IAltFuture;
import com.futurice.cascade.reactive.PersistentValue;
import com.futurice.cascade.util.AssertUtil;
import com.futurice.cascade.util.Origin;
import com.futurice.cascade.util.RCLog;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.activity.SettingsActivity;
import com.futurice.hereandnow.card.Comment;
import com.futurice.hereandnow.card.ICard;
import com.futurice.hereandnow.card.ITopic;
import com.futurice.hereandnow.i.ScampiDataChangeListener;
import com.futurice.scampiclient.ScampiPeerDiscoveryService;
import com.futurice.scampiclient.items.Event;
import com.futurice.scampiclient.items.Peer;
import com.futurice.scampiclient.items.PictureCardVO;
import com.futurice.scampiclient.items.ScampiItem;
import com.futurice.scampiclient.items.VideoCardVO;
import com.futurice.scampiclient.utils.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.futurice.cascade.Async.UI;
import static com.futurice.cascade.Async.WORKER;

public class ModelSingleton extends Origin {

    private static ModelSingleton instance;
    // Persistent values
    @NonNull
    public final PersistentValue<String> myTag;
    @NonNull
    public final PersistentValue<String> myIdTag;
    @NonNull
    public final PersistentValue<String> myAboutMe;
    @NonNull
    public final PersistentValue<long[]> myLikes;
    @NonNull
    public final Map<Long, List<String>> likes; // All likes, key card uid and value list of users
    @NonNull
    public final PersistentValue<String[]> myComments;
    @NonNull
    public final Map<Long, List<String>> comments; // All comments, key card uid and value list of comments
    @NonNull
    public final PersistentValue<long[]> deletedCards; // *All* deleted cards, not just from current user
    @NonNull
    final public PersistentValue<long[]> flaggedCards; // *All* flagged cards, not just from current user
    public final String TAG = ModelSingleton.class.getName();
    @NonNull
    private final ArrayList<Peer> peerModel = new ArrayList<>();
    @NonNull
    private final ArrayList<PictureCardVO> pictureCardModel = new ArrayList<>();
    @NonNull
    private final ArrayList<VideoCardVO> videoCardModel = new ArrayList<>();
    @NonNull
    private final ArrayList<ScampiDataChangeListener<Peer>> peerChangeListeners = new ArrayList<>();
    @NonNull
    private final ArrayList<ScampiDataChangeListener<PictureCardVO>> pictureCardListeners = new ArrayList<>();
    @NonNull
    private final ArrayList<ScampiDataChangeListener<VideoCardVO>> videoCardListeners = new ArrayList<>();
    @NonNull
    private final PersistentValue<String> currentEventId;
    @NonNull
    private final PersistentValue<Long> currentEventTimestamp;
    private Context context;

    public ModelSingleton(@NonNull final Context context) {
        this.context = AssertUtil.assertNotNull(context, "Context can not be null");

        final String preexistingTag = PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsActivity.TAG_KEY, "anonymous");
        final String preexistingAboutMe = PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsActivity.ABOUT_KEY, "");
        final String preexistingIdTag = PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsActivity.ID_TAG_KEY, UUID.randomUUID().toString());
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(SettingsActivity.ID_TAG_KEY, preexistingIdTag);
        editor.apply();

        // Read persistent values from shared preferences, or use a default value
        myTag = PersistentValue.getPersistentValue(
                context.getString(R.string.persist_my_tag),
                preexistingTag,
                UI,
                s -> s,
                null,
                context);
        myIdTag = PersistentValue.getPersistentValue(
                context.getString(R.string.persist_my_id_tag),
                preexistingIdTag,
                UI,
                s -> s,
                null,
                context);
        Log.d("ModelSingleton", "my id tag = " + myIdTag);
        myAboutMe = PersistentValue.getPersistentValue(
                context.getString(R.string.persist_about_me),
                preexistingAboutMe,
                UI,
                s -> s,
                null,
                context);
        myLikes = PersistentValue.getPersistentValue(
                context.getString(R.string.persist_my_likes),
                new long[0],
                UI,
                l -> l,
                null,
                context);
        myLikes.set(new long[0]);

        likes = new HashMap<>();
        myComments = PersistentValue.getPersistentValue(
                context.getString(R.string.persist_my_comments),
                new String[0],
                UI,
                s -> s,
                null,
                context);
        myComments.set(new String[0]);

        comments = new HashMap<>();
        deletedCards = PersistentValue.getPersistentValue(
                context.getString(R.string.persist_deleted_cards),
                new long[0],
                UI,
                l -> l,
                null,
                context);
        flaggedCards = PersistentValue.getPersistentValue(
                context.getString(R.string.persist_flagged_cards),
                new long[0],
                UI,
                l -> l,
                null,
                context);
        currentEventId = PersistentValue.getPersistentValue(
                context.getString(R.string.persist_event_id),
                "none",
                UI,
                l -> l,
                null,
                context);
        currentEventTimestamp = PersistentValue.getPersistentValue(
                context.getString(R.string.persist_event_start),
                0L,
                UI,
                l -> l,
                null,
                context);

        /* TODO peers are currently expired by TrendingFragment, by removing
           them straight from list after a timeout. Would be nicer here instead.
        peerTimeoutTimer = new Timer();
        peerTimeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                purgeExpiredPeers();
            }
        }, 20000);
        */
    }

    //TODO The background service creation looks like a race condition. Return an async object which completes when all chains complete
    @CallOrigin
    public static synchronized ModelSingleton create(@NonNull final Context context) {
        if (instance != null) {
            return instance;
        }

        instance = new ModelSingleton(context);

        // Shift these to WORKER as they slow the main thread down during startup and are warned in STRICT MODE
        WORKER.then(() ->
                ServiceSingleton.instance().pictureCardService().addMessageReceivedListener((key, card) ->
                        instance.gotPictureMessage(card)));
        WORKER.then(() ->
                ServiceSingleton.instance().videoService().addMessageReceivedListener((key, card) ->
                        instance.gotVideoMessage(card)));
        WORKER.then(() ->
                ServiceSingleton.instance().peerDiscoveryService().addMessageReceivedListener((key, peer) ->
                        instance.gotPeerDiscovery(peer)));
        WORKER.then(() ->
                ServiceSingleton.instance().eventService().addMessageReceivedListener((key, event) ->
                        instance.gotEvent(event)));

        return instance;
    }

    @NonNull
    public static ModelSingleton instance() {
        return instance;
    }

    public void notifyAllPictureCards(@NonNull final ScampiDataChangeListener<PictureCardVO> listener) {
        for (PictureCardVO card : this.pictureCardModel) {
            listener.onItemAdded(card);
        }
    }

    public void notifyAllVideoCards(@NonNull final ScampiDataChangeListener<VideoCardVO> listener) {
        for (VideoCardVO card : this.videoCardModel) {
            listener.onItemAdded(card);
        }
    }

    public void notifyAllPeers(@NonNull final ScampiDataChangeListener<Peer> listener) {
        for (Peer peer : this.peerModel) {
            listener.onItemAdded(peer);
        }
    }

    public void addPeerListener(@NonNull final ScampiDataChangeListener<Peer> listener) {
        this.peerChangeListeners.add(listener);
    }

    public void removePeerListener(@NonNull final ScampiDataChangeListener<Peer> listener) {
        this.peerChangeListeners.remove(listener);
    }

    @NonNull
    public ArrayList<Peer> getPeerModel() {
        return peerModel;
    }

    public void addPictureCardListener(ScampiDataChangeListener<PictureCardVO> listener) {
        this.pictureCardListeners.add(listener);
    }

    public void removePictureCardListener(@NonNull final ScampiDataChangeListener<PictureCardVO> listener) {
        this.pictureCardListeners.remove(listener);
    }

    public void addVideoCardListener(@NonNull final ScampiDataChangeListener<VideoCardVO> listener) {
        this.videoCardListeners.add(listener);
    }

    public void removeVideoCardListener(@NonNull final ScampiDataChangeListener<VideoCardVO> listener) {
        this.videoCardListeners.remove(listener);
    }

    public int getLikes(@NonNull final ITopic topic) {
        int likes = 0;

        for (final ICard card : topic.getCards()) {
            likes += getLikes(card);
        }

        return likes;
    }

    public int getLikes(@NonNull final ICard card) {
        final List<Peer> likes = new ArrayList<>();

        for (final Peer peer : getPeers(true)) {
            for (final long id : peer.cardLikeUniqueIds.get()) {
                if (id == card.getUid()) {
                    likes.add(peer);
                    break;
                }
            }
        }

        for (final long id : myLikes.get()) {
            for (Peer peer : getPeers(true)) {
                // TODO: Needs testing
                if (id == peer.uid) {
                    likes.remove(peer);
                    break;
                }
            }
        }

        return likes.size();
    }

    public String getLikesList(@NonNull final ICard card) {
        final StringBuilder likesBuffer = new StringBuilder();

        for (final Peer peer : getPeers(true)) {
            for (final long id : peer.cardLikeUniqueIds.get()) {
                if (id == card.getUid()) {
                    likesBuffer.append(peer.tag);
                    likesBuffer.append(",");
                    break;
                }
            }
        }

        for (final long id : myLikes.get()) {
            if (id == card.getUid()) {
                likesBuffer.append(ModelSingleton.instance().myTag);
                likesBuffer.append(",");
                break;
            }
        }
        if (likesBuffer.length() > 0)
            return likesBuffer.substring(0, likesBuffer.length() - 1);
        else return "";
    }

    public List<Comment> getCommentsList(@NonNull final ICard card) {
        Log.d(TAG, "Getting the comment list");

        final List<Comment> comments;
        comments = new ArrayList<Comment>();

        for (final Peer peer : getPeers(false)) {
            for (final String commentJSON : peer.comments.get()) {
                final Comment comment = Comment.fromJSONString(commentJSON);
                if (comment != null && comment.getCardId() == card.getUid()) {
                    comments.add(comment);
                }
            }
        }
        for (final String commentString : myComments.get()) {
            final Comment comment = Comment.fromJSONString(commentString);
            if (comment != null && comment.getCardId() == card.getUid()) {
                comments.add(comment);
            }
        }
        Log.d(TAG, "Found " + comments.size() + " comments");

        if (!comments.isEmpty()) {
            Collections.sort(comments, (lhs, rhs) -> {
                if (lhs.getTimestamp() == rhs.getTimestamp())
                    return 0;
                else if (lhs.getTimestamp() < rhs.getTimestamp())
                    return -1;
                else return 1;
            });
        }
        Log.d(TAG, "Got the comment list: " + comments.size());

        return comments;
    }


    // Need to add ourselves if still missing
    @NonNull
    @SuppressWarnings("unchecked")
    private List<Peer> getPeers(final boolean removeSelf) {
        final ArrayList<Peer> peers = (ArrayList<Peer>) this.peerModel.clone();
        final Iterator<Peer> i = peers.iterator();

        if (removeSelf) {
            while (i.hasNext()) {
                final Peer peer = i.next();
                if (peer.tag.equals(myTag.get())) {
                    i.remove();
                    break;
                }
            }
        }

        return peers;
    }

    public void deleteCard(final long uid) {
        if (!ArrayUtils.valueExists(deletedCards.get(), uid)) {
            long[] deletes = ArrayUtils.prepend(deletedCards.get(), uid);
            deletedCards.set(deletes);

            final IAltFuture<?, ?> altFuture = Async.UI.then(() -> {
                // No need to notify video card listeners separately, the effect is the same
                for (ScampiDataChangeListener<PictureCardVO> listener : pictureCardListeners) {
                    listener.onItemsRemoved(new long[]{uid});
                }
            });
        }
    }

    public void flagCard(final long uid) {
        if (!ArrayUtils.valueExists(flaggedCards.get(), uid)) {
            long[] flags = ArrayUtils.prepend(flaggedCards.get(), uid);
            flaggedCards.set(flags);
        }
    }

    public boolean isFlagged(final long uid) {
        return ArrayUtils.valueExists(flaggedCards.get(), uid);
    }

    @NonNull
    public String getCurrentEventId() {
        String eventId = currentEventId.get();
        final Event latestEventReceived = ServiceSingleton.instance().eventService().getLatestStartEvent();
        if (latestEventReceived != null) {
            eventId = latestEventReceived.eventId;
            currentEventId.set(eventId);
        }

        return eventId;
    }

    private void purgeExpiredPeers() {
        List<Long> expiredPeers = new ArrayList<>();
        for (final Peer peer : this.peerModel) {
            if (((System.currentTimeMillis() - peer.timestamp) / 1000) > ScampiPeerDiscoveryService.MESSAGE_LIFETIME_SECONDS) {
                expiredPeers.add(peer.uid);
            }
        }

        // Updated peers act like new items, the old one is timed out soon
        for (ScampiDataChangeListener<Peer> listener : peerChangeListeners) {
            listener.onItemsRemoved(ArrayUtils.asArray(expiredPeers));
        }
    }

    private void gotPictureMessage(@NonNull final PictureCardVO card) {
        new Handler(context.getMainLooper())
                .post(new PictureDiscoveryHandler(this.pictureCardModel, this.pictureCardListeners, card));
    }

    private void gotVideoMessage(@NonNull final VideoCardVO card) {
        new Handler(context.getMainLooper())
                .post(new VideoDiscoveryHandler(this.videoCardModel, this.videoCardListeners, card));
    }

    private void gotPeerDiscovery(@NonNull final Peer peer) {
        new Handler(context.getMainLooper())
                .post(new PeerDiscoveryHandler(this.peerModel, this.peerChangeListeners, peer));
    }

    private void gotEvent(@NonNull final Event event) {
        // XXX: Don't think there's a need to use the ScampiDiscoveryHandler stuff for this.
        new Handler(context.getMainLooper())
                .post(() -> {
                    // Update the event ID if we received newer event info.
                    final long currentEventTime = currentEventTimestamp.get();
                    if (event.startTime > currentEventTime) {
                        currentEventId.set(event.eventId);
                        currentEventTimestamp.set(event.startTime);
                        RCLog.d(ModelSingleton.this, "Updated current event to: " + event.eventId);
                    }
                });
    }

    //// INCOMING MESSAGE HANDLERS

    private class ScampiDiscoveryHandler<T> implements Runnable {
        protected final List<ScampiDataChangeListener<T>> listeners;
        protected final List<T> model;
        protected final T item;
        protected final long uid;

        public ScampiDiscoveryHandler(
                @NonNull final List<T> model,
                @NonNull final List<ScampiDataChangeListener<T>> listeners,
                @NonNull final T item) {
            this.listeners = listeners;
            this.model = model;
            this.item = item;
            this.uid = ((ScampiItem) item).uid;
        }

        @Override
        public void run() {
            boolean exists = false;
            int index = 0;
            for (T existingItem : this.model) {
                if (existingItem.equals(this.item)) {
                    exists = true;
                    this.model.set(index, this.item);
                    break;
                }

                ++index;
            }

            if (ArrayUtils.valueExists(deletedCards.get(), uid)) {
                return; // This item is flagged as deleted, ignore
            }

            if (!exists) {
                this.model.add(this.item);

                for (ScampiDataChangeListener<T> listener : this.listeners) {
                    listener.onItemAdded(this.item);
                }
            }

            updateCrossReferences();
        }

        public void updateCrossReferences() {
            // Implementation in class specializations
        }
    }

    /**
     * Specializations for cards
     */
    private class PictureDiscoveryHandler extends ScampiDiscoveryHandler<PictureCardVO> {
        public PictureDiscoveryHandler(
                @NonNull final List<PictureCardVO> model,
                @NonNull final List<ScampiDataChangeListener<PictureCardVO>> scampiDataChangeListeners,
                @NonNull final PictureCardVO item) {
            super(model, scampiDataChangeListeners, item);
        }

        @Override
        public void run() {
            if (ArrayUtils.valueExists(flaggedCards.get(), uid)) {
                this.item.flagged = true;
            }

            super.run();
        }
    }

    private class VideoDiscoveryHandler extends ScampiDiscoveryHandler<VideoCardVO> {
        public VideoDiscoveryHandler(
                @NonNull final List<VideoCardVO> model,
                @NonNull final List<ScampiDataChangeListener<VideoCardVO>> scampiDataChangeListeners,
                @NonNull final VideoCardVO item) {
            super(model, scampiDataChangeListeners, item);
        }

        @Override
        public void run() {
            if (ArrayUtils.valueExists(flaggedCards.get(), uid)) {
                this.item.flagged = true;
            }

            super.run();
        }
    }

    /**
     * Specialization for Peers, since we want new likes and deletions to be reflected
     * immediately in the existing models.
     */
    private class PeerDiscoveryHandler extends ScampiDiscoveryHandler<Peer> {
        public PeerDiscoveryHandler(
                @NonNull final List<Peer> model,
                @NonNull final List<ScampiDataChangeListener<Peer>> scampiDataChangeListeners,
                @NonNull final Peer item) {
            super(model, scampiDataChangeListeners, item);
        }

        @Override
        public void updateCrossReferences() {
            final List<Long> deletes = deletedCards();
            final List<Long> updates = likedCards();

            updates.addAll(flaggedCards());
            updates.removeAll(deletes);

            // Note: in theory we should notify also video card listeners, since we can't know
            // the type of the deleted cards. Here we however know that all video card listeners
            // also listen for picture cards, so one notification is enough.
            if (deletes.size() > 0) {
                for (ScampiDataChangeListener<PictureCardVO> listener : pictureCardListeners) {
                    listener.onItemsRemoved(ArrayUtils.asArray(deletes));
                }
            }

            if (updates.size() > 0) {
                for (ScampiDataChangeListener<PictureCardVO> listener : pictureCardListeners) {
                    listener.onItemsUpdated(ArrayUtils.asArray(updates));
                }
            }

            // Updated peers act like new items, the old one is timed out soon
            for (ScampiDataChangeListener<Peer> listener : peerChangeListeners) {
                listener.onItemAdded(this.item);
            }
        }

        @NonNull
        private List<Long> likedCards() {
            return ArrayUtils.asList(this.item.cardLikeUniqueIds.get());
        }

        @NonNull
        private List<Long> flaggedCards() {
            List<Long> flags = ArrayUtils.asList(flaggedCards.get());
            List<Long> newFlags = new ArrayList<>();

            for (long uid : this.item.cardFlagUniqueIds.get()) {
                if (!flags.contains(uid)) {
                    newFlags.add(uid);
                }
            }

            flags.addAll(newFlags);
            flaggedCards.set(ArrayUtils.asArray(flags)); // Persist all flags
            return newFlags;
        }

        @NonNull
        private List<Long> deletedCards() {
            List<Long> deletes = ArrayUtils.asList(deletedCards.get());
            List<Long> newDeletes = new ArrayList<>();

            for (long uid : this.item.cardDeletionUniqueIds.get()) {
                if (!deletes.contains(uid)) {
                    newDeletes.add(uid);
                }
            }

            deletes.addAll(newDeletes);
            deletedCards.set(ArrayUtils.asArray(deletes)); // Persist all deletes
            return newDeletes;
        }
    }
}
