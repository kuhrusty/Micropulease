package com.kuhrusty.micropul.model;

/**
 * Ugh.  In some cases, we pass the renderer the full board... but in others,
 * we pass just a single tile to draw.  A Board is a little heavy for that, so
 * in those cases, we'll just pass some wrapper around the Tile, and the
 * renderer doesn't have to worry about which it is.
 *
 * <p>Note that some methods take their coordinates in <i>tiles,</i> while
 * others take their coordinates in <i>squares;</i> each tile is made up of
 * four squares.  (0, 0) is always the lower-left corner, so square (4, 2) is
 * the lower left corner of tile (2, 1).</p>
 */
public interface TileProvider {
    /**
     * Returns the current width of the board in tiles (not squares).  This can
     * change as tiles are played.
     */
    int getWidth();

    /**
     * Returns the current height of the board in tiles (not squares).  This can
     * change as tiles are played.
     */
    int getHeight();

    /**
     * Returns the contents of the square, or Square.Null, never null.  (0, 0)
     * is the lower left square.  Note that these numbers are in squares, not
     * tiles!
     */
    Square getSquare(int squareX, int squareY);

    /**
     * Returns the owner of the square, or Owner.Nobody, never null.  (0, 0)
     * is the lower left square.  Note that these numbers are in squares, not
     * tiles!
     */
    Owner getOwner(int squareX, int squareY);

    /**
     * Returns the Group to which the square belongs, or Group.None, never null.
     * (0, 0) is the lower left square.  Note that these numbers are in squares,
     * not tiles!
     */
    Group getGroup(int squareX, int squareY);

    /**
     * Note that this includes Group.None.
     */
    int getGroupCount();

    /**
     * Note that this will return Group.None instead of null for a groups which
     * used to exist but which were later merged into other groups.
     */
    Group getGroupByID(int id);

    /**
     * Returns the ID of the tile at the given position in the board, or -1.
     *
     * @param tileX
     * @param tileY
     */
    int getTileID(int tileX, int tileY);

    /**
     * Returns the rotation (-1, 0, 1, 2) of the tile at the given position in
     * the board, or 0.
     *
     * @param tileX
     * @param tileY
     */
    int getTileRotation(int tileX, int tileY);
}
