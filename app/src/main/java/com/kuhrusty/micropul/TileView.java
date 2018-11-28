package com.kuhrusty.micropul;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.kuhrusty.micropul.model.Owner;
import com.kuhrusty.micropul.model.SingleTileWrapper;
import com.kuhrusty.micropul.model.Tile;

/**
 * A View which displays a single tile in the player's hand.
 */
public class TileView extends View {
    private static final String LOGBIT = "TileView";

    private TileRenderer renderer;
    private SingleTileWrapper tile = new SingleTileWrapper(null);

    //  seems suspicious to have this here.
    private Rect lastLayout = new Rect(0, 0, 0, 0);

    //  real classy.  If we're actually using this to display remaining stones
    //  instead of a tile in the player's hand because we're too lazy to create
    //  a new class for that, then this will store 0-3.
    private int stonesRemaining = -1;
    private Owner stonesOwner = null;

    public TileView(Context context) {
        super(context);
    }
    public TileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRenderer(TileRenderer renderer) {
        this.renderer = renderer;
    }
    public void setTile(Tile tile) {
        this.tile.setTile(tile);
    }
    public Tile getTile() {
        return tile.getTile();
    }
    public void setStonesRemaining(Owner owner, int remaining) {
        stonesOwner = owner;
        stonesRemaining = remaining;
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.d(LOGBIT, "onLayout(" + changed + ", left " + left + ", top " + top + ", right " + right + ", bottom " + bottom + ") hit!");
        super.onLayout(changed, left, top, right, bottom);
        lastLayout.right = right - left;
        lastLayout.bottom = bottom - top;
        //lastLayout.set(left, top, right, bottom);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Log.d(LOGBIT, "onDraw() hit, selected == " + isSelected());
        if (renderer == null) {
            return;  //  something's gone wrong
        }
        if (stonesRemaining > 0) renderer.drawStones(stonesOwner, stonesRemaining, lastLayout, isSelected(), canvas);
        if ((renderer != null) && (tile.getTile() != null)) {
            renderer.drawTile(tile, 0, 0, lastLayout, isSelected(), canvas);
        }
    }
}
