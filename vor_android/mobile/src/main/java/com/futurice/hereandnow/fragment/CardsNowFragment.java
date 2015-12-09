package com.futurice.hereandnow.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;

import com.futurice.cascade.i.CallOrigin;
import com.futurice.hereandnow.Cards;
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

import static com.futurice.cascade.Async.UI;

public class CardsNowFragment extends BaseHereAndNowFragment {
    private static final String TAG = CardsNowFragment.class.getSimpleName();

    Socket mSocket;

    SharedPreferences mTestSP;

    OnSharedPreferenceChangeListener mTestCardListener = this::addTestCard;

    public CardsNowFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSocket = HereAndNowApplication.getSocket();
        mSocket.emit("init"); // Requests latest messages

        mTestSP = getActivity().getSharedPreferences("test123", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cards_now, container, false);

        initViewsAndAdapters();
        initListView();

        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.now_cards_list);
        frameLayout.addView(getExpandableListView());

        mSocket.on(Constants.EVENT_INIT, args -> {
            JSONArray jsonArray = (JSONArray) args[0];
            setupInitialView(jsonArray);
        });

        initTopicsAndCards(
                createPreBuiltTopics(),
                getSourceTopicModel(),
                getTopicListAdapter());
        filterModel();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mTestSP.registerOnSharedPreferenceChangeListener(mTestCardListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        mTestSP.unregisterOnSharedPreferenceChangeListener(mTestCardListener);
    }

    protected void initViewsAndAdapters() {
        final ExpandableListView elv = new ExpandableListView(getActivity());
        setExpandableListView(elv);

        TopicListAdapter tla = new TopicListAdapter(
                getSourceTopicModel(),
                "NowCardsListAdapter");
        setTopicListAdapter(tla);
    }

    private void setupInitialView(JSONArray jsonArray) {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Context context = HereAndNowApplication.getStaticContext();
                switch (jsonObject.getString(Constants.TYPE_KEY)) {
                    case Constants.SAUNA_KEY:
                        String status = jsonObject.getString("status");
                        getSourceTopicModel().add(0, Cards.sauna(status, context));
                        break;
                    case Constants.TRACK_ITEM_KEY:
                        String item = jsonObject.getString("item");
                        getSourceTopicModel().add(Cards.trackItem(item, context));
                        break;
                    default:
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        UI.execute(this::filterModel);
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

    private void addTestCard(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Constants.MESSAGE_KEY:
                String message = sharedPreferences.getString(key, "Failed");
                Log.d(TAG, "TEST MESSAGE: " + message);
                getSourceTopicModel().add(0, Cards.test(message, this.getActivity()));
                UI.execute(this::filterModel);
                break;
            default:
                break;
        }
    }
}
