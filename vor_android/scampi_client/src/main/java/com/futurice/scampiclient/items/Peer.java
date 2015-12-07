package com.futurice.scampiclient.items;

import android.support.annotation.NonNull;
import android.util.Log;

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

    public final String tag;
    public final String idTag;
    public final String aboutMe;
    public final long[] cardLikeUniqueIds;
    public final long[] cardDeletionUniqueIds;
    public final long[] cardFlagUniqueIds;
    public final String[] comments;
    public long timestamp;

    public Peer(@NonNull final String tag, @NonNull final String idTag,
                @NonNull final String aboutMe,
                final long[] cardLikeUniqueIds,
                final long[] cardDeletionUniqueIds,
                final long[] cardFlagUniqueIds,
                final String[] comments,
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
        if (o == null || getClass() != o.getClass()) return false;

        Peer peer = (Peer) o;

        // if (!tag.equals(peer.tag)) return false;
        if (!idTag.equals(peer.idTag)) return false;
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
        return "Peer(aboutMe:" + aboutMe + " tag:" + tag + " idTag:" + idTag + " uid:" + uid + " likes:" + ArrayUtils.asString(cardLikeUniqueIds) + " comments:" + ArrayUtils.asString(comments);
    }
}
