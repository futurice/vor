package com.futurice.vor.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.futurice.vor.R;
import com.futurice.vor.pojo.PersonNearby;

import java.util.ArrayList;

/**
 * Created by Luís Ramalho on 21/12/15.
 * <luis.ramalho@futurice.com>
 */
public class PeopleNearbyAdapter extends RecyclerView.Adapter<PeopleNearbyAdapter.ViewHolder> {
    private ArrayList<PersonNearby> mPeopleNearby;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView personNameTextView;
        public TextView personDistanceTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            personNameTextView = (TextView) itemView.findViewById(R.id.personNameTextView);
            personDistanceTextView = (TextView) itemView.findViewById(R.id.personDistanceTextView);
        }
    }

    public PeopleNearbyAdapter(ArrayList<PersonNearby> peopleNearby) {
        mPeopleNearby = peopleNearby;
    }

    @Override
    public PeopleNearbyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View personView = inflater.inflate(R.layout.item_person, parent, false);
        return new ViewHolder(personView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TextView personNameTextView = holder.personNameTextView;
        TextView personDistanceTextView = holder.personDistanceTextView;

        PersonNearby personNearby = mPeopleNearby.get(position);
        personNameTextView.setText(personNearby.getName());
        String distance = String.format("%.2f", personNearby.getDistance());
        personDistanceTextView.setText(String.format("%s%s", distance, "m"));
    }

    @Override
    public int getItemCount() {
        return mPeopleNearby.size();
    }
}
