package com.futurice.hereandnow.activity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.widget.EditText;
import android.widget.Toast;

import com.futurice.cascade.i.IActionOne;
import com.futurice.hereandnow.Constants;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.singleton.ModelSingleton;
import com.futurice.hereandnow.singleton.ServiceSingleton;
import com.futurice.hereandnow.utils.FileUtils;
import com.futurice.hereandnow.utils.ImageUtils;
import com.futurice.scampiclient.HereAndNowService;
import com.futurice.scampiclient.items.PictureCardVO;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class NewTopicActivity extends NewCardActivity {

    private EditText topicTitleLabel;

    @Override
    protected void setContentView() {
        setContentView(R.layout.activity_new_topic);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    protected void initFormElements() {
        super.initFormElements();
        topicTitleLabel = (EditText) findViewById(R.id.new_topic_title_edittext);
    }

    @Override
    protected void initViewListeners() {
        super.initViewListeners();

        topicTitleLabel.addTextChangedListener(textWatcher);
        submitButton.setOnClickListener(
                v -> {
                    postImageMessage();
                    onBackPressed();
                    Toast.makeText(this, R.string.topic_create_success, Toast.LENGTH_SHORT).show();
                });

        discardButton.setOnClickListener(v -> onBackPressed());
    }

    private void postImageMessage() {
        if (mediaUri.getPath() == null || mediaUri.getPath().equals("")) {
            File emptyImageFile = new File(this.getCacheDir(), Constants.EMPTY_IMAGE_FILE_NAME);
            if (!emptyImageFile.exists()) {
                try {
                    emptyImageFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ImageUtils
                        .createEmptyImageAsync(emptyImageFile, getApplicationContext())
                        .then(() -> sendCardToService(emptyImageFile))
                        .fork();
            } else {
                sendCardToService(emptyImageFile);
            }

        } else {
            // Returns null and causes a crash if the file is not local
            File pictureFile = FileUtils.getFile(this, mediaUri);
            if (pictureFile == null) {
                Toast.makeText(this, R.string.error_non_local_file, Toast.LENGTH_LONG).show();
                return;
            }
            ImageUtils
                    .createResizedImageAsync(
                            pictureFile.getPath(),
                            pictureFile.getName(),
                            ImageUtils.TARGET_SIZE_MEDIUM)
                    .then((IActionOne<Uri>) Uri -> sendCardToService(pictureFile))
                    .fork();
        }
    }

    private void sendCardToService(@NonNull final File pictureFile) {
        final String topic = topicTitleLabel.getText().toString();
        final String title = cardTextLabel.getText().toString();
        final String eventId = ModelSingleton.instance().getCurrentEventId();
        final String author = ModelSingleton.instance().myTag.get();
        final String authorId = ModelSingleton.instance().myIdTag.get();
        final String pictureType = "jpg";
        final long creationTime = new Date().getTime();
        final long cardUid = HereAndNowService.generateUid();

        ServiceSingleton.instance().pictureCardService().sendMessageAsync(new PictureCardVO(
                topic,
                pictureFile,
                pictureType,
                title,
                creationTime,
                cardUid,
                author,
                authorId,
                eventId))
                .fork();
    }

    @Override
    protected void updateSubmitEnabled() {
        String topicText = topicTitleLabel.getText().toString();
        String cardText = cardTextLabel.getText().toString();

        if (!topicText.isEmpty() && !cardText.isEmpty()) {
            submitButton.setEnabled(true);
            submitButton.getBackground().setColorFilter(null);
        } else {
            submitButton.setEnabled(false);
            submitButton.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        }
    }

}
