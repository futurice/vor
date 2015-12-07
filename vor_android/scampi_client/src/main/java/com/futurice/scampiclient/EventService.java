package com.futurice.scampiclient;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.futurice.cascade.i.IAltFuture;
import com.futurice.scampiclient.items.Event;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import fi.tkk.netlab.dtn.scampi.applib.SCAMPIMessage;

/**
 * Service for receiving event updates.
 *
 * @author teemuk
 */
public final class EventService extends HereAndNowService<Event> {

    //======================================================================//
    // Scampi Message stuff
    //======================================================================//
    /**
     * Scampi service for event messages.
     */
    private static final String SERVICE_NAME = "com.futurice.hereandnow.Event";
    /**
     * Lifetime for the generated ScampiMessages.
     */
    private static final int MESSAGE_LIFETIME_MINUTES = 2 * 24 * 60;

    /**
     * Human readable name for the event.
     */
    private static final String EVENT_NAME_LABEL = "eventName";
    /**
     * Unique identifier string for the event. This will be included in all message metadata.
     */
    private static final String EVENT_ID_LABEL = "eventId";
    /**
     * Timestamp for the start of the event.
     */
    private static final String EVENT_START_LABEL = "startTime";
    /**
     * Timestamp for the end of the event.
     */
    private static final String EVENT_END_LABEL = "endTime";

    @Nullable
    private Event latestStartEvent;

    //======================================================================//


    //======================================================================//
    // API
    //======================================================================//
    public EventService(final ScampiHandler scampiHandler) {
        super(SERVICE_NAME, MESSAGE_LIFETIME_MINUTES, TimeUnit.MINUTES,
                false, scampiHandler);
    }
    //======================================================================//

    //======================================================================//
    // Private
    //======================================================================//
    private static void checkMessagePreconditions(
            @NonNull final SCAMPIMessage scampiMessage)
            throws IOException {
        if (!scampiMessage.hasString(EVENT_ID_LABEL)) {
            throw new IOException("No image type in message.");
        }
        if (!scampiMessage.hasInteger(EVENT_START_LABEL)) {
            throw new IOException("No creation timestamp in message.");
        }
    }

    //======================================================================//
    // HereAndNowService
    //======================================================================//
    @NonNull
    @Override
    protected Event getValueFieldFromIncomingMessage(
            @NonNull final SCAMPIMessage scampiMessage)
            throws Exception {
        checkMessagePreconditions(scampiMessage);

        final String eventId = scampiMessage.getString(EVENT_ID_LABEL);
        String name = "unnamed";
        final long startTime = scampiMessage.getInteger(EVENT_START_LABEL);
        final Event event = new Event(eventId, startTime);

        if (latestStartEvent == null || event.startTime > latestStartEvent.startTime) {
            latestStartEvent = event;
        }

        return event;
    }
    //======================================================================//

    @Override
    protected void addValueFieldToOutgoingMessage(
            @NonNull final SCAMPIMessage scampiMessage,
            @NonNull final Event value) {
        scampiMessage.putString(EVENT_ID_LABEL, value.eventId);
        scampiMessage.putInteger(EVENT_START_LABEL, value.startTime);
    }

    //======================================================================//
    // IScampiService
    //======================================================================//
    @NonNull
    @Override
    public IAltFuture<?, SCAMPIMessage> sendMessageAsync(@NonNull final Event val) {
        throw new UnsupportedOperationException("Clients cannot publish events.");
    }
    //======================================================================//

    @Override
    protected void notifyMessageExpired(@NonNull final String key) {

    }

    /**
     * The event received which starts _after_ all other events which have been received
     *
     * @return
     */
    @Nullable
    public Event getLatestStartEvent() {
        return latestStartEvent;
    }

}
