package com.futurice.vor.card;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.futurice.vor.R;
import com.squareup.picasso.Picasso;

/**
 * Simple Topic that matches the EIT demo version topic.
 *
 * @author teemuk
 */
public final class Topic extends BaseTopic {

    @NonNull
    private String mText = "";
    @NonNull
    private Uri mImageUri = Uri.EMPTY;

    private int mColor;

    private String mCardType;

    public Topic(
            @NonNull final String name,
            final long topicUid,
            final Context context,
            final String type) {
        super(name, topicUid, context, R.layout.topic_layout);
        mCardType = type;
    }

    @Override
    public void updateView(@NonNull final View view, final boolean isExpanded) {
        ImageView topicIconImageView = (ImageView) view.findViewById(R.id.topic_icon);
        TextView topicTitleTextView = (TextView) view.findViewById(R.id.topic_title);

        topicIconImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        topicTitleTextView.setText(mText);

        Picasso.with(context)
                .load(mImageUri)
                .fit().centerCrop()
                .into(topicIconImageView);
    }

    @Override
    public boolean matchesSearch(@NonNull final String search) {
        return mText.toLowerCase().contains(search) || super.matchesSearch(search);
    }

    @Override
    public int compare(@NonNull final ITopic other) {
        return ((Topic) other).getLikes() - getLikes();
    }

    @NonNull
    public String getText() {
        return mText;
    }

    public void setText(@NonNull final String text) {
        mText = text;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(@NonNull final int color) {
        mColor = color;
    }

    @NonNull
    public Uri getImageUri() {
        return mImageUri;
    }

    public void setImageUri(@NonNull final Uri uri) {
        mImageUri = uri;
    }

    public String getCardType() {
        return mCardType;
    }

    public void setCardType(String cardType) {
        mCardType = cardType;
    }
}
