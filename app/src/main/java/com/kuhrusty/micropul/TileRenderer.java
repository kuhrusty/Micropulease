package com.kuhrusty.micropul;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.kuhrusty.micropul.model.Board;
import com.kuhrusty.micropul.model.IntCoordinates;
import com.kuhrusty.micropul.model.Owner;
import com.kuhrusty.micropul.model.Tile;
import com.kuhrusty.micropul.model.TileProvider;

/**
 * Instances know how to paint the board, single tiles in the player's hand,
 * and some other stuff.  This may be a pretty lousy API; I sort of bolted crap
 * on as I ran into new things I needed drawn.
 *
 * <p>My intent was that renderers would be stateless, but I don't think that
 * really worked out in practice; instead, a bunch of information is passed
 * around in every API call instead of being stored in local variables for the
 * duration of the rendering operation.</p>
 *
 * <p>Or, maybe you could improve things by using separate instances of the
 * same class: one for the BoardView, a different one for the TileViews, etc.
 * That way, each might only have to scale its paints once.</p>
 */
public interface TileRenderer {

    /**
     * Should return the name you want displayed in the start screen.
     */
    String toString();

    /**
     * The resource ID of a Drawable which shows a preview of this renderer, or
     * 0 or R.drawable.preview_none for a default "no preview" image.
     */
    int getPreviewDrawableID();

    /**
     * This will be called after the renderer has been selected by the user; it
     * should complete the initialization of the renderer.
     */
    void prepare();

    /**
     * Draw the current board state.
     *
     * @param board the board; will not be null.
     * @param showValidPlays if not null, then this is the tile which the player
     *                       is considering playing, and potentially valid plays
     *                       should maybe be indicated.
     * @param showValidGroups if true, then the player is considering playing a
     *                        stone, and potentially valid groups should maybe
     *                        be indicated.
     * @param rect the area to draw in.
     * @param canvas
     */
    void drawBoard(Board board, Tile showValidPlays,
                   boolean showValidGroups, Rect rect, Canvas canvas);

    /**
     * Draw a single tile in the player's hand.  The xpos and ypos parameters
     * exist for implementations which might call this once per tile in drawBoard().
     *
     * @param tp the Board, or a wrapper around a single Tile.
     * @param xpos the tile's x position in the board (in tiles, not squares).
     * @param ypos the tile's y position in the board (in tiles, not squares).
     * @param rect the area to draw in.
     * @param isSelected true if the tile is selected; false if not.
     * @param canvas
     */
    void drawTile(TileProvider tp, int xpos, int ypos, Rect rect, boolean isSelected, Canvas canvas);

    /**
     * Draw some representation of the number of stones remaining for the given
     * player.
     *
     * @param owner Owner.P1 or Owner.P2.
     * @param stones the number of stones remaining.
     * @param rect the area to draw in.
     * @param isSelected true if the player has selected the view being drawn,
     *                   suggesting that they're considering playing a stone
     *                   instead of a tile.
     * @param canvas
     */
    void drawStones(Owner owner, int stones, Rect rect, boolean isSelected, Canvas canvas);

    /**
     * Given a board and a rectangle to render it into, this returns the
     * coordinates of the tile which would be under the given x, y position on
     * the screen, or null.  Note that this may return the coordinates of tiles
     * which are just outside the given board; returning (-1, 0) means the empty
     * space to the left of the tile at (0, 0).
     *
     * @param board
     * @param rect
     * @param xtouch
     * @param ytouch
     * @return
     */
    IntCoordinates touchToTile(Board board, Rect rect, float xtouch, float ytouch);

    /**
     * Given a board and a rectangle to render it into, this returns the
     * coordinates of the square (not tile!) which would be under the given x, y
     * position on the screen, or null.  Note that this may return the
     * coordinates of squares which are just outside the given board; returning
     * (-1, 0) means the empty space to the left of the square at (0, 0).
     *
     * @param board
     * @param rect
     * @param xtouch
     * @param ytouch
     * @return
     */
    IntCoordinates touchToSquare(Board board, Rect rect, float xtouch, float ytouch);
}
