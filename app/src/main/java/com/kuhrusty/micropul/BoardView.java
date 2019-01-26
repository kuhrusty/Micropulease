package com.kuhrusty.micropul;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.kuhrusty.micropul.model.GameState;
import com.kuhrusty.micropul.model.IntCoordinates;
import com.kuhrusty.micropul.model.Tile;

/**
 * Basically a wrapper around the TileRenderer.
 */
public class BoardView extends View {
    private static final String LOGBIT = "BoardView";

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float oldScale = scale;
            scale *= detector.getScaleFactor();

            //  Well, TileRenderer scales the board as it grows to fit inside
            //  the rectangle we gave it, so no need to let the scale go below
            //  1.  And, let's set some arbitrary upper bound, too.
            if (scale < 1.0f) scale = 1.0f;
            else if (scale > 5.0f) scale = 5.0f;

            if (scale != oldScale) {
                maxPanX = (scale - 1f) * lastWidth;
                maxPanY = (scale - 1f) * lastHeight;
                invalidate();
            }
            return true;
        }
    }

    private class PanListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            scrollSinceLastActionDown = true;
            float oldPanX = panX;
            float oldPanY = panY;
            panX += distanceX;
            panY += distanceY;

            //  No sense letting these guys go less than zero or greater than
            //  the amount of window hanging off the screen.
            if (panX < 0f) panX = 0f;
            else if (panX > maxPanX) panX = maxPanX;
            if (panY < 0f) panY = 0f;
            else if (panY > maxPanY) panY = maxPanY;

            if ((panX != oldPanX) || (panY != oldPanY)) {
                invalidate();
            }
            return true;
        }
    }

    public BoardView(Context context) {
        super(context);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        panDetector = new GestureDetector(context, new PanListener());
    }
    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        panDetector = new GestureDetector(context, new PanListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            scrollSinceLastActionDown = false;
        }
        boolean rv = scaleDetector.onTouchEvent(ev);
        rv = panDetector.onTouchEvent(ev) || rv;
        if (!rv) super.onTouchEvent(ev);
        //  I don't know,
        //  https://developer.android.com/training/gestures/scale#basic-scaling-example
        //  had this, and it didn't work when it was returning false.
        return true;
    }

    /**
     * Returns true if we've detected a scroll gesture since our last
     * ACTION_DOWN motion event.
     */
    public boolean scrollSinceLastActionDown() {
        return scrollSinceLastActionDown;
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.d(LOGBIT, "onLayout(" + changed + ", left " + left + ", top " + top + ", right " + right + ", bottom " + bottom + ") hit!");
        super.onLayout(changed, left, top, right, bottom);
        //no idea whether I need to save this
        lastLayout.set(left, top, right, bottom);
        lastWidth = lastLayout.width();
        lastHeight = lastLayout.height();
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
        xpos = (xpos + panX) / scale;
        ypos = (ypos + panY) / scale;
        return renderer.touchToTile(game.getBoard(), lastLayout, xpos, ypos);
    }
    public IntCoordinates touchToSquarePosition(float xpos, float ypos) {
        xpos = (xpos + panX) / scale;
        ypos = (ypos + panY) / scale;
        return renderer.touchToSquare(game.getBoard(), lastLayout, xpos, ypos);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Log.d(LOGBIT, "onDraw() hit, pan " + panX + ", " + panY +
                ", scale " + scale + ", game == " + game);
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(-panX, -panY);
        canvas.scale(scale, scale);
        if ((renderer != null) && (game != null)) {
            renderer.drawBoard(game.getBoard(), selectedTile, selectedStone, lastLayout, canvas);
        }
        canvas.restore();
    }

    //  not sure whether it's right to keep some of this stuff in the view.
    private TileRenderer renderer;
    private GameState game;
    private Tile selectedTile;
    private boolean selectedStone = false;

    private Rect lastLayout = new Rect();
    private int lastWidth;
    private int lastHeight;

    private ScaleGestureDetector scaleDetector;
    private GestureDetector panDetector;
    private float scale = 1f;
    private float panX = 0f;
    private float panY = 0f;
    private float maxPanX = 0f;  //  this is the upper limit on those guys
    private float maxPanY = 0f;
    private boolean scrollSinceLastActionDown = false;
}
