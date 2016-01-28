package com.futurice.vor.fragment;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.futurice.vor.R;
import com.futurice.vor.activity.PeopleMapActivity;
import com.futurice.vor.activity.SettingsActivity;
import com.futurice.vor.adapter.PeopleNearbyAdapter;
import com.futurice.vor.interfaces.FragmentLifecycle;
import com.futurice.vor.pojo.PersonNearby;
import com.futurice.vor.utils.VorUtils;
import com.futurice.vor.utils.PeopleManager;

import java.util.ArrayList;
import java.util.Collections;

import static com.futurice.cascade.Async.UI;

public class PeopleFragment extends BaseVorFragment implements FragmentLifecycle {

    private RecyclerView mRecyclerView;
    SharedPreferences preferences;

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

        // Set an empty adapter for the layout.
        mRecyclerView.setAdapter(new PeopleNearbyAdapter(new ArrayList<>()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        return v;
    }

    @Override
    public void onResumeFragment() {
        updateView();
    }

    @Override
    public void onPauseFragment() {
        //Empty function
    }

    public void updateView() {
        String userEmail = preferences.getString(SettingsActivity.EMAIL_KEY, "");
        PeopleManager.Person user = PeopleMapActivity.mPeopleManager.getPerson(userEmail);
        if (user == null) {
            return;
        }

        ArrayList<PersonNearby> listValues = new ArrayList<>();

        for (PeopleManager.Person person : PeopleMapActivity.mPeopleManager.getPeople()) {
            if (person.getEmail().equals(userEmail) || person.getFloor() != user.getFloor()) {
                continue;
            }

            final float personX = person.getMeterLocationX();
            final float personY = person.getMeterLocationY();

            final double distance = Math.sqrt(Math.pow(personX - user.getMeterLocationX(), 2)
                    + Math.pow(personY - user.getMeterLocationY(), 2));
            PersonNearby personNearby = new PersonNearby(VorUtils.getName(person.getEmail()),
                    distance);
            listValues.add(personNearby);
        }

        // Sort the values.
        Collections.sort(listValues, new PersonNearby.PersonComparator());

        // Update the view.
        UI.execute(() -> {
            mRecyclerView.invalidate();
            mRecyclerView.setAdapter(new PeopleNearbyAdapter(listValues));
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        });
    }
}
