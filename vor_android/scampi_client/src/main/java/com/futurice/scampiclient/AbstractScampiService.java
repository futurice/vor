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
import com.futurice.cascade.util.Origin;
import com.futurice.cascade.util.RCLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CallOrigin
public abstract class AbstractScampiService<T> extends Origin implements IScampiService<T> {
    protected final ScampiHandler scampiHandler;
    private final String serviceName;
    private final List<MessageReceivedListener<T>> messageReceivedListeners = new CopyOnWriteArrayList<>();
    protected boolean stopped = false;

    public AbstractScampiService(@NonNull final String serviceName, @NonNull final ScampiHandler scampiHandler) {
        this.serviceName = serviceName;
        this.scampiHandler = scampiHandler;

        try {
            this.scampiHandler.addService(this);
        } catch (InterruptedException ignore) {
        }
    }

    @NonNull
    public String getName() {
        return serviceName;
    }

    public void stop() {
        RCLog.d(this, "Stopping " + getName());
        stopped = true;
    }

    @Override
    public void addMessageReceivedListener(@NonNull final MessageReceivedListener<T> messageReceivedListener) {
        RCLog.d(this, "Adding message recieved listener:");
        messageReceivedListeners.add(messageReceivedListener);
    }

    @Override
    public void removeMessageReceivedListener(@NonNull final MessageReceivedListener<T> messageReceivedListener) {
        RCLog.d(this, "Stopping " + getName());
        messageReceivedListeners.remove(messageReceivedListener);
    }

    protected final void notifyAllListeners(
            @NonNull final String key,
            @NonNull final T value) {
        final ArrayList<MessageReceivedListener<T>> listeners = new ArrayList<>(messageReceivedListeners.size());
        listeners.addAll(messageReceivedListeners);

        for (MessageReceivedListener<T> messageReceivedListener : listeners) {
            messageReceivedListener.messageReceived(key, value);
        }
    }
}
