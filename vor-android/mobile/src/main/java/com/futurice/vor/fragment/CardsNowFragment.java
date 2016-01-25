package com.futurice.vor.fragment;

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
import android.widget.RelativeLayout;

import com.futurice.cascade.i.CallOrigin;
import com.futurice.vor.Cards;
import com.futurice.vor.R;
import com.futurice.vor.VorApplication;
import com.futurice.vor.adapter.TopicListAdapter;
import com.futurice.vor.card.ITopic;
import com.futurice.vor.utils.SharedPreferencesManager;
import com.futurice.vor.utils.BeaconLocationManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;

import static com.futurice.cascade.Async.UI;
import static com.futurice.vor.Constants.*;

public class CardsNowFragment extends BaseVorFragment {
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

        mSocket = VorApplication.getSocket();

        mTestSP = getActivity().getSharedPreferences(TEST_KEY, Context.MODE_PRIVATE);
        mFoodSP = getActivity().getSharedPreferences(FOOD_KEY, Context.MODE_PRIVATE);
        mPoolSP = getActivity().getSharedPreferences(POOL_KEY, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cards_now, container, false);

        initViewsAndAdapters();
        initListView();

        RelativeLayout emptyView = (RelativeLayout) inflater.inflate(R.layout.no_cards_view, null);

        ExpandableListView expandableListView = getExpandableListView();
        setEmptyExpandableListView(getActivity(), emptyView);

        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.now_cards_list);
        frameLayout.addView(expandableListView);

        mSocket.on(EVENT_INIT, args -> {
            UI.execute(() -> {
                JSONObject jsonObject = (JSONObject) args[0];

                try {
                    JSONArray jsonArray = jsonObject.getJSONArray(INIT_BEACONS_KEY);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject beacon = (JSONObject) jsonArray.get(i);
                        String identifier = beacon.getString(ID_KEY);
                        int floor = beacon.getInt(FLOOR_KEY);
                        float x = (float) beacon.getDouble(LOCATION_X_KEY);
                        float y = (float) beacon.getDouble(LOCATION_Y_KEY);

                        BeaconLocationManager bm = VorApplication.getBeaconLocationManager();
                        bm.addBeacon(identifier, floor, x, y, getContext());
                    }

                    // Start the manager.
                    VorApplication.getBeaconLocationManager().resume();

                    // Initialize cards.
                    JSONArray cardsArray = jsonObject.getJSONArray(INIT_MESSAGES_KEY);
                    setupInitialView(cardsArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        });

        mSocket.emit(EVENT_INIT); // Requests latest messages

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
                Context context = VorApplication.getStaticContext();
                List<ITopic> cards = getSourceTopicModel();
                switch (jsonObject.getString(TYPE_KEY)) {
                    case TOILET_KEY:
                        SharedPreferencesManager.saveToSharedPreferences(jsonObject, context);
                        break;
                    case POOL_KEY:
                        String poolImage = jsonObject.getString(IMAGE_KEY);
                        cards.add(0, Cards.pool(poolImage, context));
                        removeDuplicates(POOL_KEY, cards);
                        break;
                    case FOOD_KEY:
                        String foodImage = jsonObject.getString(IMAGE_KEY);
                        cards.add(0, Cards.food(foodImage, context));
                        removeDuplicates(FOOD_KEY, cards);
                        break;
                    case TEST_KEY:
                        String message = jsonObject.getString(MESSAGE_KEY);
                        cards.add(0, Cards.test(message, context));
                        break;
                    case SAUNA_KEY:
                        String status = jsonObject.getString(STATUS_KEY);
                        cards.add(0, Cards.sauna(status, context));
                        removeDuplicates(SAUNA_KEY, cards);
                        break;
                    case TRACK_ITEM_KEY:
                        String item = jsonObject.getString(ITEM_KEY);
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
//        list.add(Cards.printer3d(getActivity()));
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
                if (jsonData.has(IMAGE_KEY)) {
                    String image = jsonData.getString(IMAGE_KEY);
                    List<ITopic> cards = getSourceTopicModel();
                    if (key.equals(POOL_KEY)) {
                        cards.add(0, Cards.pool(image, getActivity()));
                        removeDuplicates(POOL_KEY, cards);
                    } else if (key.equals(FOOD_KEY)) {
                        cards.add(0, Cards.food(image, getActivity()));
                        removeDuplicates(FOOD_KEY, cards);
                    }
                } else if (jsonData.has(MESSAGE_KEY)) {
                    String message = jsonData.getString(MESSAGE_KEY);
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
