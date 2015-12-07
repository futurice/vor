package com.futurice.scampiclient;

import android.support.annotation.NonNull;

import com.futurice.cascade.i.IAltFuture;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import fi.tkk.netlab.dtn.scampi.applib.SCAMPIMessage;

/**
 * Service for sending commands to a big screen.
 *
 * @author teemuk
 */
public class BigScreenControllerService extends HereAndNowService<BigScreenControllerService.Command> {

    //======================================================================//
    // Constants
    //======================================================================//
    private static final String TAG = BigScreenControllerService.class.getSimpleName();
    /**
     * Scampi service aboutMe to use for created messages.
     */
    private static final String SERVICE_NAME = "com.futurice.hereandnow.BigScreenCommand";
    /**
     * Key for the ScampiMessage content item that contains the command.
     */
    private static final String COMMAND_FIELD_LABEL = "CommandField";
    /**
     * Key for the video id.
     */
    private static final String VIDEO_ID_FIELD_LABEL = "VideoId";
    /**
     * Key for the creation timestamp for the message.
     */
    private static final String TIMESTAMP_FIELD_LABEL = "Timestamp";
    /**
     * Key for a uid ID for the message (timestamp + id = globally uid).
     */
    private static final String ID_FIELD_LABEL = "Id";
    /**
     * Lifetime for the generated ScampiMessages.
     */
    private static final int MESSAGE_LIFETIME_MINUTES = 10;
    private static final int MESSAGE_LIFETIME_HOURS = 12;
    //======================================================================//

    //======================================================================//
    // Instance vars
    //======================================================================//
    private final Random rng = new Random();
    //======================================================================//


    //=============================================================================//
    // Instantiation
    //=============================================================================//
    public BigScreenControllerService(ScampiHandler scampiHandler) {
        super(SERVICE_NAME, MESSAGE_LIFETIME_MINUTES, TimeUnit.MINUTES, false, scampiHandler);
    }
    //=============================================================================//

    @Override
    protected void notifyMessageExpired(@NonNull String key) {
        //TODO
    }

    //=============================================================================//
    // Super class implementation
    //=============================================================================//
    @NonNull

    @Override
    protected Command getValueFieldFromIncomingMessage(@NonNull SCAMPIMessage scampiMessage)
            throws Exception {
        // We don't do anything with the received commands, but something implement
        // for completeness.
        // TODO: Need to include uid ID info in received commands if this is ever used.

        // Precondition check
        this.checkMessagePreconditions(scampiMessage);

        final long type = scampiMessage.getInteger(COMMAND_FIELD_LABEL);
        final long videoId = scampiMessage.getInteger(VIDEO_ID_FIELD_LABEL);

        return new Command(type, videoId);
    }

    @Override
    protected void addValueFieldToOutgoingMessage(@NonNull SCAMPIMessage scampiMessage, @NonNull Command value) {
        scampiMessage.putInteger(COMMAND_FIELD_LABEL, value.type);
        scampiMessage.putInteger(VIDEO_ID_FIELD_LABEL, value.videoId);
        scampiMessage.putInteger(TIMESTAMP_FIELD_LABEL, System.currentTimeMillis());
        scampiMessage.putInteger(ID_FIELD_LABEL, this.rng.nextInt(Integer.MAX_VALUE));
    }

    @NonNull

    @Override
    public IAltFuture<?, SCAMPIMessage> sendMessageAsync(@NonNull Command val) {
        final SCAMPIMessage.Builder builder = SCAMPIMessage.builder();
        builder.lifetime(MESSAGE_LIFETIME_MINUTES, TimeUnit.MINUTES);
        final SCAMPIMessage scampiMessage = builder.build();
        this.addValueFieldToOutgoingMessage(scampiMessage, val);

        return scampiHandler.sendMessageAsync(getName(), scampiMessage)
                ;
    }
    //=============================================================================//


    //=============================================================================//
    // Private
    //=============================================================================//
    private void checkMessagePreconditions(SCAMPIMessage scampiMessage)
            throws IOException {
        if (!scampiMessage.hasInteger(COMMAND_FIELD_LABEL)) {
            throw new IOException("No command in message.");
        }
        if (!scampiMessage.hasInteger(VIDEO_ID_FIELD_LABEL)) {
            throw new IOException("No video ID in message.");
        }
        if (!scampiMessage.hasInteger(TIMESTAMP_FIELD_LABEL)) {
            throw new IOException("No creation timestamp in message.");
        }
        if (!scampiMessage.hasInteger(ID_FIELD_LABEL)) {
            throw new IOException("No id in the message.");
        }
    }
    //=============================================================================//


    //=============================================================================//
    // Command class
    //=============================================================================//
    public static class Command {
        public static final long PLAY_COMMAND = 0;

        public final long type;
        public final long videoId;

        public Command(final long type, final long videoId) {
            this.type = type;
            this.videoId = videoId;
        }
    }
    //=============================================================================//
}
