package com.kuhrusty.micropul.model;

/**
 * One quarter of a Tile.
 */
public enum Square {
    /**  A black micropul. */
    Black(true, false, 0, 0),
    /**  A white micropul. */
    White(false, true, 0, 0),
    BigBlackDot(true, false, 1, 0),
    BigWhiteDot(false, true, 1, 0),
    BigBlackPlus(true, false, 0, 1),
    BigWhitePlus(false, true, 0, 1),
    SingleCatalyst(1, 0),
    DoubleCatalyst(2, 0),
    PlusCatalyst(0, 1),
    /**
     * Empty tile.
     */
    Empty(false),
    /**
     * No tile is in this space.
     */
    Null(true);

    /**
     * Returns true if this is a black micropul.
     */
    public boolean isBlack() {
        return black;
    }

    /**
     * Returns true if this is a white micropul.
     */
    public boolean isWhite() {
        return white;
    }

    /**
     * Returns true if this is a micropul.
     */
    public boolean isMicropul() {
        return black || white;
    }

    /**
     * Returns true if this is a big micropul.
     */
    public boolean isBig() {
        return big;
    }

    /**
     * Returns true if this is a catalyst.
     */
    public boolean isCatalyst() {
        return catalyst;
    }

    /**
     * Returns true if this square is blank (that is, a blank/empty corner of a
     * tile, not a square where no tile has been played).
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * Returns true if no tile has been played here.
     */
    public boolean isNull() {
        return nul;
    }

    /**
     * Returns the number of tile draws you get when this catalyst is activated,
     * or 0 if this is not a catalyst, or not a catalyst which gives tile draws.
     */
    public int getTileDraws() {
        return tiles;
    }

    /**
     * Returns the number of extra turns you get when this catalyst is
     * activated, or 0 if this is not a catalyst, or not a catalyst which gives
     * extra turns.
     */
    public int getExtraTurns() {
        return turns;
    }

    /**
     * Returns true if this square is a micropul, and the other square is a
     * micropul, and they are the same color.
     */
    public boolean micropulMatches(Square other) {
        return isMicropul() && other.isMicropul() && (black == other.black);
    }

    /**
     * Returns true if this square is a micropul, and the other square is a
     * micropul, and they are different colors.
     */
    public boolean micropulConflicts(Square other) {
        return isMicropul() && other.isMicropul() && (black != other.black);
    }

    private Square(boolean isBlack, boolean isWhite, int tiles, int turns) {
        this.black = isBlack;
        this.white = isWhite;
        this.big = (turns | tiles) != 0;
        this.catalyst = big;
        this.tiles = tiles;
        this.turns = turns;
        this.empty = false;
        this.nul = false;
    }

    private Square(int tiles, int turns) {
        this.black = false;
        this.white = false;
        this.big = false;
        this.catalyst = true;
        this.tiles = tiles;
        this.turns = turns;
        this.empty = false;
        this.nul = false;
    }

    //  creates Empty or Null.
    private Square(boolean isNull) {
        this.black = false;
        this.white = false;
        this.big = false;
        this.catalyst = false;
        this.tiles = 0;
        this.turns = 0;
        this.empty = !isNull;
        this.nul = isNull;
    }

    private final boolean empty;
    private final boolean nul;
    private final boolean black;
    private final boolean white;
    private final boolean big;
    private final boolean catalyst;
    private final int tiles;
    private final int turns;
}
