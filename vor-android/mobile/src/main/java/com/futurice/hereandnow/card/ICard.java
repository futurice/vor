package com.futurice.hereandnow.card;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.futurice.cascade.i.INamed;

/**
 * Interface for cards.
 *
 * @author teemuk
 */
public interface ICard extends INamed {

    /**
     * Returns a newly inflated view of the card.
     *
     * @param parentView
     * @return newly created view of the card.
     */
    @NonNull
    View getView(@NonNull ViewGroup parentView);

    /**
     * Updates the view contents with the card's data. The passed view will have
     * been previously created by calling {@link #getView};
     *
     * @param view view whose contents to update
     */
    void updateView(@NonNull View view);

    /**
     * Returns a uid identifier for this card.
     *
     * @return uid identifier
     */
    long getUid();

    /**
     * Return a type code for this card. This is needed by ListView to recycle
     * views, and the type corresponds to the view type that the card uses.
     *
     * @return card type
     */
    int getType();

    /**
     * Returns true when card matches the search string, false otherwise.
     *
     * @param search
     * @return
     */
    boolean matchesSearch(@NonNull String search);
}
