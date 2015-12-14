package com.futurice.scampiclient;

import android.support.annotation.NonNull;

import com.futurice.cascade.i.IAltFuture;

import java.util.concurrent.TimeUnit;

import fi.tkk.netlab.dtn.scampi.applib.SCAMPIMessage;

/**
 *
 */
public class HereAndNowStringService extends HereAndNowService<String> {
    protected String stringFieldLabel = "HereAndNowStringFieldLabel";

    public HereAndNowStringService(
            @NonNull final String serviceName,
            final int messageLifetime,
            @NonNull final TimeUnit messageLifetimeTimeUnit,
            final boolean persistentMessages,
            @NonNull final ScampiHandler scampiHandler) {
        super(serviceName, messageLifetime, messageLifetimeTimeUnit, persistentMessages, scampiHandler);
    }

    @NonNull

    @Override
    protected String getValueFieldFromIncomingMessage(@NonNull final SCAMPIMessage scampiMessage) {
        return scampiMessage.getString(stringFieldLabel);
    }

    @NonNull

    @Override
    public IAltFuture<?, SCAMPIMessage> sendMessageAsync(@NonNull final String val) {
        final SCAMPIMessage.Builder builder = SCAMPIMessage.builder();
        builder.lifetime(messageLifetime, messageLifetimeTimeUnit);
        final SCAMPIMessage scampiMessage = builder.build();
        scampiMessage.putString(stringFieldLabel, val);

        return scampiHandler.sendMessageAsync(getName(), scampiMessage);
    }

    @Override
    protected void addValueFieldToOutgoingMessage(
            @NonNull final SCAMPIMessage scampiMessage,
            @NonNull final String value) {
        scampiMessage.putString(stringFieldLabel, value);
    }

    @Override
    protected void notifyMessageExpired(@NonNull String key) {
        //TODO
    }
}
