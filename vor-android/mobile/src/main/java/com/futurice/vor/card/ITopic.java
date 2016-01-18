package com.futurice.vor.card;

import android.support.annotation.NonNull;
import android.view.*;

import com.futurice.cascade.i.*;

import java.util.*;

/**
 * Topic containing Cards.
 *
 * @author teemuk
 */
public interface ITopic extends INamed {

    /**
     * Returns a list of all cards attached to this topic.
     *
     * @return list of all cards of this topic.
     */
    @NonNull
    List<ICard> getCards();

    /**
     * Adds a card to the list of cards attached to this topic.
     *
     * @param card
     */
    void addCard(@NonNull  ICard card);

    /**
     * Removes a card from the list of cards attached to this topic.
     *
     * @param card
     */
    void removeCard(@NonNull  ICard card);

    /**
     * Retruns a newly inflated view of this topic.
     *
     * @param parentView     parent view for the view.
     * @return view of this topic
     */
    @NonNull
    View getView(@NonNull  ViewGroup parentView, boolean isExpanded);

    /**
     * Update the given view with the Topic's data. The passed view will have
     * been previously created by {@link #getView}.
     *
     * @param view           the view to update
     */
    void updateView(@NonNull  View view, boolean isExpanded);

    /**
     * Returns the UID of this topic.
     *
     * @return
     */
    long getUid();

    /**
     * Returns true when topic matches the search string, false otherwise.
     *
     * @param search
     * @return
     */
    boolean matchesSearch(@NonNull  String search);

    /**
     * Returns the comparison result between this topic and other.
     *
     * @param other
     * @return
     */
    int compare(@NonNull  ITopic other);

    /**
     * Return true for prebuilt topics, false otherwise. This allows the UI to
     * treat prebuilt topics differently from user-generated content.
     *
     * @return
     */
    boolean isPrebuiltTopic();

    /**
     * Sets the number of likes the cards in this topic have aggregated.
     *
     * @param likes
     */
    void setLikes(int likes);

    /**
     * Returns the type of this topic.
     *
     * @return the type
     */
    String getCardType();
}
