/*
 * Copyright (c) 2015 Futurice GmbH. All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.futurice.scampiclient;

import android.support.annotation.NonNull;

import com.futurice.cascade.i.CallOrigin;
import com.futurice.cascade.i.IAltFuture;
import com.futurice.cascade.i.NotCallOrigin;
import com.futurice.cascade.util.RCLog;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fi.tkk.netlab.dtn.scampi.applib.SCAMPIMessage;

@CallOrigin
public class ChatBotService extends HereAndNowService<String> {
    public static final String NOTIFICATION_KEY = "ChatBot";
    private static final String HELLO_SERVICE_NAME = "ChatbotService";
    private static final String CHAT_MESSAGE_FIELD_LABEL = "ChatbotField";
    private static final int MESSAGE_LIFETIME_MINUTES = 10;
    private final String deviceName; // Used for identifying the source device for chat robot messages

    public ChatBotService(ScampiHandler scampiHandler, String deviceName) {
        super(HELLO_SERVICE_NAME, MESSAGE_LIFETIME_MINUTES, TimeUnit.MINUTES, false, scampiHandler);

        this.deviceName = deviceName;
        sendMessageAsync("Chatbot start " + deviceName)
                .fork();
        scheduleMessageBot("10sec Bot " + deviceName, 10000);
        scheduleMessageBot("11sec Bot " + deviceName, 11000);
        RCLog.d(this, "Chatbot started");
    }

    @SuppressWarnings("missingsupercall")
    public void stop() {
        RCLog.d(this, "Stopping ChatbotService");
        sendMessageAsync("Chatbot stop " + deviceName)
                .then(super::stop)
                .onError(e -> {
                    super.stop();
                    RCLog.e(this, "Problem stopping chatbot", e);
                })
                .fork();
    }

    @Override
    public void messageReceived(@NonNull SCAMPIMessage scampiMessage) {
        final String message = scampiMessage.getString(CHAT_MESSAGE_FIELD_LABEL);

        notifyAllListeners(NOTIFICATION_KEY, message);
    }

    @NonNull

    @Override
    protected String getValueFieldFromIncomingMessage(@NonNull SCAMPIMessage scampiMessage) {
        return scampiMessage.getString(CHAT_MESSAGE_FIELD_LABEL);
    }

    @Override
    protected void addValueFieldToOutgoingMessage(@NonNull SCAMPIMessage scampiMessage, @NonNull String value) {
        scampiMessage.putString(HereAndNowService.MESSAGE_FIELD_LABEL, value);
    }

    @Override
    protected void notifyMessageExpired(@NonNull String key) {
        //TODO
    }

    @NonNull

    @NotCallOrigin
    public IAltFuture<?, SCAMPIMessage> sendMessageAsync(@NonNull String message) {
        final SCAMPIMessage.Builder builder = SCAMPIMessage.builder();
        builder.lifetime(MESSAGE_LIFETIME_MINUTES, TimeUnit.MINUTES);
        final SCAMPIMessage scampiMessage = builder.build();
        scampiMessage.putString(CHAT_MESSAGE_FIELD_LABEL, message);

        return scampiHandler.sendMessageAsync(getName(), scampiMessage);
    }

    private ScheduledFuture scheduleMessageBot(String message, long interval) {
        final String messageText = message;
        final long botMessageInterval = interval;

        RCLog.d(this, "Scheduling message in 3 seconds: " + message);
        return hereAndNowScheduledExecService.schedule(new Runnable() {
            @NotCallOrigin
            public void run() {
                if (!stopped) {
                    try {
                        RCLog.v(this, "Attempting bot message sendEventMessage");
                        sendMessageAsync(messageText)
                                .then(() ->
                                        RCLog.d(this, "Bot message sent: " + messageText))
                                .onError(e ->
                                        RCLog.e(this, "Bot message sendEventMessage FAIL: " + messageText, e))
                                .fork();
                    } catch (Exception e) {
                        RCLog.e(this, "Can not sendEventMessage bot message", e);
                    } finally {
                        hereAndNowScheduledExecService.schedule(this, botMessageInterval, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }, botMessageInterval, TimeUnit.MILLISECONDS);
    }
}
