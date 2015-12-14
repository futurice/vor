package com.futurice.scampiclient.items;

import android.support.annotation.NonNull;

/**
 * Model object for Here and Now events. These represent the event or session context where the
 * application is used. E.g., different conferences, or different workshop sessions.
 *
 * New cards published by the user should be tagged with the event name in the metadata to specify
 * the context within which it has been created. This can then be used, e.g., to filter out
 * irrelevant messages in the Scampi router.
 *
 * @author teemuk
 */
public final class Event {
    // XXX:
    // The event system could be expanded to include more information about events etc.
    // For now it is only used to tag sent messages with the latest start time (even if that time
    // is in the future). In a star-configuration the central node can then filter the messages
    // based on the tag.

    /** Unique id of the event. */
    @NonNull
    public final String eventId;
    /** Start time of the event. */
    public final long startTime;

    public Event(
            final @NonNull String eventId,
            final long startTime) {
        this.eventId = eventId;
        this.startTime = startTime;
    }
}
