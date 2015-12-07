package com.futurice.hereandnow.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.futurice.hereandnow.Constants;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.card.VideoCard;
import com.futurice.hereandnow.singleton.ModelSingleton;
import com.futurice.hereandnow.singleton.ServiceSingleton;
import com.futurice.hereandnow.utils.FileUtils;
import com.futurice.hereandnow.utils.ImageUtils;
import com.futurice.scampiclient.HereAndNowService;
import com.futurice.scampiclient.items.PictureCardVO;
import com.futurice.scampiclient.items.VideoCardVO;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewCardActivity extends BaseActivity {

    public static final String EXTRA_TOPIC = "TOPIC";
    public static final String EXTRA_CONTENT_TYPE = "TYPE";
    public static final String EXTRA_TOPIC_UID = "UID";
    public static final String TAG = NewCardActivity.class.getSimpleName();
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    protected static final int PICK_IMAGE_RESULT = 2000;
    protected static final int NEW_IMAGE_RESULT = 2001;
    protected EditText cardTextLabel;
    protected ImageView mImageView;
    protected ImageView mPickImageView;
    protected ImageView cameraButton;
    protected FrameLayout discardButton;
    protected FrameLayout submitButton;
    @NonNull

    protected String topic = "no topic";
    @NonNull

    protected Uri mediaUri = Uri.EMPTY;
    @NonNull

    protected Uri thumbnailUri = Uri.EMPTY;
    @Nullable

    protected String contentType = "";
    @NonNull

    protected TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            updateSubmitEnabled();
        }
    };
    @Nullable

    Long topicUid = null;
    private Uri fileUri;

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "HereAndNow");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("HereAndNow", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String t = (String) getExtra(EXTRA_TOPIC);
        if (t != null) {
            topic = t;
        }
        topicUid = (Long) getExtra(EXTRA_TOPIC_UID);
        contentType = (String) getExtra(EXTRA_CONTENT_TYPE);
        if (contentType == null) {
            contentType = "";
        }

        if (contentType.startsWith("video") && mediaUri != Uri.EMPTY) {
            thumbnailUri = VideoCard.createThumbnail(mediaUri);
        } else if (contentType.startsWith("image")) {
            thumbnailUri = mediaUri;
        }

        setContentView();
        initFormElements();
        initViewListeners();
        updateImageView();
        updateSubmitEnabled();
    }

    protected void setContentView() {
        setContentView(R.layout.activity_new_card);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        if (contentType != null && contentType.startsWith("video")) {
            TextView mediaOptionalTextView = (TextView) findViewById(R.id.media_optional);
            ImageView pickImageView = (ImageView) findViewById(R.id.new_card_media_pick_image_view);
            ImageView takePictureImageView = (ImageView) findViewById(R.id.new_card_take_picture);
            Drawable cardNavbarVideoDarkDrawable = ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.card_navbar_video_dark);

            mediaOptionalTextView.setText(R.string.optional_video_text);
            pickImageView.setImageResource(R.drawable.ic_videocam_24dp);
            takePictureImageView.setImageDrawable(cardNavbarVideoDarkDrawable);

        }
    }

    protected void initFormElements() {
        cardTextLabel = (EditText) findViewById(R.id.new_card_text_edittext);
        mImageView = (ImageView) findViewById(R.id.new_card_imageview);
        mPickImageView = (ImageView) findViewById(R.id.new_card_media_pick_image_view);
        cameraButton = (ImageView) findViewById(R.id.new_card_take_picture);

        // Inflate a "Done/Cancel" custom action bar view.
//        final Context context = actionBar != null ? actionBar.getThemedContext() : this;
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View actionBarView = inflater.inflate(
                R.layout.actionbar_custom_view_done_cancel,
                null);
        final ActionBar actionBar = getSupportActionBar();

        // Show the custom action bar view and hide the normal Home icon and title.
        if (actionBar != null) {
            actionBar.setDisplayOptions(
                    ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM
                            | ActionBar.DISPLAY_SHOW_HOME
                            | ActionBar.DISPLAY_SHOW_TITLE);

            actionBar.setCustomView(actionBarView,
                    new ActionBar.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            //FIXME Support devices with no action bar
            throw new UnsupportedOperationException("Not yet supporting devices with no action bar");
        }

        discardButton = (FrameLayout) actionBarView.findViewById(R.id.actionbar_cancel);
        submitButton = (FrameLayout) actionBarView.findViewById(R.id.actionbar_done);
    }

    protected void initViewListeners() {
        cardTextLabel.addTextChangedListener(textWatcher);
        mImageView.setOnClickListener(
                v -> {
                    Intent intent = new Intent();
                    intent.setType(contentType.isEmpty() ? "image/*" : contentType);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.gen_select_picture)), PICK_IMAGE_RESULT);
                });
        cameraButton.setOnClickListener(
                v -> {
                    Intent intent = new Intent();
                    if (contentType.startsWith("video")) {
                        intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
                        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                    } else {
                        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    }
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    startActivityForResult(intent, NEW_IMAGE_RESULT);
                });

        submitButton.setOnClickListener(
                v -> {
                    if (contentType.startsWith("video")) {
                        postVideoMessage();
                    } else if (contentType.startsWith("image")) {
                        postImageMessage();
                    }
                    setResult(Activity.RESULT_OK);
                    finish();
                });

        discardButton.setOnClickListener(
                v -> {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                });
    }

    private void postImageMessage() {
        if (mediaUri.getPath() == null || mediaUri.getPath().equals("")) {
            createEmptyImageCard();
        } else {
            File pictureFile = FileUtils.getFile(this, mediaUri);
            ImageUtils.createResizedImageAsync(pictureFile.getPath(), pictureFile.getName(), ImageUtils.TARGET_SIZE_MEDIUM)
                    .then((Uri) -> {
                        sendCardToService(pictureFile);
                    })
                    .fork();
        }
    }

    public void createEmptyImageCard() {
        final File emptyImageFile = new File(this.getCacheDir(), Constants.EMPTY_IMAGE_FILE_NAME);
        if (!emptyImageFile.exists()) {
            try {
                emptyImageFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ImageUtils
                    .createEmptyImageAsync(emptyImageFile, getApplicationContext()).then(() -> sendCardToService(emptyImageFile))
                    .fork();
        } else {
            sendCardToService(emptyImageFile);
        }
    }

    private void sendCardToService(@NonNull final File pictureFile) {
        String title = cardTextLabel.getText().toString();
        long creationTime = new Date().getTime();
        long cardUid = HereAndNowService.generateUid();
        final String eventId = ModelSingleton.instance().getCurrentEventId();
        String author = ModelSingleton.instance().myTag.get();
        String authorId = ModelSingleton.instance().myIdTag.get();

        ServiceSingleton.instance().pictureCardService().sendMessageAsync(new PictureCardVO(
                topic,
                pictureFile,
                "jpg",
                title,
                creationTime,
                cardUid,
                author,
                authorId,
                eventId
        ))
                .fork();

    }

    private void postVideoMessage() {
        if (mediaUri.getPath() == null || mediaUri.getPath().equals("")) {
            createEmptyImageCard();
        } else {
            File videoFile = FileUtils.getFile(this, mediaUri);

            ImageUtils.createResizedVideoImageAsync(videoFile.getPath(), videoFile.getName(), ImageUtils.TARGET_SIZE_MEDIUM)
                    .then(Uri -> {
                        File thumbnailFile = FileUtils.getFile(getApplicationContext(), thumbnailUri);
                        String title = cardTextLabel.getText().toString();
                        long creationTime = new Date().getTime();
                        long uid = HereAndNowService.generateUid();
                        String eventId = ModelSingleton.instance().getCurrentEventId();
                        String author = ModelSingleton.instance().myTag.get();
                        String authorId = ModelSingleton.instance().myIdTag.get();

                        ServiceSingleton.instance().videoService().sendMessageAsync(new VideoCardVO(
                                topic,
                                topicUid,
                                videoFile,
                                "video",
                                thumbnailFile,
                                "jpg",
                                title,
                                creationTime,
                                uid,
                                author,
                                authorId,
                                eventId
                        ));
                    })
                    .fork();
        }
    }

    protected void updateImageView() {
        if (thumbnailUri != Uri.EMPTY) {
            Picasso.with(getApplicationContext())
                    .load(thumbnailUri)
                    .noPlaceholder()
                    .resize(500, 500) // Downscale huge images first
                    .onlyScaleDown()
                    .centerInside() // To keep the aspect ratio on resize
                    .into(mImageView);
            mPickImageView.setVisibility(View.INVISIBLE);
        } else {
            mPickImageView.setVisibility(View.VISIBLE);
        }
    }

    protected void updateSubmitEnabled() {
        String cardText = cardTextLabel.getText().toString();

        if (!cardText.isEmpty()) {
            submitButton.setEnabled(true);
            submitButton.getBackground().setColorFilter(null);
        } else {
            submitButton.setEnabled(false);
            submitButton.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, @NonNull final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_RESULT) {
            mediaUri = resultCode == Activity.RESULT_OK ? data.getData() : Uri.EMPTY;
            if (contentType.startsWith("video") && mediaUri != Uri.EMPTY) {
                thumbnailUri = VideoCard.createThumbnail(mediaUri);
            } else {
                thumbnailUri = mediaUri;  // TODO reactive value updating automatically?
            }

            updateImageView();
            updateSubmitEnabled();
        } else if (requestCode == NEW_IMAGE_RESULT) {
            Log.d(TAG, "onActivityResult: " + requestCode + ", " + resultCode + ", " + data);
            if (resultCode == RESULT_OK) {
                mediaUri = fileUri;
                if (contentType.startsWith("video") && mediaUri != Uri.EMPTY) {
                    thumbnailUri = VideoCard.createThumbnail(mediaUri);
                } else {
                    thumbnailUri = mediaUri;  // TODO reactive value updating automatically?
                }
                Log.d(TAG, "mediaUri=" + mediaUri);
                updateImageView();
                updateSubmitEnabled();
            }
        }
    }

}
