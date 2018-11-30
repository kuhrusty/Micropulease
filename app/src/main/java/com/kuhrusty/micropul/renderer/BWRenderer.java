package com.kuhrusty.micropul.renderer;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.kuhrusty.micropul.R;
import com.kuhrusty.micropul.model.Square;
import com.kuhrusty.micropul.model.TileProvider;

public class BWRenderer extends BaseBoardPlus2Renderer {
    //private static final String LOGBIT = "BWRenderer";

    private Paint tileBGPaint;
    private Paint tileBlackPaint;
    private Paint tileWhitePaint;
    private Paint catalystPaint;

    public BWRenderer(Resources res) {
        super(res.getString(R.string.bw_renderer_name), R.drawable.preview_bwrenderer);
    }

    @Override
    public void prepare() {
        bgPaint = newPaint(Color.GRAY);
        bgGridPaint = newPaint(Color.LTGRAY, Paint.Style.STROKE);
        bgGridPaint.setStrokeWidth(1.0f);
        tileBGPaint = newPaint(Color.LTGRAY);
        tileBlackPaint = newPaint(Color.BLACK);
        tileWhitePaint = newPaint(Color.WHITE);
        catalystPaint = newPaint(Color.RED);
        tileValidPaint = newPaint(Color.YELLOW, 63);
        p1StonePaint = newPaint(Color.RED);
        p2StonePaint = newPaint(Color.BLUE);
        p1GroupPaint = newPaint(Color.RED, 63);
        p2GroupPaint = newPaint(Color.BLUE, 63);
        bothGroupPaint = newPaint(0xff00ff, 63);
    }

    @Override
    public void drawTile(TileProvider board, int xpos, int ypos, Rect rect, boolean isSelected, Canvas canvas) {
        int oldRectLeft = rect.left;
        int oldRectRight = rect.right;
        int oldRectTop = rect.top;
        int oldRectBottom = rect.bottom;
        catalystPaint.setStrokeWidth(rect.width() / 20);
        canvas.drawRect(rect, tileBGPaint);
        int sqx = xpos * 2;
        int sqy = ypos * 2;
        if (board.getSquare(sqx, sqy).isBig()) {
            canvas.drawCircle(rect.left + rect.width() / 2,
                    rect.top + rect.height() / 2, rect.height() / 2 - 4,
                    board.getSquare(sqx, sqy).isBlack() ? tileBlackPaint : tileWhitePaint);
            if (board.getSquare(sqx, sqy).getTileDraws() > 0) {
                canvas.drawCircle(rect.left + rect.width() / 2,
                        rect.top + rect.height() / 2,
                        rect.height() / 20, catalystPaint);
            } else {
                canvas.drawLine(rect.left + rect.width() / 2, rect.top + rect.height() / 2 - rect.height() / 10,
                        rect.left + rect.width() / 2,rect.top + rect.height() / 2 + rect.height() / 10, catalystPaint);
                canvas.drawLine(rect.left + rect.width() / 2 - rect.width() / 10, rect.top + rect.height() / 2,
                        rect.left + rect.width() / 2 + rect.width() / 10,rect.top + rect.height() / 2, catalystPaint);
            }
        } else {
            float p4 = rect.height() / 4f;
            float r = p4 - 2f;
            rect.right = rect.left + rect.width() / 2;
            rect.bottom = rect.top + rect.height() / 2;
            drawSquare(board.getSquare(sqx, sqy + 1), rect, r, canvas);
            rect.left = rect.right;
            rect.right = oldRectRight;
            drawSquare(board.getSquare(sqx + 1, sqy + 1), rect, r, canvas);
            rect.top = rect.bottom;
            rect.bottom = oldRectBottom;
            drawSquare(board.getSquare(sqx + 1, sqy), rect, r, canvas);
            rect.right = rect.left;
            rect.left = oldRectLeft;
            drawSquare(board.getSquare(sqx, sqy), rect, r, canvas);
        }
        rect.set(oldRectLeft, oldRectTop, oldRectRight, oldRectBottom);
        if (isSelected) canvas.drawRect(rect, tileValidPaint);
        drawGroups(board, sqx, sqy, rect, canvas);
    }

    /**
     * catalystPaint is assumed to be set to the right stroke width.
     */
    private void drawSquare(Square square, Rect rect, float micropulRadius, Canvas canvas) {
        if (square.isMicropul()) {
            canvas.drawCircle(rect.left + rect.width() / 2, rect.top + rect.height() / 2, micropulRadius, square.isBlack() ? tileBlackPaint : tileWhitePaint);
        } else if (square.isCatalyst()) {
            if (square.getTileDraws() == 1) {
                canvas.drawCircle(rect.left + rect.width() / 2, rect.top + rect.height() / 2,
                        rect.height() / 10, catalystPaint);
            } else if (square.getTileDraws() == 2) {
                float off = rect.width() / 8;
                canvas.drawCircle(rect.left + rect.width() / 2 - off, rect.top + rect.height() / 2 - off,
                        rect.height() / 10, catalystPaint);
                canvas.drawCircle(rect.left + rect.width() / 2 + off, rect.top + rect.height() / 2 + off,
                        rect.height() / 10, catalystPaint);
            } else if (square.getExtraTurns() == 1) {
                canvas.drawLine(rect.left + rect.width() / 2, rect.top + rect.height() / 2 - rect.height() / 5,
                        rect.left + rect.width() / 2,rect.top + rect.height() / 2 + rect.height() / 5, catalystPaint);
                canvas.drawLine(rect.left + rect.width() / 2 - rect.width() / 5, rect.top + rect.height() / 2,
                        rect.left + rect.width() / 2 + rect.width() / 5,rect.top + rect.height() / 2, catalystPaint);
            } else {
                //Log.w(LOGBIT, "don't know what to do what to do with " + square);
            }
        }
    }
}
