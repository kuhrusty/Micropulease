package com.kuhrusty.micropul.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.kuhrusty.micropul.model.Owner;
import com.kuhrusty.micropul.model.TileProvider;

/**
 * This paints crude rectangles around the edges of squares which form the
 * boundary between owned groups and other groups.
 */
public class BorderOwnerRenderer implements OwnerRenderer {
    private Paint p1Paint;
    private Paint p2Paint;
    private Paint bothPaint;

    //  well... we calculate these guys once at the start of drawOwners()
    private int tileSize = -1;
    private int squareSize = -1;
    private int borderWidth = -1;

    /**
     * @param p1Paint will be used to paint rectangles around the edges of
     *                groups owned by player 1.
     * @param p2Paint will be used to paint rectangles around the edges of
     *                groups owned by player 2.
     * @param bothPaint will be used to paint rectangles around the edges of
     *                  groups owned by both players.
     */
    public BorderOwnerRenderer(Paint p1Paint, Paint p2Paint, Paint bothPaint) {
        this.p1Paint = p1Paint;
        this.p2Paint = p2Paint;
        this.bothPaint = bothPaint;
    }

    @Override
    public void drawOwners(TileProvider board, int sqx, int sqy, Rect rect, Canvas canvas) {
        if (rect.width() != tileSize) {
            tileSize = rect.width();
            squareSize = tileSize / 2;
            borderWidth = tileSize / 16;
        }
        if (board.getSquare(sqx, sqy).isBig()) {
            Owner owner = board.getOwner(sqx, sqy);
            if (owner.equals(Owner.Nobody)) return;

            int gid = board.getGroup(sqx, sqy).getID();
            Paint tp = owner.equals(Owner.P1) ? p1Paint : (owner.equals(Owner.P2) ? p2Paint : bothPaint);
            //  top edge
            if (needEdge(board, sqx, sqy + 2, gid)) {
                canvas.drawRect(rect.left, rect.top, rect.left + squareSize, rect.top + borderWidth, tp);
            }
            if (needEdge(board, sqx + 1, sqy + 2, gid)) {
                canvas.drawRect(rect.left + squareSize, rect.top, rect.right, rect.top + borderWidth, tp);
            }
            //  left edge
            if (needEdge(board, sqx - 1, sqy + 1, gid)) {
                canvas.drawRect(rect.left, rect.top, rect.left + borderWidth, rect.top + squareSize, tp);
            }
            if (needEdge(board, sqx - 1, sqy, gid)) {
                canvas.drawRect(rect.left, rect.top + squareSize, rect.left + borderWidth, rect.bottom, tp);
            }
            //  right edge
            if (needEdge(board, sqx + 2, sqy + 1, gid)) {
                canvas.drawRect(rect.right - borderWidth, rect.top, rect.right, rect.top + squareSize, tp);
            }
            if (needEdge(board, sqx + 2, sqy, gid)) {
                canvas.drawRect(rect.right - borderWidth, rect.top + squareSize, rect.right, rect.bottom, tp);
            }
            //  bottom
            if (needEdge(board, sqx, sqy - 1, gid)) {
                canvas.drawRect(rect.left, rect.bottom - borderWidth, rect.left + squareSize, rect.bottom, tp);
            }
            if (needEdge(board, sqx + 1, sqy - 1, gid)) {
                canvas.drawRect(rect.left + squareSize, rect.bottom - borderWidth, rect.right, rect.bottom, tp);
            }
            return;
        }

        int oldRectLeft = rect.left;
        int oldRectTop = rect.top;
        for (int xoff = 0; xoff <= 1; ++xoff) {
            for (int yoff = 0; yoff <= 1; ++yoff) {
                //  Maybe draw a border around this square.
                Owner to = board.getOwner(sqx + xoff, sqy + yoff);
                if (to.equals(Owner.Nobody)) continue;

                rect.set(oldRectLeft + (xoff * squareSize),
                        oldRectTop + ((1 - yoff) * squareSize),
                        oldRectLeft + ((xoff + 1) * squareSize),
                        oldRectTop + ((2 - yoff) * squareSize));
                int tx = sqx + xoff;
                int ty = sqy + yoff;
                int gid = board.getGroup(tx, ty).getID();
                Paint tp = to.equals(Owner.P1) ? p1Paint : (to.equals(Owner.P2) ? p2Paint : bothPaint);
                //  top edge
                if (needEdge(board, tx, ty + 1, gid)) {
                    canvas.drawRect(rect.left, rect.top, rect.right, rect.top + borderWidth, tp);
                }
                //  left edge
                if (needEdge(board, tx - 1, ty, gid)) {
                    canvas.drawRect(rect.left, rect.top, rect.left + borderWidth, rect.bottom, tp);
                }
                //  right edge
                if (needEdge(board, tx + 1, ty, gid)) {
                    canvas.drawRect(rect.right - borderWidth, rect.top, rect.right, rect.bottom, tp);
                }
                //  bottom
                if (needEdge(board, tx, ty - 1, gid)) {
                    canvas.drawRect(rect.left, rect.bottom - borderWidth, rect.right, rect.bottom, tp);
                }
            }
        }
    }

    //  If this square isn't null, and doesn't have the same owner and color,
    //  then return true (we don't want to draw a line if the adjacent square
    //  has no tile or is part of the same group).
    private boolean needEdge(TileProvider board, int sqx, int sqy, int group) {
        if (board.getSquare(sqx, sqy).isNull()) {
            return false;
        }
        return board.getGroup(sqx, sqy).getID() != group;
    }
}
