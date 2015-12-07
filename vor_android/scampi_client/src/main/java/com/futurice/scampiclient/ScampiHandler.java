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
import android.support.annotation.Nullable;

import com.futurice.cascade.i.IAltFuture;
import com.futurice.cascade.functional.ImmutableValue;
import com.futurice.cascade.i.IAsyncOrigin;
import com.futurice.cascade.i.NotCallOrigin;
import com.futurice.cascade.util.RCLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fi.tkk.netlab.dtn.scampi.applib.AppLib;
import fi.tkk.netlab.dtn.scampi.applib.AppLibLifecycleListener;
import fi.tkk.netlab.dtn.scampi.applib.LocationUpdateCallback;
import fi.tkk.netlab.dtn.scampi.applib.MessageReceivedCallback;
import fi.tkk.netlab.dtn.scampi.applib.SCAMPIMessage;
import fi.tkk.netlab.dtn.scampi.applib.impl.parser.Protocol;

import static com.futurice.cascade.Async.SERIAL_WORKER;
import static com.futurice.cascade.Async.UI;

/**
 * Connect to the local SCAMPI service over TCP for sending split receiving messages
 */
@NotCallOrigin
public class ScampiHandler implements IAsyncOrigin {
    private static final long RECONNECT_INTERVAL = 5000;
    public final AppLib appLib = AppLib.builder().build();
    final protected ImmutableValue<String> origin = RCLog.originAsync();
    final ScheduledExecutorService reconnectService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "ScampiReconnect"));
    final LinkedList<IScampiService> scampiServices = new LinkedList<>();
    final ConcurrentLinkedQueue<SCAMPIMessage> unpublishedSCAMPIMessages = new ConcurrentLinkedQueue<>();
    private volatile ScampiConnectionState state = ScampiConnectionState.DISCONNECTED;
    private Runnable reconnectRunnable;
    private volatile Future<?> reconnectFuture;
    private volatile boolean paused;

    public ScampiHandler() {
        appLib.start();

        appLib.addLifecycleListener(new AppLibLifecycleListener() {
            @Override
            public void onConnected(String s) {
                RCLog.d(this, "Scampi CONNECTED: " + s);
                setScampiConnectionState(ScampiConnectionState.CONNECTED);
            }

            @Override
            public void onDisconnected() {
                RCLog.d(this, "Scampi disconnected");
                setScampiConnectionState(ScampiConnectionState.DISCONNECTED);
            }

            @Override
            public void onConnectFailed() {
                RCLog.d(this, "Scampi connect failed");
                setScampiConnectionState(ScampiConnectionState.CONNECT_FAILED);
            }

            @Override
            public void onStopped() {
                RCLog.d(this, "Scampi stopped");
                setScampiConnectionState(ScampiConnectionState.STOPPED);
            }
        });

        appLib.addLocationUpdateCallback(new LocationUpdateCallback() {
            @Override
            @SuppressWarnings("deprecation")
            public void locationUpdated(@NonNull final AppLib appLib,
                                        final double v,
                                        final double v2,
                                        final double v3,
                                        final double v4,
                                        final long l) {
                RCLog.d(this, "location updated: " + v + ", " + v2 + ", " + v3 + ", " + v4 + ", " + l);
            }

            @Override
            public void gpsLocationUpdated(Protocol.GpsLocation gpsLocation) {
                RCLog.d(this, "gps location updated: lat:" + gpsLocation.latitude + ", lon:" + gpsLocation.longitude + ", el:" + gpsLocation.elevation + ", error:" + gpsLocation.error + ", timestamp:" + gpsLocation.timestamp);
            }
        });

        appLib.addMessageReceivedCallback(new MessageReceivedCallback() {
            @Override
            public void messageReceived(@NonNull final SCAMPIMessage scampiMessage,
                                        @NonNull final String service) {
                RCLog.v(this, "message received: " + scampiMessage + ", " + service);
                try {
                    scampiMessageReceived(scampiMessage, service);
                } catch (IOException e) {
                    RCLog.e(this, "Problem in synchonous message dispatch", e);
                }
//TODO Debug, was working but..
/*                scampiIThreadType.subscribeTarget(() -> scampiMessageReceived(scampiMessage, service))
                        .subscribeTarget((SCAMPIMessage message) -> Async.UI.v(TAG, "Message received split handled by " + service + ": " + message))
                        .onError((message, e) -> Aspect.UI.e(TAG, "Message receive handler error to service=" + service + ": " + message, e));
*/
            }
        });
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(final boolean paused) {
        UI.execute(() -> {
            this.paused = paused;
            if (paused) {
                cancelReconnect();
            }
        });
    }

    //TODO only shedule reconnect if not in PAUSED or STOPPED state in parent Activity
    public synchronized void scheduleReconnect() {
        if (reconnectRunnable == null) {
            RCLog.v(this, "Scheduling reconnect every " + RECONNECT_INTERVAL + "ms");
            cancelReconnect();
            reconnectRunnable = () -> {
                try {
                    if (isPaused()) {
                        cancelReconnect();
                    }
                    RCLog.v(this, "Attempting reconnect");
                    connect();
                } catch (Exception e) {
                    RCLog.e(this, "Can not reconnect", e);
                }
            };
            reconnectFuture = reconnectService.scheduleAtFixedRate(reconnectRunnable, 0, RECONNECT_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }

    public synchronized void cancelReconnect() {
        if (reconnectFuture != null) {
            RCLog.v(this, "Reconnect cancalled");
            reconnectFuture.cancel(false);
            reconnectFuture = null;
        }
    }

    /**
     * Call this only before connect()
     *
     * @param scampiService
     * @throws InterruptedException
     */
    public void addService(IScampiService scampiService) throws InterruptedException {
        scampiServices.add(scampiService);
        appLib.subscribe(scampiService.getName());
    }

    /**
     * Call after registering services. Service callbacks split messages will start to arrive at some point after this
     *
     * @return
     */
    public void connect() {
        if (state != ScampiConnectionState.CONNECTED) {
            RCLog.d(this, "Attempt connect");
            appLib.connect();
        }
    }

    @NonNull

    public ScampiConnectionState getState() {
        return state;
    }

    /**
     * Call this _after_ you have registered to receive connection state change callbacks to get the
     * initial state.
     *
     * @return
     */
    public boolean isConnected() {
        return state == ScampiConnectionState.CONNECTED;
    }

    private void setScampiConnectionState(@NonNull final ScampiConnectionState newState) {
        this.state = newState;
    }

    /**
     * Hold the SCAMPI thread split use it to copy values until we have received the entire message
     * contents. This throttles the rate at which messages from the cache are delivered at startup
     * to prevent memory/queue overruns. In practice, the messages from SCAMPI core at startup arrive
     * slowly due to flash memory read bandwidth etc so this would mostly guard against startup when
     * app startup does heavy logic, flash etc.
     *
     * @param scampiMessage
     * @param serviceName
     */
    @NonNull

    private SCAMPIMessage scampiMessageReceived(
            @NonNull final SCAMPIMessage scampiMessage,
            @NonNull final String serviceName) throws IOException {
        try {
            if (scampiMessageIsExpired(scampiMessage)) {
                RCLog.i(this, "Received EXPIRED scampi message- ignoring: " + scampiMessage);
                return scampiMessage;
            }
            for (final IScampiService scampiService : scampiServices) {
                if (scampiService.getName().equals(serviceName)) {
                    RCLog.d(this, "Routing message to the right scampi service: " + serviceName);
                    scampiService.messageReceived(scampiMessage);
                    return scampiMessage;
                }
            }
            RCLog.d(this, "Received split ignored message for unknown scampi service: " + serviceName);

            return scampiMessage;
        } finally {
            scampiMessage.close(); // We have a copy- the service can now deallocate the message in the scampi cache as needed
        }
    }

    private boolean scampiMessageIsExpired(@NonNull final SCAMPIMessage scampiMessage) {
        return scampiMessage.getLifetime() <= 0;
    }

    /**
     * IScampiService implementations call this method to dispatch messages
     *
     * @param service
     * @param scampiMessage
     * @return
     */
    @NonNull
    IAltFuture<?, SCAMPIMessage> sendMessageAsync(
            @NonNull final String service,
            @NonNull final SCAMPIMessage scampiMessage) {
        unpublishedSCAMPIMessages.add(scampiMessage);

        return SERIAL_WORKER
                .then(() -> {
                    appLib.publish(
                            scampiMessage,
                            service,
                            (appLib1, publishedScampiMessage) -> unpublishedSCAMPIMessages.remove(publishedScampiMessage));
                    RCLog.d(this, "Message sent to " + service);
                    return scampiMessage;
                });
    }

    /**
     * Close split exit after any pending subscribeTarget operations
     * <p>
     * Note that this can not be called from the workerExecutorService thread. It is normally called
     * during app lifecycle events.
     *
     * @param timeoutMillis
     * @return A list of all SCAMPIMessage objects which have been published but not yet acknowledged by the persistent router cache. These may need to be stored by the app split re-published at next application start
     */
    @NonNull
    public List<SCAMPIMessage> stop(final int timeoutMillis) {
        for (final IScampiService service : scampiServices) {
            /*
             * It is allowed but may or may not perform well for IScampiService.stop() to trigger other
              * asynchronous activities such as message sending during service stop. These activities
              * will try to complete, but if they are too slow during an application end-of-life event
              * subscribeTarget there is a risk the phone will interrupt() these actions
              *
              */
            service.stop();
        }

        try {
            final IAltFuture<?, List<SCAMPIMessage>> servicesStoppedAltFuture = SERIAL_WORKER
                    .then(() ->
                            RCLog.i(this, "Shutdown-associated tasks done"))
                    .onError(e -> {
                        RCLog.e(this, "Some services were not stopped", e);
                    })
                    .then(() -> {
                        reconnectService.shutdownNow();
                        RCLog.d(this, "Sending applib stop");
                        appLib.stop();
                        final List<SCAMPIMessage> unpublishedMessage = new ArrayList<>(unpublishedSCAMPIMessages.size());
                        unpublishedMessage.addAll(unpublishedSCAMPIMessages);
                        synchronized (ScampiHandler.this) {
                            ScampiHandler.this.notifyAll();
                        }

                        return unpublishedMessage;
                    });

            synchronized (this) {
                if (!servicesStoppedAltFuture.isDone()) {
                    RCLog.d(this, "Shutdown-associated tasks not done, waiting " + timeoutMillis);
                    this.wait(timeoutMillis);
                }
                if (servicesStoppedAltFuture.isDone()) {
                    RCLog.d(this, "Shutdown-associated tasks are done, continuing");
                    return servicesStoppedAltFuture.get();
                }
            }
        } catch (InterruptedException e) {
            RCLog.e(this, "Interrupted waiting for IScampiService(s) to stop", e);
        }

        return new ArrayList<>(); // Return an empty list if there was a shutdown problem
    }

    @NonNull
    @Override // IAsyncOrigin
    public ImmutableValue<String> getOrigin() {
        return origin;
    }

    public enum ScampiConnectionState {
        DISCONNECTED("Disconnected"),
        CONNECTED("Connected"),
        CONNECT_FAILED("Connect Failed"),
        STOPPED("Stopped");

        private final String name;

        private ScampiConnectionState(@NonNull String s) {
            name = s;
        }

        public boolean equalsName(@Nullable final String otherName) {
            return (otherName != null) && name.equals(otherName);
        }

        @NonNull
        public String toString() {
            return name;
        }
    }
}
