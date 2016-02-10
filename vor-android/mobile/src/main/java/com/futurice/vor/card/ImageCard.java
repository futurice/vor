package com.futurice.vor.card;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import static com.futurice.vor.Constants.*;
import com.futurice.vor.R;
import com.futurice.vor.activity.ImageViewActivity;
import com.futurice.vor.utils.FileUtils;

/**
 * Card that matches the EIT demo card.
 *
 * @author teemuk
 */
public class ImageCard extends BaseCard {
    public static final int CARD_TYPE = 0;    // Needed for ListView recycling
    public static final String TAG = ImageCard.class.getName();
    @NonNull
    private String mText = "";

    @NonNull
    private String mCardType = "";

    @NonNull
    private Uri mImageUri = Uri.EMPTY;
    private String mImageBase64;

    public ImageCard(final String name, final long uid, final Context context) {
        super(name, uid, context, R.layout.card_layout);
    }

    @Override
    public void updateView(@NonNull View view) {
        super.updateView(view);

        final TextView cardAuthorTextView = (TextView) view.findViewById(R.id.card_author);
        final ImageView cardImageView = (ImageView) view.findViewById(R.id.card_image);

        final String date = DateUtils.getRelativeDateTimeString(mContext,
                getDate().getTime(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS,
                0).toString();

        cardImageView.setImageBitmap(getImageBitmap());
        cardImageView.setOnClickListener(v -> {
            Intent viewImageIntent = new Intent(mContext, ImageViewActivity.class);
            viewImageIntent.putExtra(TYPE_KEY, getCardType());
            viewImageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // FIXME Correct?
            mContext.startActivity(viewImageIntent);
        });

        cardAuthorTextView.setText(date);
    }

    @Override // BaseCard
    public int getType() {
        return CARD_TYPE;
    }

    @Override
    public boolean matchesSearch(@NonNull final String search) {
        return mText.toLowerCase().contains(search.toLowerCase());
    }

    @NonNull
    public String getText() {
        return mText;
    }

    public void setText(@NonNull final String text) {
        mText = text;
    }

    @NonNull
    public String getCardType() {
        return mCardType;
    }

    public void setCardType(@NonNull final String cardType) {
        mCardType = cardType;
    }

    @NonNull
    public Uri getImageUri() {
        return mImageUri;
    }

    public void setImageUri(@NonNull final Uri imageUri) {
        mImageUri = imageUri;
    }

    @NonNull
    public Bitmap getImageBitmap() {
        return FileUtils.base64ToBitmap(mImageBase64);
    }

    public void setImageBase64(@NonNull final String base64) {
        mImageBase64 = base64;
    }
}
