package com.futurice.hereandnow.singleton;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.futurice.cascade.util.AssertUtil;
import com.futurice.scampiclient.BigScreenControllerService;
import com.futurice.scampiclient.EventService;
import com.futurice.scampiclient.PictureCardService;
import com.futurice.scampiclient.ScampiHandler;
import com.futurice.scampiclient.ScampiPeerDiscoveryService;
import com.futurice.scampiclient.VideoService;
import com.futurice.scampiclient.VideoUriBroadcastService;
import com.futurice.scampiclient.items.Peer;

import java.io.File;

/**
 * Singleton class for application-wide services and settings
 */
public class ServiceSingleton {

    @Nullable
    private static ServiceSingleton instance;
    @NonNull
    private final ScampiHandler scampiHandler;
    @NonNull
    private final Context context;
    // Scampi services
    // private static ChatBotService chatBotTopLineService;
    @Nullable
    private ScampiPeerDiscoveryService peerDiscoveryService;
    @Nullable
    private PictureCardService pictureCardService;
    @Nullable
    private VideoService videoService;
    @Nullable
    private VideoUriBroadcastService videoBroadcastService;
    @Nullable
    private BigScreenControllerService bigScreenControllerService;
    @Nullable
    private EventService eventService;

    private ServiceSingleton(@NonNull final Context c) {
        context = c;
        scampiHandler = new ScampiHandler();
    }

    public static synchronized ServiceSingleton create(@NonNull final Context context) {
        if (instance != null) {
            return instance;
        }
        return instance = new ServiceSingleton(context);
    }

    @NonNull
    public static ServiceSingleton instance() {
        return AssertUtil.assertNotNull(instance);
    }

    @NonNull
    public ScampiHandler scampiHandler() {
        return scampiHandler;
    }

    @NonNull
    public ScampiPeerDiscoveryService peerDiscoveryService() {
        if (peerDiscoveryService == null) {
            peerDiscoveryService = new ScampiPeerDiscoveryService(
                    scampiHandler,
                    new Peer(ModelSingleton.instance().myTag.get(),
                            ModelSingleton.instance().myIdTag.get(),
                            ModelSingleton.instance().myAboutMe.get(),
                            ModelSingleton.instance().myLikes.get(),
                            ModelSingleton.instance().deletedCards.get(),
                            ModelSingleton.instance().flaggedCards.get(),
                            ModelSingleton.instance().myComments.get(),
                            System.currentTimeMillis())
            );
        }

        return peerDiscoveryService;
    }

    @NonNull
    public PictureCardService pictureCardService() {
        if (pictureCardService == null) {
            final File picCardStorage = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "HereAndNowPics"
            );
            pictureCardService = new PictureCardService(scampiHandler, picCardStorage, context);
        }
        return pictureCardService;
    }

    @NonNull
    public VideoService videoService() {
        if (videoService == null) {
            final File receivedVideoStorage = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "HereAndNowVideos"
            );
            videoService = new VideoService(scampiHandler, receivedVideoStorage, context);
        }
        return videoService;
    }

    @NonNull
    public VideoUriBroadcastService videoBroadcastService() {
        if (videoBroadcastService == null) {
            videoBroadcastService = new VideoUriBroadcastService(scampiHandler, context);
        }
        return videoBroadcastService;
    }

    @NonNull
    public BigScreenControllerService bigScreenControllerService() {
        if (bigScreenControllerService == null) {
            bigScreenControllerService = new BigScreenControllerService(scampiHandler);
        }
        return bigScreenControllerService;
    }

    @NonNull
    public EventService eventService() {
        if (this.eventService == null) {
            this.eventService = new EventService(scampiHandler);
        }
        return this.eventService;
    }
}
