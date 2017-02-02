/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.futurice.vor.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore.Images;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.reactivecascade.Async;
import com.reactivecascade.i.IAltFuture;
import com.reactivecascade.util.RCLog;
import com.futurice.vor.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

/**
 * Thanks to Pablo
 */
public final class ImageUtils {
    /**
     * Constant used to indicate we should recycle the input in
     * {@link #extractThumbnail(Bitmap, int, int, int)} unless the output is the input.
     */
    public static final int OPTIONS_RECYCLE_INPUT = 0x2;
    /**
     * Constant used to indicate the dimension of mini thumbnail.
     */
    public static final int TARGET_SIZE_MEDIUM = 1024;
    /**
     * Constant used to indicate the dimension of mini thumbnail.
     */
    public static final int TARGET_SIZE_MINI = 320;
    /**
     * Constant used to indicate the dimension of micro thumbnail.
     */
    public static final int TARGET_SIZE_MICRO = 96;
    private static final String TAG = ImageUtils.class.getSimpleName();
    /* Maximum pixels size for created bitmap. */
    private static final int MAX_NUM_PIXELS_MEDIUM = 1024 * 1024;
    private static final int MAX_NUM_PIXELS_MINI = 512 * 384;
    private static final int MAX_NUM_PIXELS_MICRO = 160 * 120;
    private static final int UNCONSTRAINED = -1;
    /* Options used internally. */
    private static final int OPTIONS_NONE = 0x0;
    private static final int OPTIONS_SCALE_UP = 0x1;

    @NonNull
    @CheckResult(suggest = "IAltFuture#fork()")
    public static IAltFuture<?, Uri> createResizedImageAsync(
            @NonNull final String sourcePath,
            @NonNull final String targetName,
            final int size) {
        return Async.SERIAL_WORKER.then(() -> {
            final Bitmap bitmap = createImageThumbnail(sourcePath, size);
            final File directory = new File(Environment.getExternalStorageDirectory() + "/image_thumbnails/");
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    throw new IllegalStateException("Can not create directory to reasize image: " + directory);
                }
            }

            final File file = new File(directory.getAbsolutePath() + "/" + targetName + ".jpg");
            final OutputStream outStream = new FileOutputStream(file);

            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream);
                outStream.flush();
            } catch (NullPointerException e) {
                throw new NullPointerException("Can not compress bitmap: " + directory);
            } finally {
                outStream.close();
            }

            return Uri.fromFile(file);
        });
    }

    @CheckResult(suggest = "IAltFuture#fork()")
    public static IAltFuture<?, File> createEmptyImageAsync(File emptyImageFile, Context context) {
        return Async.SERIAL_WORKER.then(() -> {
            final Bitmap bitmap = BitmapFactory.decodeResource(
                    context.getResources(),
                    R.raw.no_image_placeholder);
            final FileOutputStream fos = new FileOutputStream(emptyImageFile);

            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                fos.close();
            }

            return emptyImageFile;
        });
    }

    @CheckResult(suggest = "IAltFuture#fork()")
    public static IAltFuture<?, Uri> createResizedVideoImageAsync(String sourcePath, String targetName, int size) {
        return Async.SERIAL_WORKER.then(() -> {
            final Bitmap bitmap = createVideoThumbnail(sourcePath, size);
            final File directory = new File(Environment.getExternalStorageDirectory() + "/image_thumbnails/");

            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    throw new IllegalStateException("Can not create directory to reasize image: " + directory);
                }
            }

            final File file = new File(directory.getAbsolutePath() + "/" + targetName + ".jpg");
            final OutputStream outStream = new FileOutputStream(file);

            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream);
                outStream.flush();
            } catch (NullPointerException e) {
                throw new NullPointerException("Can not compress bitmap: " + directory);
            } finally {
                outStream.close();
            }

            return Uri.fromFile(file);
        });
    }

    /**
     * This method first examines if the thumbnail embedded in EXIF is bigger than our target
     * size. If not, then it'll create a thumbnail from original image. Due to efficiency
     * consideration, we want to let MediaThumbRequest avoid calling this method twice for
     * both kinds, so it only requests for MICRO_KIND and set saveImage to true.
     * <p>
     * This method always returns a "square thumbnail" for MICRO_KIND thumbnail.
     *
     * @param filePath the path of image file
     * @param size     could be TARGET_SIZE_X
     * @return Bitmap, or null on failures
     */
    @Nullable
    public static Bitmap createImageThumbnail(@NonNull final String filePath,
                                              final int size) {
        int targetSize = getTargetSize(size);
        int maxPixels = getMaxPixels(targetSize);

        final SizedThumbnailBitmap sizedThumbnailBitmap = new SizedThumbnailBitmap();
        Bitmap bitmap = null;
        final String fileType = getFileType(filePath);
        if ("JPG".equals(fileType) || "JPEG".equals(fileType)) {
            createThumbnailFromEXIF(filePath, targetSize, maxPixels, sizedThumbnailBitmap);
            bitmap = sizedThumbnailBitmap.mBitmap;
        }

        if (bitmap == null) {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(filePath);
                final FileDescriptor fd = stream.getFD();
                final BitmapFactory.Options options = new BitmapFactory.Options();

                options.inSampleSize = 1;
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fd, null, options);
                if (options.mCancel || options.outWidth == -1
                        || options.outHeight == -1) {
                    return null;
                }
                options.inSampleSize = computeSampleSize(
                        options, targetSize, maxPixels);
                options.inJustDecodeBounds = false;

                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);
            } catch (IOException ex) {
                Log.e(TAG, "", ex);
            } catch (OutOfMemoryError oom) {
                Log.e(TAG, "Unable to decode file " + filePath + ". OutOfMemoryError.", oom);
            } finally {
                try {
                    if (stream != null) {
                        stream.close();
                    }
                } catch (IOException ex) {
                    Log.e(TAG, "", ex);
                }
            }

        }

        if (targetSize == TARGET_SIZE_MICRO) {
            // now we make it a "square thumbnail" for MICRO_KIND thumbnail
            bitmap = extractThumbnail(bitmap,
                    TARGET_SIZE_MICRO,
                    TARGET_SIZE_MICRO, OPTIONS_RECYCLE_INPUT);
        }

        return bitmap;
    }

    private static int getTargetSize(final int size) {
        switch (size) {
            case TARGET_SIZE_MEDIUM:
                return TARGET_SIZE_MEDIUM;
            case TARGET_SIZE_MICRO:
                return TARGET_SIZE_MICRO;
            case TARGET_SIZE_MINI:
            default:
                return TARGET_SIZE_MINI;
        }
    }

    private static int getMaxPixels(final int size) {
        switch (size) {
            case TARGET_SIZE_MEDIUM:
                return MAX_NUM_PIXELS_MEDIUM;
            case TARGET_SIZE_MICRO:
                return MAX_NUM_PIXELS_MINI;
            case TARGET_SIZE_MINI:
            default:
                return MAX_NUM_PIXELS_MICRO;
        }
    }

    /**
     * Create a video thumbnail for a video. May return null if the video is
     * corrupt or the format is not supported.
     *
     * @param filePath the path of video file
     * @param kind     could be MINI_KIND or MICRO_KIND
     */
    @NonNull

    private static Bitmap createVideoThumbnail(@NonNull final String filePath, final int kind) {
        Bitmap bitmap = null;
        final MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(-1);
        } catch (RuntimeException ex) {
            RCLog.i(ImageUtils.class.getSimpleName(), "Probable corrupt video file, image not created: " + filePath + " : " + ex);
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                RCLog.i(ImageUtils.class.getSimpleName(), "Video file cleanup error: " + filePath + " : " + ex);
            }
        }

        if (bitmap == null) {
            throw new RuntimeException("Corrupt bitmap: " + filePath);
        }

        if (kind == Images.Thumbnails.MINI_KIND) {
            // Scale down the bitmap if it's too large.
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int max = Math.max(width, height);
            if (max > 512) {
                float scale = 512f / max;
                int w = Math.round(scale * width);
                int h = Math.round(scale * height);
                bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
            }
        } else if (kind == Images.Thumbnails.MICRO_KIND) {
            bitmap = extractThumbnail(bitmap,
                    TARGET_SIZE_MICRO,
                    TARGET_SIZE_MICRO,
                    OPTIONS_RECYCLE_INPUT);
        }

        if (bitmap == null) {
            throw new RuntimeException("Corrupt bitmap: " + filePath);
        }

        return bitmap;
    }

    /**
     * Creates a centered bitmap of the desired size.
     *
     * @param source original bitmap source
     * @param width  targeted width
     * @param height targeted height
     */
    @Nullable

    private static Bitmap extractThumbnail(
            Bitmap source, int width, int height) {
        return extractThumbnail(source, width, height, OPTIONS_NONE);
    }

    /**
     * Creates a centered bitmap of the desired size.
     *
     * @param source  original bitmap source
     * @param width   targeted width
     * @param height  targeted height
     * @param options options used during thumbnail extraction
     */
    @Nullable

    private static Bitmap extractThumbnail(
            @Nullable final Bitmap source,
            final int width,
            final int height,
            final int options) {
        if (source == null) {
            return null;
        }

        float scale;
        if (source.getWidth() < source.getHeight()) {
            scale = width / (float) source.getWidth();
        } else {
            scale = height / (float) source.getHeight();
        }
        final Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);

        return transform(matrix, source, width, height,
                OPTIONS_SCALE_UP | options);
    }

    /*
     * Compute the sample size as a function of minSideLength
     * and maxNumOfPixels.
     * minSideLength is used to specify that minimal width or height of a
     * bitmap.
     * maxNumOfPixels is used to specify the maximal size in pixels that is
     * tolerable in terms of memory usage.
     *
     * The function returns a sample size based on the constraints.
     * Both size and minSideLength can be passed in as IImage.UNCONSTRAINED,
     * which indicates no care of the corresponding constraint.
     * The functions prefers returning a sample size that
     * generates a smaller bitmap, unless minSideLength = IImage.UNCONSTRAINED.
     *
     * Also, the function rounds up the sample size to a power of 2 or multiple
     * of 8 because BitmapFactory only honors sample size this way.
     * For example, BitmapFactory downsamples an image by 2 even though the
     * request is 3. So we round up the sample size to avoid OOM.
     */
    private static int computeSampleSize(
            @NonNull final BitmapFactory.Options options,
            final int minSideLength,
            final int maxNumOfPixels) {
        final int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(
            @NonNull final BitmapFactory.Options options,
            final int minSideLength,
            final int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == UNCONSTRAINED) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == UNCONSTRAINED) &&
                (minSideLength == UNCONSTRAINED)) {
            return 1;
        } else if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    /**
     * Make a bitmap from a given Uri, minimal side length, and maximum number of pixels.
     * The image data will be read from specified pfd if it's not null, otherwise
     * a new input stream will be created using specified ContentResolver.
     * <p>
     * Clients are allowed to pass their own BitmapFactory.Options used for bitmap decoding. A
     * new BitmapFactory.Options will be created if options is null.
     */
    @Nullable

    private static Bitmap makeBitmap(
            final int minSideLength,
            final int maxNumOfPixels,
            @NonNull final Uri uri,
            @NonNull final ContentResolver cr,
            @Nullable ParcelFileDescriptor pfd,
            @Nullable BitmapFactory.Options options) {
        Bitmap b = null;

        try {
            if (pfd == null) {
                pfd = makeInputStream(uri, cr);
            }
            if (pfd != null) {
                if (options == null) {
                    options = new BitmapFactory.Options();
                }

                final FileDescriptor fd = pfd.getFileDescriptor();
                options.inSampleSize = 1;
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fd, null, options);
                if (options.mCancel
                        || options.outWidth == -1
                        || options.outHeight == -1) {
                    return null;
                }
                options.inSampleSize = computeSampleSize(
                        options, minSideLength, maxNumOfPixels);
                options.inJustDecodeBounds = false;

                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                b = BitmapFactory.decodeFileDescriptor(fd, null, options);
            }
        } catch (OutOfMemoryError ex) {
            Log.e(TAG, "Got out of memory exception ", ex);
            return null;
        } finally {
            closeSilently(pfd);
        }

        return b;
    }

    private static void closeSilently(@Nullable final ParcelFileDescriptor c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable t) {
                // do nothing
            }
        }
    }

    @Nullable
    private static ParcelFileDescriptor makeInputStream(
            @NonNull final Uri uri,
            @NonNull final ContentResolver cr) {
        try {
            return cr.openFileDescriptor(uri, "r");
        } catch (IOException ex) {
            RCLog.e(ImageUtils.class.getSimpleName(), "Can not make input stream: " + uri);
            return null;
        }
    }

    /**
     * Transform source Bitmap to targeted width and height.
     */
    private static Bitmap transform(
            @NonNull final Matrix scaler,
            @NonNull final Bitmap source,
            final int targetWidth,
            final int targetHeight,
            final int options) {
        final boolean scaleUp = (options & OPTIONS_SCALE_UP) != 0;
        final boolean recycle = (options & OPTIONS_RECYCLE_INPUT) != 0;

        int deltaX = source.getWidth() - targetWidth;
        int deltaY = source.getHeight() - targetHeight;
        if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
            /*
            * In this case the bitmap is smaller, at least in one dimension,
            * than the target.  Transform it by placing as much of the image
            * as possible into the target and leaving the top/bottom or
            * left/right (or both) black.
            */
            Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b2);

            int deltaXHalf = Math.max(0, deltaX / 2);
            int deltaYHalf = Math.max(0, deltaY / 2);
            final Rect src = new Rect(
                    deltaXHalf,
                    deltaYHalf,
                    deltaXHalf + Math.min(targetWidth, source.getWidth()),
                    deltaYHalf + Math.min(targetHeight, source.getHeight()));
            int dstX = (targetWidth - src.width()) / 2;
            int dstY = (targetHeight - src.height()) / 2;
            final Rect dst = new Rect(
                    dstX,
                    dstY,
                    targetWidth - dstX,
                    targetHeight - dstY);
            c.drawBitmap(source, src, dst, null);
            if (recycle) {
                source.recycle();
            }
            c.setBitmap(null);
            return b2;
        }
        float bitmapWidthF = source.getWidth();
        float bitmapHeightF = source.getHeight();

        float bitmapAspect = bitmapWidthF / bitmapHeightF;
        float viewAspect = (float) targetWidth / targetHeight;

        Matrix scalerMatrix = scaler;
        if (bitmapAspect > viewAspect) {
            float scale = targetHeight / bitmapHeightF;
            if (scale < .9F || scale > 1F) {
                scaler.setScale(scale, scale);
            } else {
                scalerMatrix = null;
            }
        } else {
            float scale = targetWidth / bitmapWidthF;
            if (scale < .9F || scale > 1F) {
                scaler.setScale(scale, scale);
            } else {
                scalerMatrix = null;
            }
        }

        final Bitmap b1;
        if (scalerMatrix != null) {
            // this is used for minithumb and crop, so we want to filter here.
            b1 = Bitmap.createBitmap(source, 0, 0,
                    source.getWidth(), source.getHeight(), scalerMatrix, true);
        } else {
            b1 = source;
        }

        if (recycle && b1 != source) {
            source.recycle();
        }

        final int dx1 = Math.max(0, b1.getWidth() - targetWidth);
        final int dy1 = Math.max(0, b1.getHeight() - targetHeight);

        final Bitmap b2 = Bitmap.createBitmap(
                b1,
                dx1 / 2,
                dy1 / 2,
                targetWidth,
                targetHeight);

        if (b2 != b1) {
            if (recycle || b1 != source) {
                b1.recycle();
            }
        }

        return b2;
    }

    /**
     * Creates a bitmap by either downsampling from the thumbnail in EXIF or the full image.
     * The functions returns a SizedThumbnailBitmap,
     * which contains a downsampled bitmap and the thumbnail data in EXIF if exists.
     */
    private static void createThumbnailFromEXIF(
            @Nullable final String filePath,
            final int targetSize,
            final int maxPixels,
            @NonNull final SizedThumbnailBitmap sizedThumbBitmap) {
        if (filePath == null) {
            return;
        }

        ExifInterface exif;
        byte[] thumbData = null;
        try {
            exif = new ExifInterface(filePath);
            thumbData = exif.getThumbnail();
        } catch (IOException ex) {
            Log.w(TAG, ex);
        }

        BitmapFactory.Options fullOptions = new BitmapFactory.Options();
        BitmapFactory.Options exifOptions = new BitmapFactory.Options();
        int exifThumbWidth = 0;
        int fullThumbWidth = 0;

        // Compute exifThumbWidth.
        if (thumbData != null) {
            exifOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, exifOptions);
            exifOptions.inSampleSize = computeSampleSize(exifOptions, targetSize, maxPixels);
            exifThumbWidth = exifOptions.outWidth / exifOptions.inSampleSize;
        }

        // Compute fullThumbWidth.
        fullOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, fullOptions);
        fullOptions.inSampleSize = computeSampleSize(fullOptions, targetSize, maxPixels);
        fullThumbWidth = fullOptions.outWidth / fullOptions.inSampleSize;

        // Choose the larger thumbnail as the returning sizedThumbBitmap.
        if (thumbData != null && exifThumbWidth >= fullThumbWidth) {
            int width = exifOptions.outWidth;
            int height = exifOptions.outHeight;
            exifOptions.inJustDecodeBounds = false;
            sizedThumbBitmap.mBitmap = BitmapFactory.decodeByteArray(thumbData, 0,
                    thumbData.length, exifOptions);
            if (sizedThumbBitmap.mBitmap != null) {
                sizedThumbBitmap.mThumbnailData = thumbData;
                sizedThumbBitmap.mThumbnailWidth = width;
                sizedThumbBitmap.mThumbnailHeight = height;
            }
        } else {
            fullOptions.inJustDecodeBounds = false;
            sizedThumbBitmap.mBitmap = BitmapFactory.decodeFile(filePath, fullOptions);
        }
    }

    @Nullable
    private static String getFileType(@NonNull final String path) {
        int lastDot = path.lastIndexOf('.');
        if (lastDot < 0) {
            return null;
        }

        return path.substring(lastDot + 1).toUpperCase(Locale.ROOT);
    }

    /**
     * SizedThumbnailBitmap contains the bitmap, which is downsampled either from
     * the thumbnail in exif or the full image.
     * mThumbnailData, mThumbnailWidth and mThumbnailHeight are set together only if mThumbnail
     * is not null.
     * <p>
     * The width/height of the sized bitmap may be different from mThumbnailWidth/mThumbnailHeight.
     */
    private static class SizedThumbnailBitmap {
        public byte[] mThumbnailData;
        public Bitmap mBitmap;
        public int mThumbnailWidth;
        public int mThumbnailHeight;
    }
}
