package com.futurice.scampiclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;

import com.futurice.cascade.i.IAltFuture;
import com.futurice.cascade.util.RCLog;
import com.futurice.scampiclient.items.PictureCardVO;
import com.futurice.scampiclient.utils.UriUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import fi.tkk.netlab.dtn.scampi.applib.SCAMPIMessage;

/**
 * Service for publishing and receiving picture sub-cards.
 *
 * @author teemuk
 */
public final class PictureCardService extends HereAndNowService<PictureCardVO> {
    // TODO:
    // - Increase lifetime for production version.
    // - Title is ignored for now, so that a card can be generated from only an image.
    // - Stored images need to be cleaned up after lifetime expires.

    //======================================================================//
    // Constants
    //======================================================================//
    /**
     * Scampi service aboutMe to use for created messages.
     */
    private static final String SERVICE_NAME = "com.futurice.hereandnow.PictureCard";
    /**
     * Key for the ScampiMessage content item that contains the image.
     */
    private static final String IMAGE_DATA_FIELD_LABEL = "ImageData";
    /**
     * Key for the image type (file aboutMe extension).
     */
    private static final String IMAGE_TYPE_LABEL = "ImageType";
    /**
     * Key for the topic aboutMe for the card.
     */
    private static final String TOPIC_FIELD_LABEL = "FileNameField";
    /**
     * Key for the creation timestamp for the message.
     */
    private static final String TIMESTAMP_FIELD_LABEL = "Timestamp";
    /**
     * Key for a uid ID for the message (topic + timestamp + id = globally uid).
     */
    private static final String ID_FIELD_LABEL = "Id";
    /**
     * Key for the message field.
     */
    private static final String MESSAGE_FIELD_LABEL = "Message";
    /**
     * Key for the author aboutMe field.
     */
    private static final String AUTHOR_FIELD_LABEL = "Author";
    /**
     * Key for the author aboutMe field.
     */
    private static final String AUTHOR_ID_FIELD_LABEL = "AuthorId";
    /**
     * Lifetime for the generated ScampiMessages.
     */
    private static final int MESSAGE_LIFETIME_MINUTES = 2 * 24 * 60;
    private static final int SCALE_TO_WIDTH_TARGET = 640;
    private static final int SCALE_TO_HEIGHT_TARGET = 480;
    //======================================================================//

    @NonNull
    private final File imageStorageDir;
    @NonNull
    private final Context context;
    private final Random rng = new Random();


    /**
     * Create a new picture card service
     *
     * @param scampiHandler handler to Scampi, used to sendEventMessage and receive messages
     * @param storageDir    directory where incoming images are stored
     */
    @RequiresPermission(allOf = {"READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE"})
    public PictureCardService(
            @NonNull final ScampiHandler scampiHandler,
            @NonNull final File storageDir,
            @NonNull final Context context) {
        super(SERVICE_NAME, MESSAGE_LIFETIME_MINUTES, TimeUnit.MINUTES,
                false, scampiHandler);
        this.imageStorageDir = storageDir;
        this.context = context;

        // Make sure storageDir exists
        if (!storageDir.exists()) {
            final boolean createdSomething = storageDir.mkdirs();
        }
        if (!storageDir.isDirectory()) {
//TODO FIXME                RCLog.e(this, "Failed to create picture storage directory", new IllegalArgumentException("Storage directory doesn't exist and cannot be created"));
        }
    }
    //======================================================================//


    //======================================================================//
    // HereAndNowService
    //======================================================================//
    @NonNull
    @Override
    protected PictureCardVO getValueFieldFromIncomingMessage(@NonNull final SCAMPIMessage scampiMessage)
            throws Exception {
        // Precondition check
        this.checkMessagePreconditions(scampiMessage);

        // Get fields
        final String type = scampiMessage.getString(IMAGE_TYPE_LABEL);
        final String topic = scampiMessage.getString(TOPIC_FIELD_LABEL);
        final long timestamp = scampiMessage.getInteger(TIMESTAMP_FIELD_LABEL);
        final long unique = scampiMessage.getInteger(ID_FIELD_LABEL);
        final String message = scampiMessage.hasString(MESSAGE_FIELD_LABEL)
                ? scampiMessage.getString(MESSAGE_FIELD_LABEL)
                : null;
        final String author = scampiMessage.hasString(AUTHOR_FIELD_LABEL)
                ? scampiMessage.getString(AUTHOR_FIELD_LABEL)
                : "anonymous";
        final String authorId = scampiMessage.hasString(AUTHOR_ID_FIELD_LABEL)
                ? scampiMessage.getString(AUTHOR_ID_FIELD_LABEL)
                : "anonymous";

        String eventId = scampiMessage.getMetadataString(METADATA_NAMESPACE, EVENT_METADATA_KEY);
        if (eventId == null) eventId = "none";

        // Construct filename
        final String filename = topic + "-" + author + "-" + timestamp
                + "-" + unique + "." + type;
        final File image = new File(this.imageStorageDir, filename);

        // Move binary to storage
        // Continue even if image exists (i.e., has been received previously).
        // This means that the receiver has to check for duplicates using the
        // PictureCard.equals() method, but also means that images don't need to be
        // copied/moved again when restarting the application.
        if (!image.exists()) {
            scampiMessage.moveBinary(IMAGE_DATA_FIELD_LABEL, image);
        }
        // Create the return value

        return new PictureCardVO(topic, image, type, message, timestamp, unique, author, authorId, eventId);
    }

    @Override
    protected void addValueFieldToOutgoingMessage(
            @NonNull final SCAMPIMessage scampiMessage,
            @NonNull final PictureCardVO value) {
        scampiMessage.putBinary(IMAGE_DATA_FIELD_LABEL, value.pictureFile, false);
        scampiMessage.putString(IMAGE_TYPE_LABEL, value.pictureType);
        scampiMessage.putString(TOPIC_FIELD_LABEL, value.topic);
        scampiMessage.putInteger(TIMESTAMP_FIELD_LABEL, value.creationTime);
        scampiMessage.putInteger(ID_FIELD_LABEL, this.rng.nextInt(Integer.MAX_VALUE));
        scampiMessage.putString(MESSAGE_FIELD_LABEL, value.title);

        // Identify the current event in the metadata.
        scampiMessage.setMetadata(METADATA_NAMESPACE, EVENT_METADATA_KEY, value.eventId);

        if (value.author != null) {
            scampiMessage.putString(AUTHOR_FIELD_LABEL, value.author);
        }
        if (value.authorId != null) {
            scampiMessage.putString(AUTHOR_ID_FIELD_LABEL, value.authorId);
        }
        // Note: Title is left out for now (requires UI to create)
    }

    @NonNull
    public IAltFuture<?, SCAMPIMessage> sendMessageAsync(
            @NonNull final Uri image,
            @NonNull final String topic,
            @NonNull final String author,
            @NonNull final String authorId,
            @NonNull final String eventId) {
        // TODO: How do you make the returned future fail immediately, e.g.,
        // TODO (TIMO remove this comment): call cancel(), but this is cooperative and won't blow an error. Instead throw any Exception, often RuntimeException is less hassle. Example throw... below will set the AltFuture to an error state and call down-chain onError method if there is one, like a friendly interrupt of the process
        //TODO Don't know if you need this, but if you want to clean up for example a temp file when there is an error in the process, put a call to the cleanup method in .onError(() -> { do.cleanupFile()
        // when the image uri is invalid?

        // Construct Card data
        final String filePath = UriUtils.getPathForUri(this.context, image);
        if (filePath == null) {
            throw new IllegalStateException("No path to image file: " + image);
        }
        final File imageFile = new File(filePath);
        final String extension = filePath.substring(filePath.lastIndexOf(".") + 1);
        final long timestamp = System.currentTimeMillis();
        final long unique = this.rng.nextInt(Integer.MAX_VALUE);

        // Create a scaled down version to sendEventMessage
        final String filename = topic + "-" + author + "-" + timestamp
                + "-" + unique + "." + extension;
        final File scaledImageFile = new File(this.imageStorageDir, filename);
        try {
            this.createScaledImage(imageFile, scaledImageFile,
                    SCALE_TO_WIDTH_TARGET, SCALE_TO_HEIGHT_TARGET);
        } catch (IOException e) {
            RCLog.d(this, "Failed to scale image (" + e.getMessage() + ")");
            throw new RuntimeException(e);
        }

        // Construct Card
        final PictureCardVO card = new PictureCardVO(topic, scaledImageFile, extension, null,
                timestamp, unique, author, authorId, eventId);

        return this.sendMessageAsync(card);
    }

    //======================================================================//
    // IScampiService
    //======================================================================//
    @NonNull
    @Override
    @CheckResult(suggest = "IAltFuture#fork()")
    public IAltFuture<?, SCAMPIMessage> sendMessageAsync(@NonNull final PictureCardVO val) {
        // TODO: This seems to be duplicated for different types of cards. Needed?
        final SCAMPIMessage.Builder builder = SCAMPIMessage.builder();
        builder.lifetime(MESSAGE_LIFETIME_MINUTES, TimeUnit.MINUTES);
        final SCAMPIMessage scampiMessage = builder.build();
        this.addValueFieldToOutgoingMessage(scampiMessage, val);

        return scampiHandler.sendMessageAsync(getName(), scampiMessage);
    }

    @Override
    protected void notifyMessageExpired(@NonNull String key) {
        //TODO
    }
    //======================================================================//


    //======================================================================//
    // Private
    //======================================================================//
    private void checkMessagePreconditions(@NonNull final SCAMPIMessage scampiMessage)
            throws IOException {
        if (!scampiMessage.hasBinary(IMAGE_DATA_FIELD_LABEL)) {
            throw new IOException("No image data in message.");
        }
        if (!scampiMessage.hasString(IMAGE_TYPE_LABEL)) {
            throw new IOException("No image type in message.");
        }
        if (!scampiMessage.hasString(TOPIC_FIELD_LABEL)) {
            throw new IOException("No topic in message.");
        }
        if (!scampiMessage.hasInteger(TIMESTAMP_FIELD_LABEL)) {
            throw new IOException("No creation timestamp in message.");
        }
        if (!scampiMessage.hasInteger(ID_FIELD_LABEL)) {
            throw new IOException("No id in the message.");
        }
    }
    //======================================================================//


    //======================================================================//
    // Private - image scaling
    //======================================================================//

    /**
     * Creates a scaled thumbnail that will display in an area of a given width
     * and height. The resulting image will not be the exact width and height,
     * instead it's scaled down to the smallest possible size such that: the scale
     * factor is a power of 2 and both dimensions of the resulting image are equal
     * or larger than the corresponding dimensions of the original image.
     *
     * @param srcFile source file
     * @param dstFile destination file
     * @param width   target width
     * @param height  target height
     */
    private void createScaledImage(File srcFile, File dstFile, int width, int height) throws IOException {
        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(srcFile);
            BitmapFactory.decodeStream(fis, null, o);
        } catch (FileNotFoundException e) {
            RCLog.d(this, "Couldn't create thumbnail. (" + e.getMessage() + ")");
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    RCLog.d(this, "Failed to close source file when creating a thumbnail (" + e.getMessage() + ").");
                }
            }
        }

        // Calculate scales
        int h_scale = 1;
        while (o.outWidth / h_scale >= width) {
            h_scale *= 2;
        }
        h_scale /= 2;
        int v_scale = 1;
        while (o.outHeight / v_scale >= height) {
            v_scale *= 2;
        }
        v_scale /= 2;
        // Pick the smaller scale factor
        int scale = (h_scale <= v_scale) ? (h_scale) : (v_scale);
        if (scale <= 0) {
            throw new IOException("Source picture dimensions " +
                    "are invalid.");
        }
        int w = o.outWidth / scale;
        int h = o.outHeight / scale;
        RCLog.d(this, "Thumbnail scale factor: " + scale + ", " +
                "resulting dimensions: " + w + " x " + h + ", " +
                "target dimensions: " + width + " x " + height);

        // Decode scaled down bitmap
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;

        fis = null;
        Bitmap bitmap = null;
        try {
            fis = new FileInputStream(srcFile);
            bitmap = BitmapFactory.decodeStream(fis, null, o2);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    RCLog.e(this, "Failed to close source file when creating a " +
                            "thumbnail (" + e.getMessage() + ")", e);
                }
            }
        }

        if (bitmap == null) {
            throw new IOException("Failed to decode.");
        }

        // Save bitmap to destination
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(dstFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 65, fout);
        } finally {
            if (fout != null) {
                try {
                    fout.flush();
                } catch (Exception e) {
                    RCLog.e(this, "Failed to write file when creating a thumbnail (" + e.getMessage() + ")", e);
                }
                try {
                    fout.close();
                } catch (Exception e) {
                    RCLog.e(this, "Failed to close destination file when creating a thumbnail (" + e.getMessage() + ")", e);
                }
            }
        }
    }
}
