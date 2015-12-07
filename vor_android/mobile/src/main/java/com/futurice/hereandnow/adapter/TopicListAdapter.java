package com.futurice.hereandnow.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.futurice.cascade.functional.ImmutableValue;
import com.futurice.cascade.i.IAsyncOrigin;
import com.futurice.cascade.i.INamed;
import com.futurice.cascade.util.RCLog;
import com.futurice.hereandnow.card.ICard;
import com.futurice.hereandnow.card.ITopic;
import com.futurice.hereandnow.card.Topic;
import com.futurice.hereandnow.singleton.ModelSingleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Adapter for a list view displaying Topics and Cards.
 *
 * @author teemuk
 */
public class TopicListAdapter extends BaseExpandableListAdapter implements INamed, Filterable, IAsyncOrigin {
    // ListView needs to know how many different types of children (=Cards) there are.
    // This must be given statically, and each different child has to have a different type.
    private static final int CHILD_TYPE_COUNT = 5;
    @NonNull
    private final List<ITopic> topicModel = new ArrayList<>();
    @NonNull
    private final List<ITopic> sourceTopicModel;
    @NonNull
    private final ImmutableValue<String> origin = RCLog.originAsync();
    @NonNull
    private final String name;
    @Nullable
    private Comparator<ITopic> topicComparator;
    @Nullable
    private SimpleFilter topicFilter;
    private int lastExpanded = 0;

    public TopicListAdapter(@NonNull final List<ITopic> sourceTopicModel,
                            @NonNull final String name) {
        this.sourceTopicModel = sourceTopicModel;
        this.name = name;
    }

    @Override
    @NonNull
    public View getGroupView(
            final int topicIndex,
            final boolean isExpanded,
            @Nullable final View convertView,
            @NonNull final ViewGroup parentView) {
        final ITopic topic = this.getTopic(topicIndex);
        final View topicView;



        if (convertView != null) {
            topic.updateView(convertView, isExpanded);
            topicView = convertView;
        } else {
            topicView = topic.getView(parentView, isExpanded);
        }

        return topicView;
    }

    @NonNull
    public ITopic getTopic(final int topicIndex) {
        return topicModel.get(topicIndex);
    }

    @NonNull
    public ICard getCard(final int topicIndex, final int cardIndexWithinTopic) {
        return getTopic(topicIndex).getCards().get(cardIndexWithinTopic);
    }

    /**
     * Sets a comparator for sorting the list contents.
     *
     * @param comparator
     */
    public void setSortFunction(@Nullable final Comparator<ITopic> comparator) {
        this.topicComparator = comparator;
    }

    /**
     * Adds a filter function, in addition to the default search box filtering.
     *
     * @param filter
     */
    public void setFilterFunction(@Nullable final SimpleFilter filter) {
        this.topicFilter = filter;
    }

    public void updateSorting() {
        if (this.topicComparator != null) {
            Collections.sort(this.topicModel, this.topicComparator);
            notifyDataSetChanged();
        }
    }

    // This Function used to inflate Card row views
    @Override
    @NonNull
    public View getChildView(
            final int topicIndex,
            final int cardIndexWithinTopic,
            final boolean isLastChild,
            @Nullable final View convertView,
            @NonNull ViewGroup parentView) {
        final ICard card = getCard(topicIndex, cardIndexWithinTopic);

        final View topicView;
        if (convertView != null) {
            RCLog.v(this, "View is being recycled: " + convertView);
            card.updateView(convertView);
            topicView = convertView;
        } else {
            RCLog.v(this, "New View is being created");
            topicView = card.getView(parentView);
        }

        return topicView;
    }

    @Override
    @NonNull
    public Object getChild(final int groupPosition, final int childPosition) {
        return topicModel.get(groupPosition).getCards().get(childPosition);
    }

    //Call when child row clicked
    @Override
    public long getChildId(final int groupPosition, final int childPosition) {
        return topicModel.get(groupPosition).getCards().get(childPosition).getUid();
    }

    @Override
    public int getChildrenCount(final int groupPosition) {
        return topicModel.get(groupPosition).getCards().size();
    }

    @Override
    public Object getGroup(final int groupPosition) {
        ITopic topic = topicModel.get(groupPosition);
        RCLog.d(this, "getGroup(" + groupPosition + ") will return " + topic.getName());

        return topic;
    }

    @Override
    public int getGroupCount() {
        return topicModel.size();
    }

    @Override
    public void onGroupExpanded(final int topicIndex) {
        super.onGroupExpanded(topicIndex);
        ((Topic) getTopic(lastExpanded)).collapsed();
        ((Topic) getTopic(topicIndex)).expanded();
        lastExpanded = topicIndex;
    }

    @Override
    public void onGroupCollapsed(final int topicIndex) {
        super.onGroupCollapsed(topicIndex);

        ((Topic) getTopic(topicIndex)).collapsed();
    }

    //Call when parent row clicked
    @Override
    public long getGroupId(final int groupPosition) {
        return topicModel.get(groupPosition).getUid();
    }

    @Override
    public boolean isEmpty() {
        return topicModel.isEmpty();
    }

    @Override
    public boolean isChildSelectable(final int groupPosition, final int childPosition) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    @NonNull
    public String getName() {
        return name;
    }

    @Override
    public int getChildTypeCount() {
        return CHILD_TYPE_COUNT;
    }

    @Override
    public int getChildType(final int groupPosition, final int childPosition) {
        return this.topicModel.get(groupPosition).getCards().get(childPosition).getType();
    }

    @Override
    public Filter getFilter() {
        final Filter filter = new Filter() { //TODO Redundant variable, check
            @Override
            protected FilterResults performFiltering(@NonNull final CharSequence constraint) {
                final FilterResults results = new FilterResults();
                final ArrayList<ITopic> filtered = new ArrayList<>();

                for (final ITopic topic : sourceTopicModel) {
                    if (TopicListAdapter.this.filter(topic, constraint)) {
                        filtered.add(topic);
                    }
                }

                results.values = filtered;
                results.count = filtered.size();
                return results;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(
                    @NonNull final CharSequence constraint,
                    @NonNull final FilterResults results) {

                topicModel.clear();
                topicModel.addAll((List<ITopic>) results.values);

                if (topicComparator != null) {
                    Collections.sort(topicModel, topicComparator);
                }

                notifyDataSetChanged();
            }
        };

        return filter;
    }

    public boolean filter(@NonNull final ITopic topic, @Nullable final CharSequence constraint) {
        boolean result = true;

        if (constraint != null && constraint.length() > 0) {
            result = topic.matchesSearch(constraint.toString());
        }

        // Check if all of the cards in this topic are deleted
        result = result && topic.getCards().size() > 0;

        // Check if topic matches the search string
        if (result && this.topicFilter != null) {
            result = this.topicFilter.filter(topic);
        }

        return result;
    }

    @NonNull
    @Override // IAsyncOrigin
    public ImmutableValue<String> getOrigin() {
        return origin;
    }

    public interface SimpleFilter {
        /**
         * Returns true if the topic should pass the filter, false otherwise.
         *
         * @param topic
         * @return
         */
        boolean filter(@NonNull ITopic topic);
    }
}
