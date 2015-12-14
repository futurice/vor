package com.futurice.scampiclient.items;


// TODO: extract common functionality from cards here
public class ScampiCard extends ScampiItem {
    public boolean flagged = false;
    /** Event ID in which this card was created. */
    public final String eventId;

    protected ScampiCard(final String eventId) {
        this.eventId = eventId;
    }
}
