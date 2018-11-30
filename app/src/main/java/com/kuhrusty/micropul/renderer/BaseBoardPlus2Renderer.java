package com.kuhrusty.micropul.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.kuhrusty.micropul.TileRenderer;
import com.kuhrusty.micropul.model.Board;
import com.kuhrusty.micropul.model.IntCoordinates;
import com.kuhrusty.micropul.model.Owner;
import com.kuhrusty.micropul.model.Tile;
import com.kuhrusty.micropul.model.TileProvider;

/**
 * This base class draws tiles as big as possible given the available area,
 * with a one-tile border around the board.
 */
public abstract class BaseBoardPlus2Renderer implements TileRenderer {
    private static final String LOGBIT = "BaseBoardPlus2Renderer";

    /**
     * If not null, paints the background of the board area.
     */
    protected Paint bgPaint = null;
    /**
     * If not null, paints tile grid lines in the background of the board area.
     */
    protected Paint bgGridPaint = null;
    /**
     * If not null, paints the background of squares where the selected tile can
     * be played.
     */
    protected Paint tileValidPaint = null;

    /**
     * If not null, this will be used by drawGroups() to paint a crude square
     * over groups owned by player 1.
     */
    protected Paint p1GroupPaint;
    /**
     * If not null, this will be used by drawGroups() to paint a crude square
     * over groups owned by player 2.
     */
    protected Paint p2GroupPaint;
    /**
     * If not null, this will be used by drawGroups() to paint a crude square
     * over groups owned by both players.
     */
    protected Paint bothGroupPaint;

    /**
     * If not null, this will be used by drawStones().
     */
    protected Paint p1StonePaint;
    /**
     * If not null, this will be used by drawStones().
     */
    protected Paint p2StonePaint;

    /**
     * Returns the name passed into the constructor.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns the ID passed into the constructor.
     */
    @Override
    public int getPreviewDrawableID() {
        return previewID;
    }

    /**
     * In your subclass constructor, get the name from resources and pass it
     * in here.  If you have a preview image, pass its ID
     * (R.drawable.your_preview or whatever), or pass 0 or
     * R.drawable.preview_none for a default "no preview" image.
     *
     * @param name
     * @param previewID
     */
    public BaseBoardPlus2Renderer(String name, int previewID) {
        this.name = name;
        this.previewID = previewID;
    }

    /**
     * This fills the board area with bgPaint (if it's not null), and then for
     * each tile, calls drawTile(), followed by drawValidGroups() if
     * showValidGroups is true.  If showValidPlays is not null, then this also
     * calls drawValidTilePlay() for each space where the the tile could legally
     * be played.
     */
    @Override
    public void drawBoard(Board board, Tile showValidPlays, boolean showValidGroups,
                          Rect rect, Canvas canvas) {
        Rect tr = new Rect(0, 0, rect.width(), rect.height());

        if (bgPaint != null) {
            canvas.drawRect(tr, bgPaint);
        }

        int viewRows = board.getHeight() + 2;
        int viewCols = board.getWidth() + 2;
        int tilesize = boardTileSize(board, rect);
        if (bgGridPaint != null) {
            for (int viewRow = 1; viewRow <= viewRows; ++viewRow) {
                int ty = viewRow * tilesize;
                canvas.drawLine(0, ty, viewCols * tilesize, ty, bgGridPaint);
            }
            for (int viewCol = 1; viewCol <= viewCols; ++viewCol) {
                int tx = viewCol * tilesize;
                canvas.drawLine(tx, 0, tx, viewRows * tilesize, bgGridPaint);
            }
        }
        for (int viewRow = 0; viewRow < viewRows; ++viewRow) {
            int boardRow = viewRowToBoardRow(board, viewRow);//viewRows - viewRow - 2;
            int br2 = boardRow * 2;
            for (int viewCol = 0; viewCol < viewCols; ++viewCol) {
                int boardCol = viewColToBoardCol(viewCol);//viewCol - 1;
                if (!board.getSquare(boardCol * 2, br2).isNull()) {
                    tr.set(viewCol * tilesize, viewRow * tilesize, (viewCol + 1) * tilesize, (viewRow + 1) * tilesize);
                    drawTile(board, boardCol, boardRow, tr, false, canvas);
                    if (showValidGroups) {
                        //  tr might've been screwed with in drawTile
                        tr.set(viewCol * tilesize, viewRow * tilesize, (viewCol + 1) * tilesize, (viewRow + 1) * tilesize);
                        drawValidGroups(board, boardCol, boardRow, tr, canvas);
                    }
                } else if (showValidPlays != null) {
                    if (board.isValidPlay(showValidPlays, boardCol, boardRow, null)) {
                        tr.set(viewCol * tilesize, viewRow * tilesize, (viewCol + 1) * tilesize, (viewRow + 1) * tilesize);
                        drawValidTilePlay(board, boardCol, boardRow, tr, canvas);
                    }
                }
            }
        }
    }

    /**
     * In drawBoard(), after calling drawTile(), it calls this if
     * showValidGroups is true.  In the base class, this fills the valid squares
     * with tileValidPaint.
     *
     * @param board will not be null.
     * @param xpos the X position of the tile (not square) being worked on.
     * @param ypos the Y position of the tile (not square) being worked on.
     * @param rect the rectangle to draw into.
     * @param canvas
     */
    protected void drawValidGroups(Board board, int xpos, int ypos, Rect rect, Canvas canvas) {
        if (tileValidPaint == null) return;
        int bc2 = xpos * 2;
        int br2 = ypos * 2;
        int ts2 = rect.width() / 2;
        int oldRectLeft = rect.left;
        int oldRectTop = rect.top;
        if (board.getSquare(bc2, br2 + 1).isMicropul() &&
                board.getOwner(bc2, br2 + 1).equals(Owner.Nobody)) {
            rect.right = rect.left + ts2;
            rect.bottom = rect.top + ts2;
            canvas.drawRect(rect, tileValidPaint);
        }
        if (board.getSquare(bc2 + 1, br2 + 1).isMicropul() &&
                board.getOwner(bc2 + 1, br2 + 1).equals(Owner.Nobody)) {
            rect.left = rect.left + ts2;
            rect.right = rect.left + ts2;
            rect.bottom = rect.top + ts2;
            canvas.drawRect(rect, tileValidPaint);
        }
        if (board.getSquare(bc2, br2).isMicropul() &&
                board.getOwner(bc2, br2).equals(Owner.Nobody)) {
            rect.left = oldRectLeft;
            rect.right = rect.left + ts2;
            rect.top = rect.top + ts2;
            rect.bottom = rect.top + ts2;
            canvas.drawRect(rect, tileValidPaint);
        }
        if (board.getSquare(bc2 + 1, br2).isMicropul() &&
                board.getOwner(bc2 + 1, br2).equals(Owner.Nobody)) {
            rect.left = oldRectLeft + ts2;
            rect.right = rect.left + ts2;
            rect.top = oldRectTop + ts2;
            rect.bottom = rect.top + ts2;
            canvas.drawRect(rect, tileValidPaint);
        }
    }

    /**
     * In drawBoard(), if a tile is selected, this is called once for each
     * location where the tile can be legally played.  In the base class, this
     * just fills the rectangle with tileValidPaint.
     *
     * @param board will not be null.
     * @param xpos the X position of the tile (not square) being considered.
     * @param ypos the Y position of the tile (not square) being considered.
     * @param rect the rectangle to draw into.
     * @param canvas
     */
    protected void drawValidTilePlay(Board board, int xpos, int ypos, Rect rect, Canvas canvas) {
        if (tileValidPaint != null) canvas.drawRect(rect, tileValidPaint);
    }

    /**
     * This is for use by drawTile() implementations after drawing the tile
     * image; if p1GroupPaint, p2GroupPaint, and bothGroupPaint are not null,
     * this just fills each owned square with those paints.
     */
    protected void drawGroups(TileProvider board, int sqx, int sqy, Rect rect, Canvas canvas) {
        Paint paint;
        if (board.getSquare(sqx, sqy).isBig()) {
            paint = ownerToPaint(board.getOwner(sqx, sqy));
            if (paint != null) {
                canvas.drawRect(rect, paint);
            }
            return;
        }
        float p2 = rect.height() / 2;
        if (board.getSquare(sqx, sqy + 1).isMicropul() &&
                ((paint = ownerToPaint(board.getOwner(sqx, sqy + 1))) != null)) {
            canvas.drawRect(rect.left, rect.top, rect.left + p2, rect.top + p2, paint);
        }
        if (board.getSquare(sqx + 1, sqy + 1).isMicropul() &&
                ((paint = ownerToPaint(board.getOwner(sqx + 1, sqy + 1))) != null)) {
            canvas.drawRect(rect.left + p2, rect.top, rect.right, rect.top + p2, paint);
        }
        if (board.getSquare(sqx, sqy).isMicropul() &&
                ((paint = ownerToPaint(board.getOwner(sqx, sqy))) != null)) {
            canvas.drawRect(rect.left, rect.top + p2, rect.left + p2, rect.bottom, paint);
        }
        if (board.getSquare(sqx + 1, sqy).isMicropul() &&
                ((paint = ownerToPaint(board.getOwner(sqx + 1, sqy))) != null)) {
            canvas.drawRect(rect.left + p2, rect.top + p2, rect.right, rect.bottom, paint);
        }
    }

    private Paint ownerToPaint(Owner owner) {
        if ((owner == null) || owner.equals(Owner.Nobody)) return null;
        if (owner.equals(Owner.P1)) return p1GroupPaint;
        if (owner.equals(Owner.P2)) return p2GroupPaint;
        if (owner.equals(Owner.Both)) return bothGroupPaint;
        //Log.w(LOGBIT, "unhandled owner " + owner);
        return null;
    }

    /**
     * Implemented to just draw one circle per remaining stone, in either
     * p1StonePaint or p2StonePaint.  This is garbage, but at least it produces
     * something usable.
     *
     * @param owner Owner.P1 or Owner.P2.
     * @param stones the number of stones remaining.
     * @param rect the area to draw in.
     * @param isSelected true if the player has selected the view being drawn,
     *                   suggesting that they're considering playing a stone
     *                   instead of a tile.
     * @param canvas
     */
    @Override
    public void drawStones(Owner owner, int stones, Rect rect, boolean isSelected, Canvas canvas) {
        Paint stonePaint = owner.equals(Owner.P1) ? p1StonePaint : p2StonePaint;
        if (stonePaint == null) return;
        float r = rect.width() / 6;
        if (stones == 3) {
            canvas.drawCircle(rect.left + r, rect.top + r, r, stonePaint);
            canvas.drawCircle(rect.left + rect.width() / 2, rect.top + rect.height() / 2, r, stonePaint);
            canvas.drawCircle(rect.left + rect.right - r, rect.top + rect.bottom - r, r, stonePaint);
        } else if (stones == 2) {
            float r4 = rect.width() / 4;
            canvas.drawCircle(rect.left + r4, rect.top + r4, r, stonePaint);
            canvas.drawCircle(rect.left + rect.right - r4, rect.top + rect.bottom - r4, r, stonePaint);
        } else if (stones == 1) {
            canvas.drawCircle(rect.left + rect.width() / 2, rect.top + rect.height() / 2, r, stonePaint);
        }
        if (isSelected) canvas.drawRect(rect, tileValidPaint);
    }

    @Override
    public IntCoordinates touchToTile(Board board, Rect rect, float xtouch, float ytouch) {
        int tileSize = boardTileSize(board, rect);
        int xpos = (int)(xtouch / tileSize);
        int ypos = (int)(ytouch / tileSize);
        if ((xpos >= 0) && (xpos < board.getWidth() + 2) &&
            (ypos >= 0) && (ypos < board.getHeight() + 2)) {
            return new IntCoordinates(viewColToBoardCol(xpos), viewRowToBoardRow(board, ypos));
        }
        return null;
    }

    @Override
    public IntCoordinates touchToSquare(Board board, Rect rect, float xtouch, float ytouch) {
        int squareSize = boardTileSize(board, rect) / 2;
        int xpos = (int)(xtouch / squareSize);
        int ypos = (int)(ytouch / squareSize);
        if ((xpos >= 0) && (xpos < (board.getWidth() + 2) * 2) &&
                (ypos >= 0) && (ypos < (board.getHeight() + 2) * 2)) {
            return new IntCoordinates(viewColToBoardColSq(xpos), viewRowToBoardRowSq(board, ypos));
        }
        return null;
    }

    private int boardTileSize(Board board, Rect rect) {
        int tw = rect.width() / (board.getWidth() + 2);
        int th = rect.height() / (board.getHeight() + 2);
        return (tw < th) ? tw : th;
    }

    private int viewColToBoardCol(int viewCol) {
        return viewCol - 1;
    }
    private int viewRowToBoardRow(Board board, int viewRow) {
        return board.getHeight() - viewRow;
    }

    /** Squares instead of tiles. */
    private int viewColToBoardColSq(int viewCol) {
        //  if our board width is 2, then our view width is 4,
        //  and the mapping goes
        //  view col     0   1   2   3   4   5   6   7
        //  board col   -2  -1   0   1   2   3   4   5
        return viewCol - 2;
    }
    /** Squares instead of tiles. */
    private int viewRowToBoardRowSq(Board board, int viewRow) {
        //  if our board height is 2, then our view height is 4,
        //  and the mapping goes
        //  view row     0   1   2   3   4   5   6   7
        //  board row    5   4   3   2   1   0  -1  -2
        //  for board height 1, view height is 3, and it's
        //  view row     0   1   2   3   4   5
        //  board row    3   2   1   0  -1  -2
        return board.getHeight() * 2 + 1 - viewRow;
    }

    /**
     * Dumb utility method which saves me a line of code; returns a new Paint
     * set to the given color.
     */
    protected static Paint newPaint(int color) {
        Paint rv = new Paint();
        rv.setColor(color);
        return rv;
    }
    /**
     * Dumb utility method which saves me a line of code; returns a new Paint
     * set to the given color & alpha.
     */
    protected static Paint newPaint(int color, int alpha) {
        Paint rv = new Paint();
        rv.setColor(color);
        rv.setAlpha(alpha);
        return rv;
    }
    /**
     * Dumb utility method which saves me a line of code; returns a new Paint
     * set to the given color & alpha.
     */
    protected static Paint newPaint(int color, Paint.Style style) {
        Paint rv = new Paint();
        rv.setColor(color);
        rv.setStyle(style);
        return rv;
    }

    private String name;
    private int previewID;
}
