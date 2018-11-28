package com.kuhrusty.micropul.model;

/**
 * Who owns a group of micropul.
 */
public enum Owner {
    Nobody(false, false), P1(true, true), P2(true, true), Both(true, false);

    /**
     * Returns true if this group is already owned by at least one player.
     */
    public boolean isOwned() {
        return owned;
    }

    /**
     * Returns true if this group might be worth points to the player who owns
     * it.
     */
    public boolean isWorthPoints() {
        return points;
    }

    Owner(boolean owned, boolean points) {
        this.owned = owned;
        this.points = points;
    }
    private final boolean owned;
    private final boolean points;
}
