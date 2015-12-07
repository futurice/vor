package com.futurice.hereandnow.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;

import com.futurice.cascade.i.CallOrigin;
import com.futurice.cascade.i.IAltFuture;
import com.futurice.cascade.util.RCLog;
import com.futurice.hereandnow.Constants;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.activity.NewTopicActivity;
import com.futurice.hereandnow.adapter.TopicListAdapter;
import com.futurice.hereandnow.card.ICard;
import com.futurice.hereandnow.card.ITopic;
import com.futurice.hereandnow.card.ImageCard;
import com.futurice.hereandnow.card.Topic;
import com.futurice.hereandnow.card.VideoCard;
import com.futurice.hereandnow.i.ScampiDataChangeListener;
import com.futurice.hereandnow.utils.HereAndNowUtils;
import com.futurice.scampiclient.items.PictureCardVO;
import com.futurice.scampiclient.items.VideoCardVO;
import com.futurice.scampiclient.utils.ArrayUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static com.futurice.cascade.Async.UI;

public class TrendingFragment extends BaseHereAndNowFragment {

    private final ScampiDataChangeListener<VideoCardVO> videoCardReceivedListener = new ScampiDataChangeListener<VideoCardVO>() {
        @Override
        public void onItemAdded(@NonNull final VideoCardVO item) {
            gotVideoMessage(item);
        }

        @Override
        public void onItemsUpdated(@NonNull final long[] uids) {
            updateTopicLikes(uids);
        }

        @Override
        public void onItemsRemoved(@NonNull final long[] uids) {
            removeCards(uids);
        }
    };

    protected int lastExpanded = -1;
    private ScampiDataChangeListener<PictureCardVO> pictureCardReceivedListener = new ScampiDataChangeListener<PictureCardVO>() {
        @Override
        public void onItemAdded(@NonNull final PictureCardVO card) {
            gotPictureCard(card);
        }

        @Override
        public void onItemsUpdated(@NonNull final long[] uids) {
            updateTopicLikes(uids);
        }

        @Override
        public void onItemsRemoved(@NonNull final long[] uids) {
            removeCards(uids);
        }
    };

    @NonNull

    public static TrendingFragment newInstance() {
        return new TrendingFragment();
    }

    @Override
    @NonNull

    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View rootView = setupView(inflater, container);
        initViewsAndAdapters();
        initListView();

        final FrameLayout messageListFrameLayout = (FrameLayout) rootView.findViewById(R.id.here_and_now_message_list);
        messageListFrameLayout.addView(getExpandableListView());

        if (getSourceTopicModel().isEmpty()) {
            initTopicsAndCards(createPreBuiltTopics(), getSourceTopicModel(), getTopicListAdapter());
        }

        connectScampiServices();
        modelSingleton.notifyAllPictureCards(pictureCardReceivedListener);
        modelSingleton.notifyAllVideoCards(videoCardReceivedListener);

        // Update filtering right away to get prebuilt content on the screen
        filterModel();

        return rootView;
    }

    @NonNull

    protected View setupView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container) {
        return inflater.inflate(R.layout.fragment_trending, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disconnectScampiServices();
    }

    protected void initViewsAndAdapters() {
        // Set ExpandableListView values
        final ExpandableListView elv = new ExpandableListView(getActivity());
        setExpandableListView(elv);
        elv.setOnGroupExpandListener(topicIndex -> {
            if (lastExpanded != -1 && topicIndex != lastExpanded) {
                elv.collapseGroup(lastExpanded);
            }
            lastExpanded = topicIndex;
        });
        TopicListAdapter tla = new TopicListAdapter(getSourceTopicModel(),
                "TrendingExpandableListAdapter");
        setTopicListAdapter(tla);
        tla.setSortFunction(ITopic::compare);
        tla.setFilterFunction(topic -> ((Topic) topic).getLikes() > 0 || topic.isPrebuiltTopic());
    }

    @CallOrigin
    @NonNull

    protected List<ITopic> createPreBuiltTopics() {
        final String packageName = getActivity().getPackageName();
        final Date creationDate = new GregorianCalendar(2015, 5, 1, 12, 0, 0).getTime();
        final List<ITopic> list = new ArrayList<>();

//        {
//            // How-To
//            final Topic topic = new Topic("How-To Guide", 1001, this.getActivity());
//            topic.setText("How-To Guide");
//            topic.isPrebuiltTopic();
//
//            {
//                ImageCard card = new ImageCard("__", 500, this.getActivity());
//                card.setText("How-To 1");
//                card.setAuthor("CNR", "CNR");
//                card.setDate(creationDate);
//                card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.howto_1));
//                topic.addCard(card);
//            }
//            {
//                ImageCard card = new ImageCard("__", 501, this.getActivity());
//                card.setText("How-To 2");
//                card.setAuthor("CNR", "CNR");
//                card.setDate(creationDate);
//                card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.howto_2));
//                topic.addCard(card);
//            }
//            {
//                ImageCard card = new ImageCard("__", 502, this.getActivity());
//                card.setText("How-To 3");
//                card.setAuthor("CNR", "CNR");
//                card.setDate(creationDate);
//                card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.howto_3));
//                topic.addCard(card);
//            }
//            {
//                ImageCard card = new ImageCard("__", 503, this.getActivity());
//                card.setText("How-To 4");
//                card.setAuthor("CNR", "CNR");
//                card.setDate(creationDate);
//                card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.howto_4));
//                topic.addCard(card);
//            }
//            {
//                ImageCard card = new ImageCard("__", 504, this.getActivity());
//                card.setText("How-To 5");
//                card.setAuthor("CNR", "CNR");
//                card.setDate(creationDate);
//                card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.howto_5));
//                topic.addCard(card);
//            }
//            {
//                ImageCard card = new ImageCard("__", 505, this.getActivity());
//                card.setText("How-To 6");
//                card.setAuthor("CNR", "CNR");
//                card.setDate(creationDate);
//                card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.howto_6));
//                topic.addCard(card);
//            }
//            {
//                ImageCard card = new ImageCard("__", 506, this.getActivity());
//                card.setText("How-To 7");
//                card.setAuthor("CNR", "CNR");
//                card.setDate(creationDate);
//                card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.howto_7));
//                topic.addCard(card);
//            }
//            {
//                ImageCard card = new ImageCard("__", 507, this.getActivity());
//                card.setText("How-To 8");
//                card.setAuthor("CNR", "CNR");
//                card.setDate(creationDate);
//                card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.howto_8));
//                topic.addCard(card);
//            }
//            {
//                ImageCard card = new ImageCard("__", 508, this.getActivity());
//                card.setText("How-To 9");
//                card.setAuthor("CNR", "CNR");
//                card.setDate(creationDate);
//                card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.howto_9));
//                topic.addCard(card);
//            }
//            {
//                ImageCard card = new ImageCard("__", 509, this.getActivity());
//                card.setText("How-To 10");
//                card.setAuthor("CNR", "CNR");
//                card.setDate(creationDate);
//                card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.howto_10));
//                topic.addCard(card);
//            }
//            {
//                ImageCard card = new ImageCard("__", 510, this.getActivity());
//                card.setText("How-To 11");
//                card.setAuthor("CNR", "CNR");
//                card.setDate(creationDate);
//                card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.howto_11));
//                topic.addCard(card);
//            }
//            list.add(topic);
//        }
//
//        // Post-event follow up
//        {
//            final Topic topic = new Topic("Post-event", 210, this.getActivity());
//            topic.setText("Post-event");
//            topic.setIsPrebuiltTopic(true);
//
//            ImageCard card = new ImageCard("__", 520, this.getActivity());
//            card.setText("Please check out what was discussed and share your thoughts with us after the event!");
//            card.setAuthor("CNR", "CNR");
//            card.setDate(creationDate);
//            card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.post_1));
//
//            topic.addCard(card);
//            list.add(topic);
//        }
//
//        // Credits
//        {
//            final Topic topic = new Topic("Credits", 230, this.getActivity());
//            topic.setText("Credits");
//            topic.setIsPrebuiltTopic(true);
//
//            ImageCard card = new ImageCard("__", 530, this.getActivity());
//            card.setText("Build by CNR Pisa, Futurice Berlin and Spacetime Networks Helsinki");
//            card.setAuthor("CNR", "CNR");
//            card.setDate(creationDate);
//            card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.credits_1));
//
//            topic.addCard(card);
//            list.add(topic);
//        }

        // Food
        {
            final Topic topic = new Topic("Food", 240, this.getActivity());
            topic.setText("Check what's on FutuCafé table");
            topic.setIsPrebuiltTopic(true);

            ImageCard card = new ImageCard("__", 540, this.getActivity());
            card.setText("Check what's on FutuCafé table");
            card.setAuthor("Futu2", "Futu2");
            card.setDate(creationDate);
            card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.card_food));

            topic.addCard(card);
            list.add(topic);
        }

        // Light
        {
            final Topic topic = new Topic("Light", 250, this.getActivity());
            topic.setText("Lightest place");
            topic.setIsPrebuiltTopic(true);

            ImageCard card = new ImageCard("__", 550, this.getActivity());
            card.setText("Lightest place");
            card.setAuthor("Futu2", "Futu2");
            card.setDate(creationDate);
            card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.card_lightest_place));

            topic.addCard(card);
            list.add(topic);
        }

        // Silence
        {
            final Topic topic = new Topic("Silence", 260, this.getActivity());
            topic.setText("Find out latest quiet spot");
            topic.setIsPrebuiltTopic(true);

            ImageCard card = new ImageCard("__", 560, this.getActivity());
            card.setText("Find out latest quiet spot");
            card.setAuthor("Futu2", "Futu2");
            card.setDate(creationDate);
            card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.card_quiet_spot));

            topic.addCard(card);
            list.add(topic);
        }

        // Last person
        {
            final Topic topic = new Topic("Last person", 270, this.getActivity());
            topic.setText("You're the last person in the office");
            topic.setIsPrebuiltTopic(true);

            ImageCard card = new ImageCard("__", 570, this.getActivity());
            card.setText("You're the last person in the office");
            card.setAuthor("Futu2", "Futu2");
            card.setDate(creationDate);
            card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.card_last_person));

            topic.addCard(card);
            list.add(topic);
        }

        // Workspace
        {
            final Topic topic = new Topic("Workspace", 280, this.getActivity());
            topic.setText("One of your workspaces is now free");
            topic.setIsPrebuiltTopic(true);

            ImageCard card = new ImageCard("__", 580, this.getActivity());
            card.setText("One of your workspaces is now free");
            card.setAuthor("Futu2", "Futu2");
            card.setDate(creationDate);
            card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.card_workspace));

            topic.addCard(card);
            list.add(topic);
        }

        return list;
    }

    /**
     * Receiving a picture card
     *
     * @param pictureCardVO
     */
    private void gotPictureCard(@NonNull final PictureCardVO pictureCardVO) {
        RCLog.d(this, "Got picture card, id = " + pictureCardVO.uid);
        final IAltFuture<?, ?> altFuture = UI.then(() -> {
            RCLog.v(this, "Processing picture message on UI thread, id = " + pictureCardVO.uid);
            final String cardName = pictureCardVO.getCardName();
            RCLog.v(this, "Picture message cardName=" + cardName + " for id = " + pictureCardVO.uid);
            ITopic topic = null;

            // Try to find existing topic.
            for (final ITopic existingTopic : getSourceTopicModel()) {
                if (existingTopic.getName().equals(pictureCardVO.topic)) {
                    // Found existing topic for this message.
                    RCLog.v(this, "Found existing topic for picture message cardName=" + cardName + " for id = " + pictureCardVO.uid);
                    topic = existingTopic;

                    // See if there's an existing card for this message already
                    for (final ICard card : existingTopic.getCards()) {
                        if (card.getName().equals(cardName)) {
                            // Card for this video already exists.
                            RCLog.v(this, "Found existing card for picture message, so will not create a new one. cardName=" + cardName + " for id = " + pictureCardVO.uid);
                            return;
                        }
                    }
                    break;
                }
            }

            // Create topic if one doesn't exist
            if (topic == null) {
                RCLog.d(this, "Creating new topic " + pictureCardVO.topic + " for picture message cardName=" + cardName + " for id = " + pictureCardVO.uid);
                topic = this.addTrendingTopic(pictureCardVO.topic);
            }

            // Create card
            ImageCard card = new ImageCard(cardName, pictureCardVO.uid, this.getActivity());
            card.setImageUri(Uri.fromFile(pictureCardVO.pictureFile));
            card.setText(pictureCardVO.title);
            card.setAuthor(pictureCardVO.author, pictureCardVO.authorId);
            card.setDate(new Date(pictureCardVO.creationTime));
            card.setFlagged(pictureCardVO.flagged);

            // Add card to the topic
            RCLog.v(this, "Adding card " + card.getName() + " to topic " + pictureCardVO.topic + " for picture message cardName=" + cardName + " for id = " + pictureCardVO.uid + ", finally with message " + card.getText());
            topic.addCard(card);
            topic.setLikes(modelSingleton.getLikes(topic));

            // Update the display
            filterModel();
        });
    }

    /**
     * Add a topic to "trending"
     *
     * @param topic
     * @return
     */
    @NonNull

    private ITopic addTrendingTopic(@NonNull final String topic) {
        final Topic simpleTopic = new Topic(topic, this.getActivity());

        simpleTopic.setText(topic);
        getSourceTopicModel().add(simpleTopic);

        return simpleTopic;
    }

    /**
     * Receive a video message
     *
     * @param videoCard
     */
    private void gotVideoMessage(@NonNull final VideoCardVO videoCard) {
        RCLog.d(this, "Got video message, id = " + videoCard.uid);

        UI.execute(() -> {
            RCLog.v(this, "Processing video message on UI thread, id = " + videoCard.uid);
            final String cardName = videoCard.getCardName();
            RCLog.v(this, "Video message cardName=" + cardName + " for id = " + videoCard.uid);
            ITopic topic = null;

            // Try to find existing topic.
            for (final ITopic existingTopic : getSourceTopicModel()) {
                if (existingTopic.getName().equals(videoCard.topic)) {
                    // Found existing topic for this message.
                    RCLog.v(this, "Found existing topic for video message cardName=" + cardName + " for id = " + videoCard.uid);
                    topic = existingTopic;

                    // See if there's an existing card for this message already
                    for (final ICard card : existingTopic.getCards()) {
                        if (card.getName().equals(cardName)) {
                            // Card for this video already exists.
                            RCLog.v(this, "Found existing card for video message, so will not create a new one. cardName=" + cardName + " for id = " + videoCard.uid);
                            return;
                        }
                    }
                }
            }

            // Create topic if one doesn't exist
            if (topic == null) {
                RCLog.v(this, "Creating new topic " + videoCard.topic + " for video message cardName=" + cardName + " for id = " + videoCard.uid);
                topic = this.addTrendingTopic(videoCard.topic);
            }

            // Create card
            final VideoCard card = new VideoCard(cardName, videoCard.topicUid, this.getActivity());
            card.setText(videoCard.title);
            card.setVideoCardVO(videoCard);
            card.setFlagged(videoCard.flagged);
            card.setAuthor(videoCard.author, videoCard.authorId);

            // Add card to the topic
            RCLog.v(this, "Adding card " + card.getName() + " to topic " + videoCard.topic + " for video message cardName=" + cardName + " for id = " + videoCard.uid);

            topic.addCard(card);
            topic.setLikes(modelSingleton.getLikes(topic));

            // Update the display
            filterModel();
        });
    }

    private void updateTopicLikes(@NonNull final long[] uids) {
        for (final ITopic topic : getSourceTopicModel()) {
            boolean containsCard = false;

            for (final ICard card : topic.getCards()) {
                if (ArrayUtils.valueExists(uids, card.getUid())) {
                    containsCard = true;
                    break;
                }
            }

            if (containsCard) {
                topic.setLikes(modelSingleton.getLikes(topic));
            }
        }

        filterModel();
    }

    private void removeCards(@NonNull final long[] uids) {
        for (ITopic topic : getSourceTopicModel()) {
            for (ICard card : topic.getCards()) {
                if (ArrayUtils.valueExists(uids, card.getUid())) {
                    topic.removeCard(card);
                }
            }
        }

        filterModel(); // Update the filtering
    }

    private void connectScampiServices() {
        modelSingleton.addPictureCardListener(pictureCardReceivedListener);
        modelSingleton.addVideoCardListener(videoCardReceivedListener);
    }

    private void disconnectScampiServices() {
        modelSingleton.removePictureCardListener(pictureCardReceivedListener);
        modelSingleton.removeVideoCardListener(videoCardReceivedListener);
    }
}


