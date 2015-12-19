package com.futurice.hereandnow.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;

import com.futurice.cascade.i.CallOrigin;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.adapter.TopicListAdapter;
import com.futurice.hereandnow.card.ITopic;
import com.futurice.hereandnow.card.ImageCard;
import com.futurice.hereandnow.card.Topic;
import com.futurice.hereandnow.utils.HereAndNowUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class TrendingFragment extends BaseHereAndNowFragment {
    protected int lastExpanded = -1;

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
        final Date creationDate = new GregorianCalendar(2015, 5, 1, 12, 0, 0).getTime();
        final List<ITopic> list = new ArrayList<>();

        // Food
        {
            final Topic topic = new Topic("Food", 240, this.getActivity());
            topic.setText("Check what's on FutuCafé table");
            topic.setIsPrebuiltTopic(true);

            ImageCard card = new ImageCard("__", 540, this.getActivity());
            card.setText("Check what's on FutuCafé table");
            card.setAuthor("Futu2", "Futu2");
            card.setDate(creationDate);
            card.setImageUri(HereAndNowUtils.getResourceUri(R.raw.card_food));

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
            card.setImageUri(HereAndNowUtils.getResourceUri(R.raw.card_lightest_place));

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
            card.setImageUri(HereAndNowUtils.getResourceUri(R.raw.card_quiet_spot));

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
            card.setImageUri(HereAndNowUtils.getResourceUri(R.raw.card_last_person));

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
            card.setImageUri(HereAndNowUtils.getResourceUri(R.raw.card_workspace));

            topic.addCard(card);
            list.add(topic);
        }

        return list;
    }
}
