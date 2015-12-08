package com.futurice.hereandnow.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.futurice.cascade.i.CallOrigin;
import com.futurice.hereandnow.Constants;
import com.futurice.hereandnow.HereAndNowApplication;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.adapter.TopicListAdapter;
import com.futurice.hereandnow.card.ITopic;
import com.futurice.hereandnow.card.ImageCard;
import com.futurice.hereandnow.card.Topic;
import com.futurice.hereandnow.utils.HereAndNowUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import io.socket.client.Socket;

public class CardsNowFragment extends BaseHereAndNowFragment {
    private static final String TAG = CardsNowFragment.class.getSimpleName();

    Socket mSocket;

    public CardsNowFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSocket = HereAndNowApplication.getSocket();
        mSocket.emit("init"); // Requests latest messages
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cards_now, container, false);

        initViewsAndAdapters();
        initListView();

        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.now_cards_list);
        frameLayout.addView(getExpandableListView());

        TextView textView = (TextView) view.findViewById(R.id.sampleInit);
        mSocket.on(Constants.EVENT_INIT, args -> {
            JSONArray jsonArray = (JSONArray) args[0];
            setupInitialView(textView, jsonArray);
        });

        initTopicsAndCards(createPreBuiltTopics(), getSourceTopicModel(), getTopicListAdapter());
        filterModel();

        return view;
    }

    protected void initViewsAndAdapters() {
        final ExpandableListView elv = new ExpandableListView(getActivity());
        setExpandableListView(elv);

        TopicListAdapter tla = new TopicListAdapter(
                getSourceTopicModel(),
                "NowCardsListAdapter");
        setTopicListAdapter(tla);
    }

    private void setupInitialView(TextView textView, JSONArray jsonArray) {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                switch (jsonObject.getString(Constants.TYPE_KEY)) {
                    case Constants.SAUNA_KEY:
                        getSourceTopicModel().add(
                                0, // adds to the beginning of list
                                saunaCard(jsonObject.getString("status")));
                        break;
                    default:
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getActivity().runOnUiThread(() -> {
            textView.setText(jsonArray.toString());
            filterModel();
        });
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
            card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.card_food));

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
     * Example pre-built card
     * @param status stauts of the sauna, ON or OFF
     * @return the card basically
     */
    private ITopic saunaCard(String status) {
        final Date creationDate = new Date();
        final Topic topic = new Topic("Sauna", 1250, this.getActivity());
        topic.setText("Sauna is " + status);
        topic.setIsPrebuiltTopic(true);

        ImageCard card = new ImageCard("__", 1550, this.getActivity());
        card.setText("Sauna is " + status);
        card.setAuthor("Futu2", "Futu2");
        card.setDate(creationDate);
        card.setImageUri(HereAndNowUtils.getResourceUri(R.drawable.card_sauna));

        topic.addCard(card);
        return topic;
    }
}
