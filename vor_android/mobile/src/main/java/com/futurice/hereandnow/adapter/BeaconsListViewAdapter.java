package com.futurice.hereandnow.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.futurice.hereandnow.R;

import java.util.List;

/**
 * Created by Luís Ramalho on 03/11/15.
 * <luis.ramalho@futurice.com>
 */
public class BeaconsListViewAdapter extends ArrayAdapter<Beacon> {

    public BeaconsListViewAdapter(@NonNull final Context context,
                                  @NonNull final List<Beacon> beacons) {
        super(context, R.layout.listview_beacon, beacons);
    }

    @Override
    public View getView(final int position,
                        @Nullable View convertView,
                        @NonNull final ViewGroup parent) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            // inflate the GridView item layout
            final LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.listview_beacon, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            viewHolder.tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        final Beacon beacon = getItem(position);
//        viewHolder.ivIcon.setImageDrawable(beacon.icon);
        viewHolder.tvTitle.setText(R.string.beacons_listviewadapter_beacons_found);
        viewHolder.tvDescription.setText(beacon.getProximityUUID().toString());

        return convertView;
    }

    /**
     * The view holder design pattern prevents using findViewById()
     * repeatedly in the getView() method of the adapter.
     *
     * @see http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
     */
    private static class ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvDescription;
    }
}