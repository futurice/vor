package com.futurice.hereandnow.card;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.futurice.hereandnow.R;

/**
 * Card to be shown for peer profiles in the tribe view.
 *
 * @author teemuk
 */
public class PeerProfileCard extends BaseCard {

    public static final int CARD_TYPE = 2;    // Needed for ListView recycling.
    private static final long TOPIC_UID_FOR_ALL_PEER_CARDS = -9001;

    @NonNull
    private String peerTag = "";
    @NonNull
    private String peerIdTag = "";
    @NonNull
    private String peerAboutMe = "";
    @NonNull
    private String peerLikes = "";

    public PeerProfileCard(@NonNull final String name, @NonNull final Context context) {
        super(name, TOPIC_UID_FOR_ALL_PEER_CARDS, context, R.layout.peer_card_layout);
    }

    @Override
    public void updateView(@NonNull final View view) {
        super.updateView(view);

        ((TextView) view.findViewById(R.id.peer_tag_text)).setText(peerTag);
        ((TextView) view.findViewById(R.id.peer_about_me_text)).setText(peerAboutMe);
    }

    @Override // BaseCard
    public final int getType() {
        return CARD_TYPE;
    }

    @Override
    public boolean matchesSearch(@NonNull final String search) {
        return false;
    }

    @NonNull
    public String getPeerIdTag() {
        return peerIdTag;
    }

    public void setPeerIdTag(@NonNull final String peerIdTag) {
        this.peerIdTag = peerIdTag;
    }

    @NonNull
    public String getPeerTag() {
        return peerTag;
    }

    public void setPeerTag(@NonNull final String peerTag) {
        this.peerTag = peerTag;
    }

    @NonNull
    public String getPeerAboutMe() {
        return peerAboutMe;
    }

    public void setPeerAboutMe(@NonNull final String peerAboutMe) {
        this.peerAboutMe = peerAboutMe;
    }

    @NonNull
    public String getPeerLikes() {
        return peerLikes;
    }

    public void setPeerLikes(@NonNull final String peerLikes) {
        this.peerLikes = peerLikes;
    }
}
