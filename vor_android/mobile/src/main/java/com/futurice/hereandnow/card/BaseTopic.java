package com.futurice.hereandnow.card;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.futurice.cascade.functional.ImmutableValue;
import com.futurice.cascade.util.Origin;
import com.futurice.cascade.util.RCLog;
import com.futurice.hereandnow.R;
import com.futurice.hereandnow.activity.DrawerActivity;
import com.futurice.hereandnow.activity.NewCardActivity;
import com.futurice.hereandnow.utils.FlavorUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Each topic contains 1 or more cards which can arrive in any order and with varying lifetimes
 */
public abstract class BaseTopic extends Origin implements ITopic {
    @NonNull
    protected final Context context;
    @NonNull
    protected final List<ICard> cards = new CopyOnWriteArrayList<>();
    protected final int layoutResource;
    @NonNull
    private final LayoutInflater inflater;
    private final String name;
    private final long uid;
    @Nullable
    protected View topicButtonBar;
    private long timestamp;
    private boolean isPrebuiltTopic = false;
    private int likes = 0;

    protected BaseTopic(
            @NonNull final String name,
            final long uid,
            @NonNull final Context context,
            final int layoutResource) {
        this.name = name;
        this.uid = uid;
        this.context = context;
        this.layoutResource = layoutResource;

        this.inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean isPrebuiltTopic() {
        return isPrebuiltTopic;
    }

    public void setIsPrebuiltTopic(boolean value) {
        this.isPrebuiltTopic = value;
    }

    @NonNull

    @Override // Topic
    public List<ICard> getCards() {
        return Collections.unmodifiableList(this.cards);
    }

    public void addCard(@NonNull ICard card) {
        this.cards.add(card);
    }

    @Override
    public void removeCard(@NonNull ICard card) {
        this.cards.remove(card);
    }

    @NonNull

    @Override // Topic
    public View getView(@NonNull final ViewGroup parentView, boolean isExpanded) {
        // Inflate the new view
        final View newView = this.inflateView(this.layoutResource, parentView);
        updateView(newView, isExpanded);

        return newView;
    }

    @Override // Topic
    public void updateView(
            @NonNull final View view,
            final boolean isExpanded) {
//        final View bar = view.findViewById(R.id.topic_button_bar);
//        topicButtonBar = bar;
//        bar.setVisibility(isExpanded && contentType() != PeerProfileCard.CARD_TYPE ? View.VISIBLE : View.GONE);

        // Show a red delete button for topics with flagged cards
//        if (FlavorUtils.isSuperuserBuild && isFlagged()) {
//            view.findViewById(R.id.topic_flagged).setVisibility(View.VISIBLE);
//        } else {
//            view.findViewById(R.id.topic_flagged).setVisibility(View.GONE);
//        }

//        view.findViewById(R.id.topic_linear_layout).setBackgroundColor(highlightColor);
//        view.findViewById(R.id.topic_table_row).setBackgroundColor(highlightColor);
//        view.findViewById(R.id.topic_button_bar).setBackgroundColor(color);

//        if (likes > 0) {
//            view.findViewById(R.id.topic_likes_layout).setVisibility(View.VISIBLE);
//            ((TextView) view.findViewById(R.id.topic_like_text)).setText(Integer.toString(likes));
//        } else {
//            view.findViewById(R.id.topic_likes_layout).setVisibility(View.GONE);
//        }

//        view.findViewById(R.id.card_picture_button).setOnClickListener(
//                v -> launchNewCardActivity("image/*"));
//
//        view.findViewById(R.id.card_video_button).setOnClickListener(
//                v -> launchNewCardActivity("video/*"));
    }

    private void launchNewCardActivity(String contentType) {
        DrawerActivity activity = (DrawerActivity) context;
        Intent createCardIntent = new Intent(activity, NewCardActivity.class);
        createCardIntent.putExtra(NewCardActivity.EXTRA_TOPIC, name);
        createCardIntent.putExtra(NewCardActivity.EXTRA_TOPIC_UID, uid);
        createCardIntent.putExtra(NewCardActivity.EXTRA_CONTENT_TYPE, contentType);
        activity.startActivity(createCardIntent);
    }

    @Override // Topic
    public long getUid() {
        return this.uid;
    }

    @Override // INamed
    @NonNull

    public String getName() {
        return this.name;
    }

    @Override
    public boolean matchesSearch(@NonNull final String search) {
        if (this.name.toLowerCase().contains(search.toLowerCase())) {
            return true;
        }

        for (ICard card : this.cards) {
            if (card.matchesSearch(search)) {
                return true;
            }
        }

        return false;
    }

    public void expanded() {
        if (topicButtonBar != null) {
            topicButtonBar.setVisibility(View.VISIBLE);
        } else {
            RCLog.v(this, "No topic button bar to expand");
        }
    }

    public void collapsed() {
        if (topicButtonBar != null) {
            topicButtonBar.setVisibility(View.GONE);
        }
    }

    @NonNull

    private View inflateView(final int resource, final ViewGroup parentView) {
        return this.inflater.inflate(resource, parentView, false);
    }

    private boolean isFlagged() {
        for (ICard card : this.cards) {
            if (((BaseCard) card).isFlagged()) {
                return true;
            }
        }

        return false;
    }

    private int contentType() {
        return cards.size() > 0 ? cards.get(0).getType() : -1;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(final int likes) {
        this.likes = likes;
        // TODO Refresh the image with number of likes
        // TODO Image would need to be reactive display
    }
}
