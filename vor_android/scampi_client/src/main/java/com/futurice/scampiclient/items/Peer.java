package com.futurice.scampiclient.items;

import android.support.annotation.NonNull;
import android.util.Log;

import com.futurice.cascade.i.IReactiveValue;
import com.futurice.scampiclient.HereAndNowService;
import com.futurice.scampiclient.utils.ArrayUtils;

/**
 * Peer discovered through Scampi.
 *
 * @author teemuk
 */
public final class Peer extends ScampiItem {
    // TODO:
    // - Need a way to distinguish users with same aboutMe.

    public final IReactiveValue<String> tag;
    public final IReactiveValue<String> idTag;
    public final IReactiveValue<String> aboutMe;
    public final IReactiveValue<long[]> cardLikeUniqueIds;
    public final IReactiveValue<long[]> cardDeletionUniqueIds;
    public final IReactiveValue<long[]> cardFlagUniqueIds;
    public final IReactiveValue<String[]> comments;
    public long timestamp;

    public Peer(@NonNull final IReactiveValue<String> tag,
                @NonNull final IReactiveValue<String> idTag,
                @NonNull final IReactiveValue<String> aboutMe,
                @NonNull final IReactiveValue<long[]> cardLikeUniqueIds,
                @NonNull final IReactiveValue<long[]> cardDeletionUniqueIds,
                @NonNull final IReactiveValue<long[]> cardFlagUniqueIds,
                @NonNull final IReactiveValue<String[]> comments,
                final long timestamp) {
        this.tag = tag;
        this.idTag = idTag;
        Log.d("Peer", "idTag=" + idTag);
        this.aboutMe = aboutMe;
        this.cardLikeUniqueIds = cardLikeUniqueIds;
        this.cardDeletionUniqueIds = cardDeletionUniqueIds;
        this.cardFlagUniqueIds = cardFlagUniqueIds;
        this.comments = comments;
        this.timestamp = timestamp;
        this.uid = HereAndNowService.generateUid();
    }

    //=================================================================//
    // Equality
    //=================================================================//
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Peer peer = (Peer) o;

        // if (!tag.equals(peer.tag)) return false;
        if (!idTag.equals(peer.idTag)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return aboutMe.hashCode();
    }
    //=================================================================//

    @NonNull
    @Override
    public String toString() {
        return "Peer(aboutMe:" +
                aboutMe.safeGet() +
                " tag:" + tag.safeGet() +
                " idTag:" + idTag.safeGet() +
                " uid:" + uid + " likes:" +
                ArrayUtils.asString(cardLikeUniqueIds.get()) +
                " comments:" + ArrayUtils.asString(comments.get());
    }
}
