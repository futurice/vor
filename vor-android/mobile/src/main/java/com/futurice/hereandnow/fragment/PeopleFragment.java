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
import com.futurice.hereandnow.R;

public class PeopleFragment extends BaseHereAndNowFragment {

    private int lastExpanded = -1;
    private boolean collapse = true;

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

        initListView();

        return rootView;
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
    }

    public void collapseLast() {
        if (collapse) {
            getExpandableListView().collapseGroup(lastExpanded);
        } else {
            collapse = true;
        }
    }
}
