package com.kuhrusty.micropul;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.kuhrusty.micropul.model.GameState;
import com.kuhrusty.micropul.model.IntCoordinates;
import com.kuhrusty.micropul.model.Tile;

/**
 * Basically a wrapper around the TileRenderer.
 */
public class BoardView extends View {
    private static final String LOGBIT = "BoardView";

    public BoardView(Context context) {
        super(context);
    }
    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.d(LOGBIT, "onLayout(" + changed + ", left " + left + ", top " + top + ", right " + right + ", bottom " + bottom + ") hit!");
        super.onLayout(changed, left, top, right, bottom);
        //no idea whether I need to save this
        lastLayout.set(left, top, right, bottom);
    }

    /**
     * Keeps a reference to the GameState.
     */
    public void setGame(GameState game) {
        this.game = game;
    }
    public void setRenderer(TileRenderer renderer) {
        this.renderer = renderer;
    }
    public void setSelectedTile(Tile tile) {
        this.selectedTile = tile;
        if (selectedTile != null) selectedStone = false;
    }
    public void setSelectedStone(boolean selected) {
        this.selectedStone = selected;
        if (selected) this.selectedTile = null;
    }

    public IntCoordinates touchToTilePosition(float xpos, float ypos) {
        return renderer.touchToTile(game.getBoard(), lastLayout, xpos, ypos);
    }
    public IntCoordinates touchToSquarePosition(float xpos, float ypos) {
        return renderer.touchToSquare(game.getBoard(), lastLayout, xpos, ypos);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Log.d(LOGBIT, "onDraw() hit, game == " + game);
        if ((renderer != null) && (game != null)) {
            renderer.drawBoard(game.getBoard(), selectedTile, selectedStone, lastLayout, canvas);
        }
    }

    //  not sure whether it's right to keep some of this stuff in the view.
    private TileRenderer renderer;
    private GameState game;
    private Rect lastLayout = new Rect();
    private Tile selectedTile;
    private boolean selectedStone = false;
}
