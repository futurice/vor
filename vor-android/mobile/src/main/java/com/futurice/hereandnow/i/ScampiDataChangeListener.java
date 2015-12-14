package com.futurice.hereandnow.i;

import android.support.annotation.NonNull;

public interface ScampiDataChangeListener<T> {
    /**
     * Callback for new items appearing
     *
     * @param item
     */
    void onItemAdded(@NonNull T item);

    /**
     * Callback for updating items
     *
     * @param uids
     */
    void onItemsUpdated(@NonNull long[] uids);

    /**
     * Callback for removing items
     *
     * @param uids
     */
    void onItemsRemoved(@NonNull long[] uids);
}