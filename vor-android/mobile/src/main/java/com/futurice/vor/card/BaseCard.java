package com.futurice.vor.card;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.reactivecascade.util.Origin;
import com.reactivecascade.util.RCLog;
import com.futurice.vor.R;

import java.util.Date;

/**
 * Base class for cards.
 *
 * @author teemuk
 */
public abstract class BaseCard extends Origin implements ICard {
    private static final String TAG = BaseCard.class.getSimpleName();

    @NonNull
    protected final Context mContext;
    protected final int mLayoutResource;

    @NonNull
    private final LayoutInflater inflater;
    @NonNull
    private final String mName;
    private final long mUid;
    @NonNull
    private boolean mFlagged = false;
    private String mAuthor;
    private String mAuthorId;
    private Date mDate;

    protected BaseCard(
            @NonNull final String name,
            final long uid,
            @NonNull final Context context,
            final int layoutResource) {
        mName = name;
        mUid = uid;
        mContext = context;
        mLayoutResource = layoutResource;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * A new card view is being created by the list model
     *
     * @param parentView
     * @return
     */
    @Override
    @NonNull

    public View getView(@NonNull final ViewGroup parentView) {
        // Inflate the new view
        final View newView = inflateView(mLayoutResource, parentView);
        updateView(newView);

        return newView;
    }

    /**
     * An existing View is being recycled by the list model
     *
     * @param view view whose contents to update
     */
    @Override
    public void updateView(@NonNull final View view) {
        RCLog.d(this, "updateView");
    }

    @NonNull
    private View inflateView(final int resource,
                               @NonNull final ViewGroup parentView) {
        return this.inflater.inflate(resource, parentView, false);
    }

    @Override // ICard
    public long getUid() {
        return mUid;
    }

    @Override // INamed
    @NonNull
    public String getName() {
        return this.mName;
    }

    @NonNull
    public String getAuthor() {
        if (mAuthor == null) {
            return mContext.getResources().getString(R.string.people_tag_anonymous);
        }

        return mAuthor;
    }
    @NonNull
    public String getAuthorId() {
        return mAuthorId;
    }

    public void setAuthor(@NonNull final String author, @NonNull final String authorId) {
        mAuthor = author;
        mAuthorId = authorId;
    }

    @NonNull
    public Date getDate() {
        if (mDate == null) {
            //FIXME set mDate=newDate() ?
            return new Date();
        }

        return mDate;
    }

    public void setDate(@NonNull final Date date) {
        mDate = date;
    }

    public boolean isFlagged() {
        return mFlagged;
    }

    public void setFlagged(final boolean flagged) {
        mFlagged = flagged;
    }
}
