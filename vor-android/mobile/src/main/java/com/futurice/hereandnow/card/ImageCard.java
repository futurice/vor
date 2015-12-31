package com.futurice.hereandnow.card;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.futurice.cascade.util.RCLog;
import static com.futurice.hereandnow.Constants.*;
import com.futurice.hereandnow.HereAndNowApplication;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.activity.ImageViewActivity;
import com.futurice.hereandnow.utils.FileUtils;
import com.futurice.hereandnow.utils.ImageUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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

    @NonNull
    private Uri mThumbnailUri = Uri.EMPTY;

    public ImageCard(final String name, final long uid, final Context context) {
        super(name, uid, context, R.layout.card_layout);
    }

    /**
     * Helper function for saving a local thumbnail of the image file.
     *
     * @param imageFile
     * @return
     */
    @NonNull
    public static Uri createThumbnail(@NonNull final Uri imageFile) {
        // For resource content we'll just use the main image as thumbnail
        if (imageFile.toString().startsWith("android.resource")) {
            return imageFile;
        }

        try {
            Context context = HereAndNowApplication.getStaticContext();
            final String filePath = FileUtils.getPath(context, imageFile);
            if (filePath == null) {
                throw new IllegalArgumentException("Can not find path to file: " + imageFile);
            }

            final File localImageFile = new File(filePath);
            int thumbnailSize = MediaStore.Video.Thumbnails.MICRO_KIND;
            final Bitmap bmThumbnail = ImageUtils.createImageThumbnail(filePath, thumbnailSize);

            if (bmThumbnail != null) {
                String path = Environment.getExternalStorageDirectory() + "/image_thumbnails/";
                final File directory = new File(path);
                if (!directory.exists() && !directory.mkdirs()) {
                    throw new IllegalStateException("Can not create external storage directory for thumbnails: " + imageFile);
                }

                final File file = new File(directory.getAbsolutePath() + "/" + localImageFile.getName() + ".jpg");
                final OutputStream outStream = new FileOutputStream(file);

                try {
                    bmThumbnail.compress(Bitmap.CompressFormat.JPEG, 90, outStream);
                    outStream.flush();
                } catch (NullPointerException e) {
                    RCLog.e(ImageCard.class.getSimpleName(), "Can not compress thumbnail", e);
                } finally {
                    outStream.close();
                }

                return Uri.fromFile(file);
            } else {
                RCLog.i(TAG, "Can not create bitmap: " + imageFile);
            }
        } catch (IOException e) {
            RCLog.e(BaseCard.class.getSimpleName(), "Problem writing thumbnail", e);
        }

        return Uri.EMPTY;
    }

    @Override
    public void updateView(@NonNull View view) {
        super.updateView(view);

        final TextView cardTextView = (TextView) view.findViewById(R.id.card_text);
        final TextView cardAuthorTextView = (TextView) view.findViewById(R.id.card_author);
        final ImageView cardImageView = (ImageView) view.findViewById(R.id.card_image);

        final String date = DateUtils.getRelativeDateTimeString(mContext,
                getDate().getTime(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS,
                0).toString();

        if (getImageUri() != Uri.EMPTY) {
            Picasso.with(mContext)
                    .load(getImageUri())
                    .resize(500, 500) // Downscale huge images first
                    .onlyScaleDown()
                    .centerInside() // To keep the aspect ratio on resize
                    .into(cardImageView);
            cardImageView.setOnClickListener(v -> {
                Intent viewImageIntent = new Intent(mContext, ImageViewActivity.class);
                viewImageIntent.putExtra(ImageViewActivity.IMAGE_URI, getImageUri().toString());
                viewImageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // FIXME Correct?
                mContext.startActivity(viewImageIntent);
            });
        } else if (mImageBase64 != null){
            cardImageView.setImageBitmap(getImageBitmap());
            cardImageView.setOnClickListener(v -> {
                Intent viewImageIntent = new Intent(mContext, ImageViewActivity.class);
                viewImageIntent.putExtra(TYPE_KEY, getCardType());
                viewImageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // FIXME Correct?
                mContext.startActivity(viewImageIntent);
            });
        }

        cardTextView.setText(getText());
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
        setThumbnailUri(createThumbnail(mImageUri));
    }

    @NonNull
    public Bitmap getImageBitmap() {
        return FileUtils.base64ToBitmap(mImageBase64);
    }

    public void setImageBase64(@NonNull final String base64) {
        mImageBase64 = base64;
    }

    @NonNull
    public Uri getThumbnailUri() {
        return mThumbnailUri;
    }

    public void setThumbnailUri(@NonNull final Uri thumbnailUri) {
        mThumbnailUri = thumbnailUri;
    }
}
