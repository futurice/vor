/*
 * Copyright (C) 2007-2008 OpenIntents.org
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

package com.futurice.hereandnow.utils;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.futurice.cascade.functional.ImmutableValue;
import com.futurice.cascade.util.Origin;
import com.futurice.cascade.util.RCLog;

import java.io.File;
import java.text.DecimalFormat;

/**
 * @author Peli
 * @author paulburke (ipaulpro)
 * @version 2013-12-11
 */
public final class FileUtils extends Origin {
    public static final String MIME_TYPE_AUDIO = "audio/*";
    public static final String MIME_TYPE_TEXT = "text/*";
    public static final String MIME_TYPE_IMAGE = "image/*";
    public static final String MIME_TYPE_VIDEO = "video/*";
    public static final String MIME_TYPE_APP = "application/*";
    public static final String HIDDEN_PREFIX = ".";
    /**
     * TAG for log messages.
     */
    static final String TAG = FileUtils.class.getSimpleName();
    private static final boolean DEBUG = false; // Set to true to enable logging
    private final ImmutableValue<String> origin = RCLog.originAsync();

    private FileUtils() {
        //private constructor to enforce Singleton pattern
    }

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     *
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension;
     * null if uri was null.
     */
    @NonNull

    public static String getExtension(@NonNull final String uri) {
        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(dot);
        } else {
            // No extension.
            return "";
        }
    }

    /**
     * @return Whether the URI is a local one.
     */
    public static boolean isLocal(@NonNull final String url) {
        return !url.startsWith("http://") && !url.startsWith("https://");
    }

    /**
     * @return True if Uri is a MediaStore Uri.
     * @author paulburke
     */
    public static boolean isMediaUri(@NonNull final Uri uri) {
        return "media".equalsIgnoreCase(uri.getAuthority());
    }

    /**
     * Convert File into Uri.
     *
     * @param file
     * @return uri
     */
    @NonNull
    public static Uri getUri(@NonNull final File file) {
        return Uri.fromFile(file);
    }

    /**
     * Returns the path only (without file name).
     *
     * @param file
     * @return
     */
    @NonNull
    public static File getPathWithoutFilename(@NonNull final File file) {
        if (file.isDirectory()) {
            // no file to be split off. Return everything
            return file;
        } else {
            String filename = file.getName();
            String filepath = file.getAbsolutePath();

            // Construct path without file name.
            String pathwithoutname = filepath.substring(0,
                    filepath.length() - filename.length());
            if (pathwithoutname.endsWith("/")) {
                pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length() - 1);
            }
            return new File(pathwithoutname);
        }
    }

    /**
     * @return The MIME type for the given file.
     */
    @NonNull
    public static String getMimeType(@NonNull final File file) {
        final String extension = getExtension(file.getName());

        if (extension.length() > 0)
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));

        return "application/octet-stream";
    }

    /**
     * @return The MIME type for the give Uri.
     */
    @Nullable
    public static String getMimeType(@NonNull final Context context,
                                     @NonNull final Uri uri) {
        final String path = getPath(context, uri);
        if (path == null) {
            Log.i(TAG, "Null path in mime type: " + uri);
            return null;
        }
        final File file = new File(path);
        return getMimeType(file);
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(@NonNull final Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(@NonNull final Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider
     */
    public static boolean isMediaDocument(@NonNull final Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(@NonNull final Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    @Nullable

    public static String getDataColumn(
            @NonNull final Context context,
            @NonNull final Uri uri,
            @Nullable final String selection,
            @Nullable final String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                if (DEBUG)
                    DatabaseUtils.dumpCursor(cursor);

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return null;
    }


    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br>
     * <br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @see #isLocal(String)
     * @see #getFile(Context, Uri)
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Nullable

    public static String getPath(@NonNull final Context context,
                                 @NonNull final Uri uri) {
        if (DEBUG)
            RCLog.d(TAG,
                    "Authority: " + uri.getAuthority() +
                            ", Fragment: " + uri.getFragment() +
                            ", Port: " + uri.getPort() +
                            ", Query: " + uri.getQuery() +
                            ", Scheme: " + uri.getScheme() +
                            ", Host: " + uri.getHost() +
                            ", Segments: " + uri.getPathSegments().toString()
            );

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return null; //FIXME Can we support these operations on older phones?
        }

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                final Uri contentUri;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else {
                    throw new UnsupportedOperationException("Unrecognized file type: " + uri);
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Convert Uri into File, if possible.
     *
     * @param context
     * @param uri
     * @return file A local file that the Uri was pointing to, or null if the
     * Uri is unsupported or pointed to a remote resource.
     * @see #getPath(Context, Uri)
     */
    @Nullable

    public static File getFile(@NonNull final Context context,
                               @NonNull final Uri uri) {
        final String path = getPath(context, uri);
        if (path != null && isLocal(path)) {
            return new File(path);
        }
        return null;
    }

    /**
     * Get the file size in a human-readable string.
     *
     * @param size
     * @return
     */
    @NonNull

    public static String getReadableFileSize(final int size) {
        final int BYTES_IN_KILOBYTES = 1024;
        final DecimalFormat dec = new DecimalFormat("###.#");
        final String KILOBYTES = " KB";
        final String MEGABYTES = " MB";
        final String GIGABYTES = " GB";
        float fileSize = 0;
        String suffix = KILOBYTES;

        if (size > BYTES_IN_KILOBYTES) {
            fileSize = size / BYTES_IN_KILOBYTES;
            if (fileSize > BYTES_IN_KILOBYTES) {
                fileSize = fileSize / BYTES_IN_KILOBYTES;
                if (fileSize > BYTES_IN_KILOBYTES) {
                    fileSize = fileSize / BYTES_IN_KILOBYTES;
                    suffix = GIGABYTES;
                } else {
                    suffix = MEGABYTES;
                }
            }
        }
        return String.valueOf(dec.format(fileSize) + suffix);
    }

    /**
     * Attempt to retrieve the thumbnail of given File from the MediaStore. This
     * should not be called on the UI thread.
     *
     * @param context
     * @param file
     * @return
     */
    @Nullable

    public static Bitmap getThumbnail(@NonNull final Context context,
                                      @NonNull final File file) {
        return getThumbnail(context, getUri(file), getMimeType(file));
    }

    /**
     * Attempt to retrieve the thumbnail of given Uri from the MediaStore. This
     * should not be called on the UI thread.
     *
     * @param context
     * @param uri
     * @return
     */
    @Nullable

    public static Bitmap getThumbnail(@NonNull final Context context,
                                      @NonNull final Uri uri) {
        return getThumbnail(context, uri, getMimeType(context, uri));
    }

    /**
     * Attempt to retrieve the thumbnail of given Uri from the MediaStore. This
     * should not be called on the UI thread.
     *
     * @param context
     * @param uri
     * @param mimeType
     * @return
     */
    @Nullable

    public static Bitmap getThumbnail(
            @NonNull final Context context,
            @NonNull final Uri uri,
            @NonNull final String mimeType) {
        if (DEBUG)
            RCLog.d(TAG, "Attempting to get thumbnail");

        if (!isMediaUri(uri)) {
            RCLog.e(TAG, "You can only retrieve thumbnails for images and videos.");
            return null;
        }

        Bitmap bm = null;
        final ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int id = cursor.getInt(0);
                if (DEBUG)
                    RCLog.d(TAG, "Got thumb ID: " + id);
                if (mimeType.contains("video")) {
                    bm = MediaStore.Video.Thumbnails.getThumbnail(
                            resolver,
                            id,
                            MediaStore.Video.Thumbnails.MINI_KIND,
                            null);
                } else if (mimeType.contains(FileUtils.MIME_TYPE_IMAGE)) {
                    bm = MediaStore.Images.Thumbnails.getThumbnail(
                            resolver,
                            id,
                            MediaStore.Images.Thumbnails.MINI_KIND,
                            null);
                }
            }
        } catch (Exception e) {
            RCLog.e(TAG, "getThumbnail", e);
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return bm;
    }

//    /**
//     * File and folder comparator. TODO Expose sorting option method
//     *
//     * @author paulburke
//     */
//    @NonNull
//    public static Comparator<File> sComparator = (f1, f2) -> {
//        // Sort alphabetically by lower case, which is much cleaner
//        return f1.getName().toLowerCase().compareTo(
//                f2.getName().toLowerCase());
//    };
//
//    /**
//     * File (not directories) filter.
//     *
//     * @author paulburke
//     */
//    @NonNull
//    public static FileFilter sFileFilter = file -> {
//        final String fileName = file.getName();
//        // Return files only (not directories) and skip hidden files
//        return file.isFile() && !fileName.startsWith(HIDDEN_PREFIX);
//    };
//
//    /**
//     * Folder (directories) filter.
//     *
//     * @author paulburke
//     */
//    @NonNull
//    public static FileFilter sDirFilter = file -> {
//        final String fileName = file.getName();
//        // Return directories only and skip hidden directories
//        return file.isDirectory() && !fileName.startsWith(HIDDEN_PREFIX);
//    };

    /**
     * Get the Intent for selecting content to be used in an Intent Chooser.
     *
     * @return The intent for opening a file with Intent.createChooser()
     * @author paulburke
     */
//    @NonNull
//    public static Intent createGetContentIntent() {
//        // Implicitly allow the user to select a particular kind of data
//        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        // The MIME data type filter
//        intent.setType("*/*");
//        // Only return URIs that can be opened with ContentResolver
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        return intent;
//    }
}