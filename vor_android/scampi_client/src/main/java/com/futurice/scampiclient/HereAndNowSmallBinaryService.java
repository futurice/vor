package com.futurice.scampiclient;

import android.support.annotation.NonNull;

import com.futurice.cascade.i.IAltFuture;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import fi.tkk.netlab.dtn.scampi.applib.SCAMPIMessage;

/**
 *
 */
public class HereAndNowSmallBinaryService extends HereAndNowService<byte[]> {
    protected String smallBinaryFieldLabel = "HereAndNowSmallBinaryField";

    public HereAndNowSmallBinaryService(
            @NonNull final String serviceName,
            final int messageLifetime,
            @NonNull final TimeUnit messageLifetimeTimeUnit,
            final boolean persistentMessages,
            @NonNull final ScampiHandler scampiHandler) {
        super(serviceName, messageLifetime, messageLifetimeTimeUnit, persistentMessages, scampiHandler);
    }

    @NonNull
    @Override
    public IAltFuture<?, SCAMPIMessage> sendMessageAsync(@NonNull final byte[] val) {
        final SCAMPIMessage.Builder builder = SCAMPIMessage.builder();
        builder.lifetime(messageLifetime, messageLifetimeTimeUnit);
        final SCAMPIMessage scampiMessage = builder.build();
        scampiMessage.putBinary(smallBinaryFieldLabel, val);

        return scampiHandler.sendMessageAsync(getName(), scampiMessage);
    }

    @NonNull
    @Override
    protected byte[] getValueFieldFromIncomingMessage(@NonNull final SCAMPIMessage scampiMessage) throws IOException {
        return scampiMessage.getBinaryBuffer(smallBinaryFieldLabel);
    }

    @Override
    protected void addValueFieldToOutgoingMessage(@NonNull final SCAMPIMessage scampiMessage, @NonNull byte[] value) {
        scampiMessage.putBinary(MESSAGE_FIELD_LABEL, value);
    }

    @Override
    protected void notifyMessageExpired(@NonNull String key) {
        //TODO
    }
}
