package com.futurice.hereandnow.fragment;

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
import com.futurice.hereandnow.adapter.TopicListAdapter;
import com.futurice.hereandnow.card.ITopic;

import java.util.ArrayList;
import java.util.List;

/**
 * Base Fragment
 * <p>
 * Created by pper on 16/04/15.
 */
public class BaseHereAndNowFragment extends Fragment implements TextWatcher, IAsyncOrigin {

    private final List<ITopic> mSourceTopicModel = new ArrayList<>();
    private ExpandableListView mExpandablelistview;
    @Nullable
    private TopicListAdapter mTopicListAdapter; // View Model, sorted and filtered from the Model
    @Nullable
    private CharSequence mSearchString;
    private final ImmutableValue<String> mOriginAsync = RCLog.originAsync();

    protected void initListView() {
        RCLog.d(this, "Init HereAndNowFragment list");
        final ExpandableListView lv = AssertUtil.assertNotNull(mExpandablelistview);

        lv.setAdapter(mTopicListAdapter);
        lv.setGroupIndicator(null);
        lv.setDividerHeight(0);
        registerForContextMenu(lv);
    }

    @Nullable
    public TopicListAdapter getTopicListAdapter() {
        return mTopicListAdapter;
    }

    public void setTopicListAdapter(@NonNull final TopicListAdapter topicListAdapter) {
        this.mTopicListAdapter = topicListAdapter;
    }

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        filterModel();
    }

    @Nullable
    public ExpandableListView getExpandableListView() {
        return mExpandablelistview;
    }

    public void setExpandableListView(@NonNull ExpandableListView expandableListView) {
        this.mExpandablelistview = expandableListView;
    }

    protected List<ITopic> getSourceTopicModel() {
        return mSourceTopicModel;
    }

    protected void initTopicsAndCards(
            @NonNull final List<ITopic> preBuiltTopics,
            @NonNull final List<ITopic> topicModel,
            @NonNull final TopicListAdapter adapter) {
        topicModel.addAll(preBuiltTopics);
        adapter.notifyDataSetChanged();
    }

    protected void filterModel() {
        if (mTopicListAdapter != null) {
            mTopicListAdapter.getFilter().filter(mSearchString);
        }
    }

    @Override
    public void onTextChanged(
            @NonNull final CharSequence sequence,
            final int start,
            final int before,
            final int count) {
        mSearchString = sequence;
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
        return mOriginAsync;
    }
}
