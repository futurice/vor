package com.futurice.vor.card;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.futurice.cascade.util.Origin;
import com.futurice.cascade.util.RCLog;
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
    private final List<ICard> mCards = new CopyOnWriteArrayList<>();
    private final int mLayoutResource;
    @NonNull
    private final LayoutInflater mInflater;
    private final String mName;
    private final long mUid;
    @Nullable
    protected View mTopicButtonBar;
    private long mTimestamp;
    private boolean mIsPrebuiltTopic = false;
    private int mLikes = 0;

    protected BaseTopic(
            @NonNull final String name,
            final long uid,
            @NonNull final Context context,
            final int layoutResource) {
        mName = name;
        mUid = uid;
        this.context = context;
        mLayoutResource = layoutResource;

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean isPrebuiltTopic() {
        return mIsPrebuiltTopic;
    }

    public void setIsPrebuiltTopic(boolean value) {
        mIsPrebuiltTopic = value;
    }

    @NonNull
    @Override // Topic
    public List<ICard> getCards() {
        return Collections.unmodifiableList(mCards);
    }

    public void addCard(@NonNull ICard card) {
        mCards.add(card);
    }

    @Override
    public void removeCard(@NonNull ICard card) {
        mCards.remove(card);
    }

    @NonNull

    @Override // Topic
    public View getView(@NonNull final ViewGroup parentView, boolean isExpanded) {
        // Inflate the new view
        final View newView = inflateView(mLayoutResource, parentView);
        updateView(newView, isExpanded);

        return newView;
    }

    @Override // Topic
    public long getUid() {
        return mUid;
    }

    @Override // INamed
    @NonNull
    public String getName() {
        return mName;
    }

    @Override
    public boolean matchesSearch(@NonNull final String search) {
        if (mName.toLowerCase().contains(search.toLowerCase())) {
            return true;
        }

        for (ICard card : mCards) {
            if (card.matchesSearch(search)) {
                return true;
            }
        }

        return false;
    }

    public void expanded() {
        if (mTopicButtonBar != null) {
            mTopicButtonBar.setVisibility(View.VISIBLE);
        } else {
            RCLog.v(this, "No topic button bar to expand");
        }
    }

    public void collapsed() {
        if (mTopicButtonBar != null) {
            mTopicButtonBar.setVisibility(View.GONE);
        }
    }

    @NonNull
    private View inflateView(final int resource, final ViewGroup parentView) {
        return mInflater.inflate(resource, parentView, false);
    }

    private boolean isFlagged() {
        for (ICard card : mCards) {
            if (((BaseCard) card).isFlagged()) {
                return true;
            }
        }

        return false;
    }

    private int contentType() {
        return mCards.size() > 0 ? mCards.get(0).getType() : -1;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(final long timestamp) {
        mTimestamp = timestamp;
    }

    public int getLikes() {
        return mLikes;
    }

    public void setLikes(final int likes) {
        mLikes = likes;
        // TODO Refresh the image with number of likes
        // TODO Image would need to be reactive display
    }
}
