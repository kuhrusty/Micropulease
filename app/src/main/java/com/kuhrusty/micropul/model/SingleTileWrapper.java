package com.kuhrusty.micropul.model;

/**
 * A TileProvider wrapper around a single Tile in a player's hand.
 */
public class SingleTileWrapper implements TileProvider {
    public SingleTileWrapper(Tile tile) {
        this.tile = tile;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }
    public Tile getTile() {
        return tile;
    }

    /**
     * Returns 1.
     */
    @Override
    public int getWidth() {
        return 1;
    }

    /**
     * Returns 1.
     */
    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public Square getSquare(int squareX, int squareY) {
        if (squareX == 0) {
            if (squareY == 0) return tile.getLowerLeft();
            else if (squareY == 1) return tile.getUpperLeft();
        } else if (squareX == 1) {
            if (squareY == 0) return tile.getLowerRight();
            else if (squareY == 1) return tile.getUpperRight();
        }
        return Square.Null;
    }

    /**
     * Returns Owner.Nobody.
     */
    @Override
    public Owner getOwner(int squareX, int squareY) {
        return Owner.Nobody;
    }

    /**
     * Returns Group.None.
     */
    @Override
    public Group getGroup(int squareX, int squareY) {
        return Group.None;
    }

    /**
     * Returns 0.  (Technically this could return 1, for Group.None, but... it
     * doesn't.)
     */
    @Override
    public int getGroupCount() {
        return 0;
    }

    /**
     * Returns Group.None.
     */
    @Override
    public Group getGroupByID(int id) {
        return Group.None;
    }

    private Tile tile;
}
