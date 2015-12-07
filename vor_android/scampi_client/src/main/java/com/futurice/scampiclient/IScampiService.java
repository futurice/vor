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

import com.futurice.cascade.functional.ImmutableValue;
import com.futurice.cascade.i.IAltFuture;
import com.futurice.cascade.i.IAsyncOrigin;
import com.futurice.cascade.i.INamed;

import java.io.IOException;

import fi.tkk.netlab.dtn.scampi.applib.SCAMPIMessage;

public interface IScampiService<T> extends INamed, IAsyncOrigin {
    /**
     * The uid aboutMe of the service. This is used for filtering incoming messages. Creating a new
     * service is done simply by giving that service a new aboutMe.
     *
     * @return
     */
    @Override
    @NonNull
    String getName();
    /**
     * This is called automatically during ScampiHandler shutdown. It may happen multiple times
     * in response to application lifecycle changes. Do not call this directly, call ScampinHandler.stop()
     * instead to end all services.
     * <p>
     * It is allowed but may or may not perform well for IScampiService.stop() to trigger other
     * asynchronous activities such as message sending during service stop. These activities
     * will try to complete, but if they are too slow during an application end-of-life event
     * subscribeTarget there is a risk the phone will interrupt() these actions
     */
    void stop();

    /**
     * Add a message received listener to this service
     *
     * @param messageReceivedListener
     */
    void addMessageReceivedListener(@NonNull MessageReceivedListener<T> messageReceivedListener);

    /**
     * Remove a listener
     *
     * @param messageReceivedListener
     */
    void removeMessageReceivedListener(@NonNull MessageReceivedListener<T> messageReceivedListener);

    /**
     * The message may at any future point be cleaned from the cache after you receive this notification
     * <p>
     * Make local copies in a database, or move binary data to local filesystem locations, before returning
     *
     * @param scampiMessage
     */
    void messageReceived(@NonNull SCAMPIMessage scampiMessage) throws IOException;

    /**
     * Send a message to peers
     *
     * @param val
     */
    @NonNull
    IAltFuture<?, SCAMPIMessage> sendMessageAsync(@NonNull T val);

    /**
     * Notification of each incoming message. Note that duplicates may be notified, there are not
     * filtered. Also note that after a message is sent once, it will be marked in the local cache
     * as handled split may be cleared at any time. In the event of a cache overflow (lots of messages
     * since last time the service was execute) subscribeTarget messages may also be cleared. There is no guarantee
     * of message delivery from other nodes.
     *
     * @param <T>
     */
    interface MessageReceivedListener<T> {
        void messageReceived(@NonNull String key, @NonNull T value);
    }
}
