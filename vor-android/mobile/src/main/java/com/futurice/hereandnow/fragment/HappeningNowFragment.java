package com.futurice.hereandnow.fragment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.futurice.hereandnow.R;
import com.futurice.hereandnow.adapter.TopicListAdapter;

// TODO common base class instead of deriving from TrendingFragment
public class HappeningNowFragment extends TrendingFragment {

    @NonNull

    public static HappeningNowFragment newInstance() {
        return new HappeningNowFragment();
    }

    @Override
    @NonNull

    protected View setupView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container) {
        return inflater.inflate(R.layout.fragment_happening_now, container, false);
    }

    @Override
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
        setTopicListAdapter(new TopicListAdapter(
                getSourceTopicModel(),
                "HappeningNowExpandableListAdapter"));
    }
}
