package com.futurice.hereandnow.fragment;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ExpandableListView;

import com.futurice.cascade.functional.ImmutableValue;
import com.futurice.cascade.i.IAsyncOrigin;
import com.futurice.cascade.util.AssertUtil;
import com.futurice.cascade.util.RCLog;
import com.futurice.hereandnow.activity.DrawerActivity;
import com.futurice.hereandnow.adapter.TopicListAdapter;
import com.futurice.hereandnow.card.ITopic;
import com.futurice.hereandnow.singleton.ModelSingleton;
import com.futurice.hereandnow.singleton.ServiceSingleton;

import java.util.ArrayList;
import java.util.List;

/**
 * Base Fragment
 * <p>
 * Created by pper on 16/04/15.
 */
public class BaseHereAndNowFragment extends Fragment implements TextWatcher, IAsyncOrigin {

    protected static final ServiceSingleton serviceSingleton = ServiceSingleton.instance();
    @NonNull
    protected static final ModelSingleton modelSingleton = ModelSingleton.instance();
    private final List<ITopic> sourceTopicModel = new ArrayList<>();
    @Nullable
    private ExpandableListView expandableListView;
    @Nullable
    private TopicListAdapter topicListAdapter; // View Model, sorted and filtered from the Model
    @Nullable
    private CharSequence searchString;
    private final ImmutableValue<String> origin = RCLog.originAsync();

    protected void initListView() {
        RCLog.d(this, "Init HereAndNowFragment list");
        final ExpandableListView lv = AssertUtil.assertNotNull(expandableListView);

        lv.setAdapter(topicListAdapter);
        lv.setGroupIndicator(null);
        lv.setDividerHeight(0);
        registerForContextMenu(lv);
    }

    @Nullable
    public TopicListAdapter getTopicListAdapter() {
        return topicListAdapter;
    }

    public void setTopicListAdapter(@NonNull final TopicListAdapter topicListAdapter) {
        this.topicListAdapter = topicListAdapter;
    }

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        filterModel();
    }

    @Nullable
    public ExpandableListView getExpandableListView() {
        return expandableListView;
    }

    public void setExpandableListView(@NonNull ExpandableListView expandableListView) {
        this.expandableListView = expandableListView;
    }

    protected List<ITopic> getSourceTopicModel() {
        return sourceTopicModel;
    }

    protected void initTopicsAndCards(
            @NonNull final List<ITopic> preBuiltTopics,
            @NonNull final List<ITopic> topicModel,
            @NonNull final TopicListAdapter adapter) {
        topicModel.addAll(preBuiltTopics);
        adapter.notifyDataSetChanged();
    }

    protected void filterModel() {
        if (topicListAdapter != null) {
            topicListAdapter.getFilter().filter(searchString);
        }
    }

    @Override
    public void onTextChanged(
            @NonNull final CharSequence sequence,
            final int start,
            final int before,
            final int count) {
        searchString = sequence;
        filterModel();
    }

    @Override
    public void beforeTextChanged(
            @NonNull final CharSequence s,
            final int start,
            final int count,
            final int after) {
    }

    @Override
    public void afterTextChanged(@NonNull final Editable s) {
    }

    @NonNull
    @Override
    public ImmutableValue<String> getOrigin() {
        return origin;
    }
}
