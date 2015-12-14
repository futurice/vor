package com.futurice.hereandnow.fragment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;

import com.futurice.cascade.util.AssertUtil;
import com.futurice.cascade.util.RCLog;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.card.ITopic;
import com.futurice.hereandnow.card.PeerProfileCard;
import com.futurice.hereandnow.card.Topic;
import com.futurice.hereandnow.i.ScampiDataChangeListener;
import com.futurice.hereandnow.utils.HereAndNowUtils;
import com.futurice.scampiclient.ScampiPeerDiscoveryService;
import com.futurice.scampiclient.items.Peer;

import java.util.ArrayList;
import java.util.List;

import static com.futurice.cascade.Async.UI;

public class PeopleFragment extends BaseHereAndNowFragment {

    private int lastExpanded = -1;
    private boolean collapse = true;
    @NonNull
    private ScampiDataChangeListener<Peer> peerReceivedListener = new ScampiDataChangeListener<Peer>() {
        @Override
        public void onItemAdded(@NonNull final Peer peer) {
            gotPeerDiscovery(peer);
        }

        @Override
        public void onItemsUpdated(@NonNull final long[] uids) {
            // Peers arent' updated
        }

        @Override
        public void onItemsRemoved(@NonNull final long[] uidArray) {
            // Peer deletions are still based on separate topic timestamps
        }
    };

    public static PeopleFragment newInstance() {
        final PeopleFragment fragment = new PeopleFragment();
        final Bundle b = new Bundle();
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    @NonNull

    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View rootView = inflater.inflate(R.layout.fragment_people_nearby, container, false);
        initViewsAndAdapters();

        final FrameLayout messageListFrameLayout = (FrameLayout) rootView.findViewById(
                R.id.here_and_now_message_list);
        messageListFrameLayout.addView(AssertUtil.assertNotNull(getExpandableListView()));

        connectScampiServices();
        initListView();

        modelSingleton.notifyAllPeers(peerReceivedListener);
        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disconnectScampiServices();
    }

    private void initViewsAndAdapters() {
        // Set ExpandableListView values
        final ExpandableListView elv = new ExpandableListView(getActivity());
        setExpandableListView(elv);
        elv.setOnGroupExpandListener(topicIndex -> {
            if (lastExpanded != -1 && topicIndex != lastExpanded) {
                elv.collapseGroup(lastExpanded);
            }
            lastExpanded = topicIndex;
        });
//        setTopicListAdapter(new TopicListAdapter(getSourceTopicModel(),
//                "PeopleNearbyExpandableListAdapter",
//                ViewUtils.getColor(getActivity(), Constants.LISTVIEWMODE_PEOPLE_NEARBY),
//                ViewUtils.getHighlightColor(getActivity(), Constants.LISTVIEWMODE_PEOPLE_NEARBY)));
    }

    /**
     * Receive a peer notification from the network
     *
     * @param peer
     */
    private void gotPeerDiscovery(@NonNull final Peer peer) {
        RCLog.d(this, "Found peer: " + peer);

        // Filter out local user (based on aboutMe). TODO: anonymous?
        //Why based on aboutMe instead of Tag?
//        if (peer.aboutMe.equals(serviceSingleton.myAboutMe.get())) {
//            return;
//        }

        // Topic for the peer
        UI.execute(() -> {
            Topic topic = null;
            PeerProfileCard profileCard = null;
            boolean addToModel = false;
            final List<ITopic> removeMeTopics = new ArrayList<>();

            // Check if the peer already exists in the topics for "tribe" model
            // TODO: Set "freshness" for the entry so it doesn't get timed out
            for (final ITopic existingTopic : getSourceTopicModel()) {
                if (existingTopic.getName().equals(peer.idTag)) {
                    ((Topic) existingTopic).setTimestamp(System.currentTimeMillis());
                    topic = (Topic) existingTopic;
                    // Assume there is a profile card
                    // TODO: Be smarter for production.
                    profileCard = (PeerProfileCard) topic.getCards().get(0);
                } else if (isTopicOutdated((Topic) existingTopic)) {
                    removeMeTopics.add(existingTopic);
                }
            }

            getSourceTopicModel().removeAll(removeMeTopics);

            // Create a new topic if necessary
            if (topic == null) {
                // New topic
                final String idTagValue = peer.idTag.get();

                topic = new Topic(idTagValue, this.getActivity());
                topic.setTimestamp(peer.timestamp);
                topic.setText(idTagValue);
                topic.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.group_small_icon)); //TODO Different icons based on tag match- in tribe looks better

                // New profile card
                profileCard = new PeerProfileCard(peer.aboutMe + "-profile", this.getActivity());
                addToModel = true;
            }
            topic.setText(peer.tag.get());
            // Set the profile data
            profileCard.setPeerAboutMe(peer.aboutMe.get());
            profileCard.setPeerTag(peer.tag.get());
            profileCard.setPeerIdTag(peer.idTag.get());

            // Update view
            // XXX: Do these need to be done from the main thread? Doing for safety.
            // I'm told yes, everything UI from main thread or odd stuff happens
            final Topic topicF = topic;
            final boolean addToModelF = addToModel;
            final PeerProfileCard cardF = profileCard;

            // Update view model
            if (addToModelF) {
                topicF.addCard(cardF);
                getSourceTopicModel().add(topicF);
            }

            filterModel();

//            getExpandableListView().invalidate();
        });

        RCLog.d(this, "Tribe model has " + getSourceTopicModel().size() + " entries.");
    }

    private void connectScampiServices() {
        modelSingleton.addPeerListener(peerReceivedListener);
    }

    private void disconnectScampiServices() {
        modelSingleton.removePeerListener(peerReceivedListener);
    }

    public void collapseLast() {
        if (collapse) {
            getExpandableListView().collapseGroup(lastExpanded);
        } else {
            collapse = true;
        }
    }

    public boolean isTopicOutdated(Topic topic) {
        long timeSec = (System.currentTimeMillis() - topic.getTimestamp()) / 1000;
        return timeSec > ScampiPeerDiscoveryService.MESSAGE_LIFETIME_SECONDS;
    }
}
