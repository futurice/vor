/*
 Copyright (c) 2015 Futurice GmbH. All rights reserved. http://futurice.com/
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
package com.futurice.scampiclient;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.futurice.cascade.i.CallOrigin;
import com.futurice.cascade.i.IAltFuture;
import com.futurice.cascade.i.NotCallOrigin;
import com.futurice.cascade.util.RCLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fi.tkk.netlab.dtn.scampi.applib.SCAMPIMessage;

import static com.futurice.cascade.Async.SERIAL_WORKER;

/**
 *
 */
public class VideoBroadcastService extends HereAndNowService<byte[]> {
    public static final String NOTIFICATION_KEY = "VideoBroadcast";
    private static final String SERVICE_NAME = "VideoBroadcastService";
    private static final String VIDEO_DATA_FIELD_LABEL = "VideoField";
    private static final int MESSAGE_LIFETIME_MINUTES = 10;
    private Resources resources;
    private int id;

    public VideoBroadcastService(
            @NonNull final ScampiHandler scampiHandler,
            @NonNull final Resources resources,
            final int id) {
        super(SERVICE_NAME, MESSAGE_LIFETIME_MINUTES, TimeUnit.MINUTES, false, scampiHandler);

        this.resources = resources;
        this.id = id;

//TODO Timed sendEventMessage disabled. Remove when no longer needed        scheduleVideoBot(id, 60000); //ms
        RCLog.d(this, "VideoBroadcastService started");
    }

    @SuppressWarnings("missingsupercall")
    public void stop() {
        RCLog.d(this, "Stopping VideoBroadcastService");
        SERIAL_WORKER
                .then(super::stop)
                .onError(e -> {
                    super.stop();
                    RCLog.e(this, "Problem stopping VideoBroadcastService", e);
                });
    }

    public void messageReceived(@NonNull final SCAMPIMessage scampiMessage) {
        try {
            final byte[] video = scampiMessage.getBinaryBuffer(VIDEO_DATA_FIELD_LABEL);
            notifyAllListeners(NOTIFICATION_KEY, video);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    protected byte[] getValueFieldFromIncomingMessage(@NonNull SCAMPIMessage scampiMessage) throws IOException {
        return scampiMessage.getBinaryBuffer(VIDEO_DATA_FIELD_LABEL);
    }

    @Override
    protected void addValueFieldToOutgoingMessage(@NonNull SCAMPIMessage scampiMessage, @NonNull byte[] value) {
        scampiMessage.putBinary(HereAndNowService.MESSAGE_FIELD_LABEL, value);
    }

    @Override
    protected void notifyMessageExpired(@NonNull String key) {
        //TODO
    }

    @NonNull
    @Override
    public IAltFuture<?, SCAMPIMessage> sendMessageAsync(@NonNull byte[] val) {
        final SCAMPIMessage.Builder builder = SCAMPIMessage.builder();
        builder.lifetime(MESSAGE_LIFETIME_MINUTES, TimeUnit.MINUTES);
        final SCAMPIMessage scampiMessage = builder.build();
        scampiMessage.putBinary(VIDEO_DATA_FIELD_LABEL, val);

        return scampiHandler.sendMessageAsync(getName(), scampiMessage);
    }

    @NotCallOrigin
    public IAltFuture<?, SCAMPIMessage> sendMessageAsync(final int id) {
        final SCAMPIMessage.Builder builder = SCAMPIMessage.builder();
        builder.lifetime(MESSAGE_LIFETIME_MINUTES, TimeUnit.MINUTES);
        final SCAMPIMessage scampiMessage = builder.build();

        InputStream is = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            is = resources.openRawResource(id);
            final byte[] buf = new byte[16384];
            int read;
            while ((read = is.read(buf)) >= 0) {
                bos.write(buf, 0, read);
            }
            scampiMessage.putBinary(VIDEO_DATA_FIELD_LABEL, bos.toByteArray());
        } catch (IOException e) {
            RCLog.e(this, "Can not do buffer copy hack to sendEventMessage a video", e);
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    RCLog.e(this, "Can not close videostream input", e);
                }
            }
            try {
                bos.close();
            } catch (IOException e) {
                RCLog.e(this, "Can not close videostream buffer output", e);
            } finally {
                bos = null;
            }
        }

        return scampiHandler.sendMessageAsync(getName(), scampiMessage);
    }

    private ScheduledFuture scheduleVideoBot(int id, long interval) {
        final long botMessageInterval = interval;

        RCLog.v(this, "Scheduling VideoBroadcastService sendEventMessage in 60 seconds: " + id);
        return hereAndNowScheduledExecService.schedule(new Runnable() {
            @CallOrigin
            public void run() {
                if (!stopped) {
                    try {
                        RCLog.v(this, "Attempting VideoBroadcastService message sendEventMessage");
                        sendMessageAsync(id)
                                .then(() ->
                                        RCLog.d(this, "VideoBroadcastService video sent: " + id))
                                .onError(e ->
                                        RCLog.e(this, "VideoBroadcastService video sendEventMessage FAIL: " + id, e))
                                .fork();
                    } catch (Exception e) {
                        RCLog.e(this, "Can not sendEventMessage VideoBroadcast message", e);
                    } finally {
                        hereAndNowScheduledExecService.schedule(this, botMessageInterval, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }, botMessageInterval, TimeUnit.MILLISECONDS);
    }
}

