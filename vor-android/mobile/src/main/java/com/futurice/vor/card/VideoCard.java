package com.futurice.vor.card;

import android.content.Context;
import android.support.annotation.NonNull;

import com.futurice.vor.R;

public class VideoCard extends BaseCard {
    public static final int CARD_TYPE = 0;    // Needed for ListView recycling

    @NonNull
    private String mText = "";

    private String mUrl = "";

    @NonNull
    private String mCardType = "";

    public VideoCard(final String name, final long uid, final Context context) {
        super(name, uid, context, 0);
    }

    @Override // BaseCard
    public int getType() {
        return CARD_TYPE;
    }

    @Override
    public boolean matchesSearch(@NonNull String search) {
        return false;
    }

    @NonNull
    public String getText() {
        return mText;
    }

    public void setText(@NonNull final String text) {
        mText = text;
    }

    public void setUrl(@NonNull final String url) {
        mUrl = url;
    }

    public void setCardType(@NonNull final String cardType) {
        mCardType = cardType;
    }
}
