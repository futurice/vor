package com.futurice.hereandnow.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.support.v4.app.ActivityCompat.startActivity;

public final class HereAndNowUtils {

    private static final String GOOGLE_PHOTOS_URI = "com.google.android.apps.photos.content";
    private static final String MEDIA_DOCUMENT_URI = "com.android.providers.media.documents";
    private static final String DOWNLOADS_DOCUMENT_URI = "com.android.providers.downloads.documents";
    private static final String EXTERNAL_STORAGE_URI = "com.android.externalstorage.documents";

    /**
     * Return the original object if no cleanup is done, or for example "" if there are zero characters worth
     * keeping after cleanup
     * <p>
     * Cleanup means conformance to "#tagnumberlowercasedigitsnotinfirstposition"
     *
     * @param value
     * @return
     */
    @NonNull

    public static String cleanupTag(@NonNull final Object value, final char firstCharacter) {
        final String tag = ((String) value);
        String cleanedTag = tag.trim().toLowerCase();

        while (cleanedTag.startsWith("##") || cleanedTag.startsWith("@@")) {
            cleanedTag = cleanedTag.substring(1);
        }
        final StringBuffer sb = new StringBuffer(cleanedTag.length());
        boolean first = true;
        for (char c : cleanedTag.toCharArray()) {
            if (!Character.isLetter(c)) {
                // Is not a letter
                if (!first && Character.isDigit(c)) {
                    sb.append(c);
                }
                continue;
            }
            sb.append(c);
            first = false;
        }
        String s = sb.toString();
        if (!s.startsWith("" + firstCharacter)) {
            s = firstCharacter + s;
        }
        if (!s.equals(tag)) {
            return s;
        }

        return tag;
    }

    @NonNull

    public static String cleanupTagList(@NonNull final Object value, final char firstCharacter) {
        String tags = (String) value;

        String[] lines = ((String) value).split("[\\p{Space}\\p{Punct}\\r?\\n]");
        List<String> nonTrivialLines = new ArrayList<>();
        for (String line : lines) {
            String s = cleanupTag(line, firstCharacter);
            if (s.length() > 1) {
                nonTrivialLines.add(s);
            }
        }
        StringBuffer sb = new StringBuffer(tags.length());
        for (String line : nonTrivialLines) {
            sb.append(line);
            sb.append("\n");
        }
        String cleanedUpTagList = sb.toString().trim();
        if (!tags.equals(cleanedUpTagList)) {
            return cleanedUpTagList;
        }

        return tags;
    }

    /**
     * Convert R id into a Uri which can be used for pushing that image etc to the UI
     *
     * @param resourceId
     * @return
     */
    @NonNull

    public static Uri getResourceUri(@NonNull final int resourceId) {
        return Uri.parse("android.resource://com.futurice.hereandnow/" + resourceId);
    }

    //From https://code.google.com/p/openintents/source/browse/trunk/compatibility/AndroidSupportV2/src/android/support/v2/app/FragmentPagerAdapter.java#104
    //We can get the fragment tag given by the adapter using this method
    @NonNull

    public static String getFragmentTag(final int viewPagerId, final int fragmentPosition) {
        return "android:switcher:" + viewPagerId + ":" + fragmentPosition;
    }

    public static boolean hasPermission(Context context, String accessCoarseLocation) {
        int permission = ContextCompat.checkSelfPermission(context, accessCoarseLocation);
        return permission != PackageManager.PERMISSION_GRANTED;
    }

    public static String capitalizeFirstLetter(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static boolean isEmailValid(String email) {
        Pattern pattern = Pattern.compile("^(.+\\..+)@futurice.com$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static String getName(String email) {
        if (!isEmailValid(email)) {
            return "";
        }
        String[] name = email.split("@")[0].split("\\.");
        String firstName = HereAndNowUtils.capitalizeFirstLetter(name[0]);
        String lastName = HereAndNowUtils.capitalizeFirstLetter(name[1]);
        return String.format("%s %s", firstName, lastName);
    }

    public static String getInitials(String email) {
        String name = getName(email);
        String[] splittedName = name.split(" ");

        if (splittedName.length > 1) {
            return String.format("%s%s", splittedName[0].charAt(0), splittedName[1].charAt(0));
        } else {
            return email.substring(0, 2).toUpperCase();
        }
    }

    /**
     * Minimizes the app
     * @param context the context
     */
    public static void minimizeApp(Context context) {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startMain);
    }
}
