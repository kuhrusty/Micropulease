package com.kuhrusty.micropul.model;

/**
 * The number of tiles you get to draw and/or the number of extra turns you get
 * to take as a result of playing a tile.
 */
public class TilePlayResult {
    private int tiles = 0;
    private int turns = 0;

    /**
     * Returns the number of tiles you get to draw as a result of this play.
     */
    public int getTiles() {
        return tiles;
    }

    /**
     * Returns the number of extra tiles you get as a result of this play.
     */
    public int getExtraTurns() {
        return turns;
    }

    /**
     * Adds the tiles/turns from the catalyst in this square (if any) to the
     * running total for this play.
     *
     * @param square must not be null.
     */
    public void activateCatalyst(Square square) {
        tiles += square.getTileDraws();
        turns += square.getExtraTurns();
    }

    /**
     * Returns true if getTiles() or getExtraTurns() will return non-zero.
     */
    public boolean catalystsActivated() {
        return (tiles != 0) || (turns != 0);
    }
}
