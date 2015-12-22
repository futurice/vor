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
import com.futurice.hereandnow.utils.Storing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;

import static com.futurice.cascade.Async.UI;

public class CardsNowFragment extends BaseHereAndNowFragment {
    private static final String TAG = CardsNowFragment.class.getSimpleName();

    Socket mSocket;

    SharedPreferences mTestSP;
    SharedPreferences mFoodSP;
    SharedPreferences mPoolSP;

    OnSharedPreferenceChangeListener mTestCardListener = this::addTestCard;
    OnSharedPreferenceChangeListener mFoodCardListener = this::addFoodCard;
    OnSharedPreferenceChangeListener mPoolCardListener = this::addPoolCard;

    public CardsNowFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSocket = HereAndNowApplication.getSocket();
        mSocket.emit(Constants.EVENT_INIT); // Requests latest messages

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
                    case Constants.TOILET_KEY:
                        Storing.saveToiletToSharedPreferences(
                                jsonObject,
                                HereAndNowApplication.getStaticContext());
                        break;
                    case Constants.POOL_KEY:
                        String poolImage = jsonObject.getString(Constants.IMAGE_KEY);
                        getSourceTopicModel().add(Cards.pool(poolImage, context));
                        break;
                    case Constants.FOOD_KEY:
                        String foodImage = jsonObject.getString(Constants.IMAGE_KEY);
                        getSourceTopicModel().add(0, Cards.food(foodImage, context));
                        break;
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
        final List<ITopic> list = new ArrayList<>();
        list.add(Cards.workspace("?", getActivity()));
        list.add(Cards.sauna("ON", getActivity()));
        list.add(Cards.trackItem("Device B78", getActivity()));
        return list;
    }

    /**
     * Adds the test card to the view
     *
     * @param sharedPreferences the shared preferences
     * @param key the key
     */
    private void addTestCard(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Constants.MESSAGE_KEY:
                String message = sharedPreferences.getString(key, "Failed");
                getSourceTopicModel().add(0, Cards.test(message, this.getActivity()));
                UI.execute(this::filterModel);
                break;
            default:
                break;
        }
    }

    /**
     * Adds the pool card to the view
     *
     * @param sharedPreferences the shared preferences
     * @param key the key
     */
    private void addPoolCard(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Constants.IMAGE_KEY:
                List<ITopic> cards = getSourceTopicModel();
                String file = sharedPreferences.getString(key, "Failed");
                cards.add(0, Cards.pool(file, getActivity()));
                removeDuplicates(Constants.POOL_KEY, cards);
                UI.execute(this::filterModel);
                break;
            default:
                break;
        }
    }


    /**
     * Adds the food card to the view
     *
     * @param sharedPreferences the shared preferences
     * @param key the key
     */
    private void addFoodCard(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Constants.IMAGE_KEY:
                List<ITopic> cards = getSourceTopicModel();
                String file = sharedPreferences.getString(key, "Failed");
                cards.add(0, Cards.food(file, this.getActivity()));
                removeDuplicates(Constants.FOOD_KEY, cards);
                UI.execute(this::filterModel);
                break;
            default:
                break;
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
