package com.futurice.hereandnow.fragment;

import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.futurice.hereandnow.R;
import com.futurice.hereandnow.adapter.PeopleNearbyAdapter;
import com.futurice.hereandnow.pojo.PersonNearby;

import java.util.ArrayList;

public class PeopleFragment extends BaseHereAndNowFragment {

    private RecyclerView mRecyclerView;

    public static PeopleFragment newInstance() {
        final PeopleFragment fragment = new PeopleFragment();
        final Bundle b = new Bundle();
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    @NonNull
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_people_nearby, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.people_recycler_view);

        ArrayList<PersonNearby> peopleNearby = new ArrayList<>();
        peopleNearby.add(new PersonNearby("Ville Tainio", 2));
        peopleNearby.add(new PersonNearby("Mika Nevalainen", 3));
        peopleNearby.add(new PersonNearby("Liva Kallite", 4));
        peopleNearby.add(new PersonNearby("Paul Houghton", 50));
        mRecyclerView.setAdapter(new PeopleNearbyAdapter(peopleNearby));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return v;
    }
}
