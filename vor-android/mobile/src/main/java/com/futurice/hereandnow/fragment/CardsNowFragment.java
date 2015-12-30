package com.futurice.hereandnow.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.futurice.hereandnow.utils.SharedPreferencesManager;
import com.futurice.hereandnow.utils.BeaconLocationManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;

import static com.futurice.cascade.Async.UI;

public class CardsNowFragment extends BaseHereAndNowFragment {
    Socket mSocket;

    SharedPreferences mTestSP;
    SharedPreferences mFoodSP;
    SharedPreferences mPoolSP;

    OnSharedPreferenceChangeListener mTestCardListener = this::addCard;
    OnSharedPreferenceChangeListener mFoodCardListener = this::addCard;
    OnSharedPreferenceChangeListener mPoolCardListener = this::addCard;

    public CardsNowFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSocket = HereAndNowApplication.getSocket();

        mTestSP = getActivity().getSharedPreferences(Constants.TEST_KEY, Context.MODE_PRIVATE);
        mFoodSP = getActivity().getSharedPreferences(Constants.FOOD_KEY, Context.MODE_PRIVATE);
        mPoolSP = getActivity().getSharedPreferences(Constants.POOL_KEY, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cards_now, container, false);

        initViewsAndAdapters();
        initListView();

        FrameLayout mFrameLayout = (FrameLayout) view.findViewById(R.id.now_cards_list);
        mFrameLayout.addView(getExpandableListView());

        mSocket.on(Constants.EVENT_INIT, args -> {
            UI.execute(() -> {
                JSONObject jsonObject = (JSONObject) args[0];

                try {
                    JSONArray jsonArray = jsonObject.getJSONArray(Constants.INIT_BEACONS_KEY);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject beacon = (JSONObject) jsonArray.get(i);
                        String identifier = beacon.getString(BeaconLocationManager.BEACON_KEY_ID);
                        int floor = beacon.getInt(BeaconLocationManager.BEACON_KEY_FLOOR);

                        BeaconLocationManager bm = HereAndNowApplication.getBeaconLocationManager();
                        bm.addBeacon(identifier, floor, getContext());
                    }

                    // Start the manager.
                    HereAndNowApplication.getBeaconLocationManager().resume();

                    // Initialize cards.
                    JSONArray cardsArray = jsonObject.getJSONArray(Constants.INIT_MESSAGES_KEY);
                    setupInitialView(cardsArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        });

        mSocket.emit(Constants.EVENT_INIT); // Requests latest messages

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
        mFoodSP.registerOnSharedPreferenceChangeListener(mFoodCardListener);
        mPoolSP.registerOnSharedPreferenceChangeListener(mPoolCardListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        mTestSP.unregisterOnSharedPreferenceChangeListener(mTestCardListener);
        mFoodSP.unregisterOnSharedPreferenceChangeListener(mFoodCardListener);
        mPoolSP.unregisterOnSharedPreferenceChangeListener(mPoolCardListener);
    }

    protected void initViewsAndAdapters() {
        final ExpandableListView elv = new ExpandableListView(getActivity());
        setExpandableListView(elv);

        TopicListAdapter tla = new TopicListAdapter(getSourceTopicModel(), "NowCardsListAdapter");
        setTopicListAdapter(tla);
    }

    private void setupInitialView(JSONArray jsonArray) {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Context context = HereAndNowApplication.getStaticContext();
                List<ITopic> cards = getSourceTopicModel();
                switch (jsonObject.getString(Constants.TYPE_KEY)) {
                    case Constants.TOILET_KEY:
                        SharedPreferencesManager.saveToSharedPreferences(jsonObject, context);
                        break;
                    case Constants.POOL_KEY:
                        String poolImage = jsonObject.getString(Constants.IMAGE_KEY);
                        cards.add(0, Cards.pool(poolImage, context));
                        removeDuplicates(Constants.POOL_KEY, cards);
                        break;
                    case Constants.FOOD_KEY:
                        String foodImage = jsonObject.getString(Constants.IMAGE_KEY);
                        cards.add(0, Cards.food(foodImage, context));
                        removeDuplicates(Constants.FOOD_KEY, cards);
                        break;
                    case Constants.TEST_KEY:
                        String message = jsonObject.getString(Constants.MESSAGE_KEY);
                        cards.add(0, Cards.test(message, context));
                        break;
                    case Constants.SAUNA_KEY:
                        String status = jsonObject.getString(Constants.STATUS_KEY);
                        cards.add(0, Cards.sauna(status, context));
                        removeDuplicates(Constants.SAUNA_KEY, cards);
                        break;
                    case Constants.TRACK_ITEM_KEY:
                        String item = jsonObject.getString(Constants.ITEM_KEY);
                        cards.add(0, Cards.trackItem(item, context));
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
        final List<ITopic> list = new ArrayList<>();
        return list;
    }

    /**
     * Adds the card to the view
     *
     * @param sharedPreferences the shared preferences
     * @param key the key
     */
    private void addCard(SharedPreferences sharedPreferences, String key) {
        String json = sharedPreferences.getString(key, null);
        if (json != null) {
            try {
                JSONObject jsonData = new JSONObject(json);
                if (jsonData.has(Constants.IMAGE_KEY)) {
                    String image = jsonData.getString(Constants.IMAGE_KEY);
                    List<ITopic> cards = getSourceTopicModel();
                    if (key.equals(Constants.POOL_KEY)) {
                        cards.add(0, Cards.pool(image, getActivity()));
                        removeDuplicates(Constants.POOL_KEY, cards);
                    } else if (key.equals(Constants.FOOD_KEY)) {
                        cards.add(0, Cards.food(image, getActivity()));
                        removeDuplicates(Constants.FOOD_KEY, cards);
                    }
                } else if (jsonData.has(Constants.MESSAGE_KEY)) {
                    String message = jsonData.getString(Constants.MESSAGE_KEY);
                    List<ITopic> cards = getSourceTopicModel();
                    cards.add(0, Cards.test(message, getActivity()));
                }
                UI.execute(this::filterModel);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Removes cards that have the same type as the one just added.
     *
     * @param key the type of card
     * @param topics the current list of cards
     */
    private void removeDuplicates(String key, List<ITopic> topics) {
        for (int i = topics.size() - 1; i > 0; i--) {
            ITopic topic = topics.get(i);
            if (topic.getCardType().equals(key)) {
                topics.remove(i);
            }
        }
    }
}
