package com.futurice.hereandnow.card;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.futurice.cascade.util.AssertUtil;
import com.futurice.cascade.util.RCLog;
import com.futurice.hereandnow.HereAndNowApplication;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.utils.FileUtils;
import com.futurice.scampiclient.items.VideoCardVO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Card that contains a video and allows sending to a big screen for playing.
 *
 * @author teemuk
 */
public class VideoCard extends ImageCard {
    public static final int CARD_TYPE = 1;    // Needed for ListView recycling.
    private static final long MIN_SEND_INTERVAL_MILLIS = 5 * 1000;
    private static final String TAG = VideoCard.class.getSimpleName();

    @NonNull
    private Uri videoUri = Uri.EMPTY;
    @Nullable

    private VideoCardVO videoCardVO;
    private long lastSend;     // Command button spamming prevention

    public VideoCard(final String name, long uid, Context context) {
        super(name, uid, context);
    }

    /**
     * Helper function for saving a local thumbnail of the video file.
     *
     * @param videoFile
     * @return
     */
    @NonNull
    public static Uri createThumbnail(@NonNull final Uri videoFile) {
        try {
            final String filePath = AssertUtil.assertNotNull(FileUtils.getPath(HereAndNowApplication.getStaticContext(), videoFile));
            final File localVideoFile = new File(filePath);
            Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);

            final File directory = new File(Environment.getExternalStorageDirectory() + "/video_thumbnails/");
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    throw new RuntimeException("Can not create directory: " + directory);
                }
            }

            final File file = new File(directory.getAbsolutePath() + "/" + localVideoFile.getName() + ".jpg");
            final OutputStream outStream = new FileOutputStream(file);

            try {
                bmThumbnail.compress(Bitmap.CompressFormat.JPEG, 85, outStream);
                outStream.flush();
            } finally {
                outStream.close();
            }

            return Uri.fromFile(file);

        } catch (Exception e) {
            RCLog.e(VideoCard.class.getSimpleName(), "Problem writing thumbnail", e);
        }

        return Uri.EMPTY;
    }

    /**
     * Sets the video message that this card will display. The message should really
     * be passed in constructor, but for ease of testing purposes do it like this for now.
     *
     * @param videoCardVO video message to display
     */
    public final void setVideoCardVO(@NonNull final VideoCardVO videoCardVO) {
        Log.d(TAG, "setVideoCard: " + videoCardVO.author + ", " + videoCardVO.authorId);
        this.videoCardVO = videoCardVO;
        this.setVideoUri(Uri.fromFile(videoCardVO.videoFile));

        createVideoThumbnail(videoCardVO);
    }

    private void createVideoThumbnail(@NonNull final VideoCardVO videoCard) {
        RCLog.d(this, "Setting thumbnail for: " + videoCard.getCardName());

        Uri videoUri = Uri.fromFile(videoCard.videoFile);
        Uri thumbnailUri = createThumbnail(videoUri);

        if (thumbnailUri != Uri.EMPTY) {
            this.setImageUri(thumbnailUri);
        }

        RCLog.d(this, "Thumbnail set: " + thumbnailUri);
    }

    @Override
    public void updateView(@NonNull final View view) {
        super.updateView(view);

        RCLog.v(this, "updateView");
        view.findViewById(R.id.video_card_play_button_image).setVisibility(View.VISIBLE);
        //  view.findViewById(R.id.video_card_tobigscreen_layout).setVisibility(View.VISIBLE);

      /*  final ImageView bigScreenButton = (ImageView) view.findViewById(R.id.video_card_tobigscreen_button);
        bigScreenButton.setOnClickListener(v -> {
            if (System.currentTimeMillis() - this.lastSend > MIN_SEND_INTERVAL_MILLIS) {
                final VideoCardVO vo = assertNotNull(this.videoCardVO);
                ServiceSingleton.instance().bigScreenControllerService().sendMessageAsync(
                        new BigScreenControllerService.Command(
                                BigScreenControllerService.Command.PLAY_COMMAND,
                                vo.uid
                        )
                );
                this.lastSend = System.currentTimeMillis();
            }
        });
*/
        final View.OnClickListener onClickListener = v -> {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(this.getVideoUri(), "video/*");
            this.context.startActivity(intent);
        };

        view.findViewById(R.id.card_image).setOnClickListener(onClickListener);
        view.findViewById(R.id.video_card_play_button_image).setOnClickListener(onClickListener);
    }

    @Override // Card
    public int getType() {
        return CARD_TYPE;
    }

    @NonNull
    public Uri getVideoUri() {
        return videoUri;
    }

    public void setVideoUri(@NonNull final Uri videoUri) {
        this.videoUri = videoUri;
    }
}
