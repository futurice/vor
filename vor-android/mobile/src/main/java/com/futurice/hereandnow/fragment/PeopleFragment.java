package com.futurice.hereandnow.fragment;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.futurice.hereandnow.HereAndNowApplication;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.activity.SettingsActivity;
import com.futurice.hereandnow.adapter.PeopleNearbyAdapter;
import com.futurice.hereandnow.pojo.PersonNearby;
import com.futurice.hereandnow.utils.BeaconLocationManager;
import com.futurice.hereandnow.utils.HereAndNowUtils;
import com.futurice.hereandnow.utils.PeopleManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import static com.futurice.cascade.Async.UI;

public class PeopleFragment extends BaseHereAndNowFragment {

    private RecyclerView mRecyclerView;

    PeopleManager peopleManager;
    SharedPreferences preferences;

    private float ownX, ownY;

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

        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        peopleManager = new PeopleManager();

        // Set an empty adapter for the layout.
        mRecyclerView.setAdapter(new PeopleNearbyAdapter(new ArrayList<PersonNearby>()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        HereAndNowApplication.getBeaconLocationManager().setOnLocationUpdateListener(new BeaconLocationManager.OnLocationUpdateListener() {
            @Override
            public void onLocationUpdate(String position) {
                try {
                    JSONObject jsonObject = new JSONObject(position);
                    String email = jsonObject.getString("email");

                    if (!peopleManager.exists(email) &&
                            !email.equals(preferences.getString(SettingsActivity.EMAIL_KEY, ""))) {
                        peopleManager.addPerson(email);
                    }

                    if (email.equals((preferences.getString(SettingsActivity.EMAIL_KEY, "")))) {
                        ownX = Float.valueOf(jsonObject.getString("x"));
                        ownY = Float.valueOf(jsonObject.getString("y"));
                    } else {
                        PeopleManager.Person selectedPerson = peopleManager.getPerson(email);
                        selectedPerson.setLocation(Float.valueOf(jsonObject.getString("x")),
                                Float.valueOf(jsonObject.getString("y")), true);
                    }

                    updateNameList();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConnectionError() {
                UI.execute(() -> {
                    Toast.makeText(getContext(), R.string.error_connect, Toast.LENGTH_SHORT).show();
                });
            }

            private void updateNameList() {
                // Invalid values for the client's own location so do not update the view.
                if (ownX <= 0 || ownY <= 0) {
                    return;
                }

                ArrayList<PersonNearby> listValues = new ArrayList<>();

                for (PeopleManager.Person person : peopleManager.getPeople()) {
                    float personX = person.getMapLocationX();
                    float personY = person.getMapLocationY();

                    double distance = Math.sqrt(Math.pow(personX - ownX, 2) + Math.pow(personY - ownY, 2));
                    PersonNearby personNearby = new PersonNearby(HereAndNowUtils.getName(person.getEmail()),
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
        });
    }
}
