package com.futurice.hereandnow.card;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.futurice.hereandnow.R;
import com.futurice.scampiclient.HereAndNowService;
import com.squareup.picasso.Picasso;

/**
 * Simple Topic that matches the EIT demo version topic.
 *
 * @author teemuk
 */
public final class Topic extends BaseTopic {

    @NonNull
    private String text = "";
    @NonNull
    private Uri imageUri = Uri.EMPTY;
    //   private int likes = 0;
    @NonNull
    private int mColor;

    public Topic(@NonNull final String name,
                 @NonNull final Context context) {
        this(name, HereAndNowService.generateUid(), context);
    }

    public Topic(@NonNull final String name,
                 final long topicUid,
                 final Context context) {
        super(name, topicUid, context, R.layout.topic_layout);
    }

    @Override
    public void addCard(@NonNull final ICard card) {
        super.addCard(card);

        // Set the topic icon to the first image added to the topic
        if (this.imageUri == Uri.EMPTY && card instanceof ImageCard) {
            setImageUri(((ImageCard) card).getThumbnailUri());
        }
    }

    @Override
    public void updateView(@NonNull final View view,
                           final boolean isExpanded) {
        super.setLikes(getLikes());
        super.updateView(view, isExpanded);

        ((ImageView) view.findViewById(R.id.topic_icon)).setScaleType(ImageView.ScaleType.CENTER_CROP);
        ((TextView) view.findViewById(R.id.topic_title)).setText(this.text);
        (view.findViewById(R.id.topic_linear_layout)).setBackgroundColor(mColor);

        //TODO Show number of likes on top of the image
        Picasso.with(context)
                .load(this.imageUri)
                .fit().centerCrop()
                .into((ImageView) view.findViewById(R.id.topic_icon));
      /*  if (getLikes() > 0) {
            ((LinearLayout) view.findViewById(R.id.topic_likes_layout)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.topic_like_text)).setText(Integer.toString(getLikes()));
        } else {
            ((LinearLayout) view.findViewById(R.id.topic_likes_layout)).setVisibility(View.GONE);
        }*/
    }

    @Override
    public boolean matchesSearch(@NonNull final String search) {
        return this.text.toLowerCase().contains(search) || super.matchesSearch(search);
    }

    @Override
    public int compare(@NonNull final ITopic other) {
        return ((Topic) other).getLikes() - getLikes();
    }

    @NonNull
    public String getText() {
        return text;
    }

    public void setText(@NonNull final String text) {
        this.text = text;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(@NonNull final int color) {
        mColor = color;
    }

    @NonNull
    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(@NonNull final Uri uri) {
        this.imageUri = uri;
    }

 /*   public int getLikes() {
        return likes;
    }

    public void setLikes(final int likes) {
        this.likes = likes;
        // TODO Refresh the image with number of likes
        // TODO Image would need to be reactive display
    }
    */
}
