package com.kuhrusty.micropul.renderer;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.kuhrusty.micropul.model.TileProvider;

public interface OwnerRenderer {
    /**
     * Draw group ownership for all four squares on a tile.
     *
     * <p>Note that this takes the coordinates of the lower-left <i>square</i>
     * in the tile, not the coordinates of the tile itself; despite that, it
     * operates on all four squares in the tile.  It just takes square
     * coordinates instead of tile coordinates because all of its operations are
     * going to be on single squares anyway.</p>
     *
     * @param tp probably the Board.
     * @param sqx the tile's x position in the board (in squares, not tiles).
     * @param sqy the tile's y position in the board (in squares, not tiles).
     * @param rect the area to draw in.  This is the entire area of the tile,
     *             not one square.
     * @param canvas
     */
    void drawOwners(TileProvider tp, int sqx, int sqy, Rect rect, Canvas canvas);
}
