package com.kuhrusty.micropul.renderer;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.kuhrusty.micropul.R;
import com.kuhrusty.micropul.model.Owner;
import com.kuhrusty.micropul.model.Square;
import com.kuhrusty.micropul.model.TileProvider;

public class CarthaginianRenderer1 extends BaseBoardPlus2Renderer {
    //private static final String LOGBIT = "CarthaginianRenderer1";

    private interface PaintChooser {
        Paint paintForSquare(TileProvider board, int squareX, int squareY);
    }

    private Paint tileBGPaint;
    private Paint tileBlackPaint;
    private Paint tileWhitePaint;
    private Paint tileBlackVPPaint1;
    private Paint tileWhiteVPPaint1;
    private Paint tileBlackVPPaint2;
    private Paint tileWhiteVPPaint2;
    private Paint tileBlackVPPaint3;
    private Paint tileWhiteVPPaint3;
    private Paint tileBlackVPPaint4;
    private Paint tileWhiteVPPaint4;
    private Paint catalystPaint;
    private Paint catalystFillPaint;
    private Paint catalystSymbolPaint;

    private Paint p1StonePaint;
    private Paint p2StonePaint;
    private Paint p1GroupPaint;
    private Paint p2GroupPaint;
    private Paint bothGroupPaint;

    /**  Returns tileBlackPaint, tileWhitePaint, catalystPaint, or null */
    private PaintChooser micropulPaintChooser;
    /**  Returns p1GroupPaint, p2GroupPaint, bothGroupPaint, or null. */
    private PaintChooser ownerPaintChooser;

    //  Well, the plan was for renderers to be stateless, but that kind of went
    //  out the window when I started calling paint.setStrokeWidth() in
    //  drawTile().
    private Canvas canvas;

    public CarthaginianRenderer1(Resources res) {
        super(res.getString(R.string.carthaginian1_renderer_name), R.drawable.preview_carthaginian1);
    }

    @Override
    public void prepare() {
        bgPaint = newPaint(Color.GRAY);
        tileBGPaint = newPaint(Color.BLACK);
        tileBlackPaint = newPaint(0xff0080ff, Paint.Style.STROKE);
        tileWhitePaint = newPaint(0xff00bb00, Paint.Style.STROKE);

        tileBlackVPPaint1 = newPaint(tileBlackPaint.getColor());
        tileWhiteVPPaint1 = newPaint(tileWhitePaint.getColor());
        tileBlackVPPaint2 = newPaint(0xff22aaff);
        tileWhiteVPPaint2 = newPaint(0xff44dd44);
        tileBlackVPPaint3 = newPaint(0xff44ccff);
        tileWhiteVPPaint3 = newPaint(0xff66ff66);
        tileBlackVPPaint4 = newPaint(0xff88ffff);
        //  tileBlackVPPaint4 is up by 0x4433; this is cranked up higher because
        //  the green is lighter, so the same increase as black wasn't really
        //  visible.
        tileWhiteVPPaint4 = newPaint(0xffccffcc);  //  cranking higher

        catalystPaint = newPaint(Color.GRAY, Paint.Style.STROKE);
        catalystFillPaint = newPaint(0xff808080);
        catalystSymbolPaint = newPaint(Color.BLACK);
        tileValidPaint = newPaint(Color.WHITE, 127);
        p1StonePaint = newPaint(0xffdddd00);//Color.YELLOW);
        p2StonePaint = newPaint(Color.CYAN);
        p1GroupPaint = newPaint(0xffdddd00, Paint.Style.STROKE);//Color.YELLOW);
        p2GroupPaint = newPaint(Color.CYAN, Paint.Style.STROKE);
        bothGroupPaint = newPaint(Color.BLACK, Paint.Style.STROKE);
        micropulPaintChooser = new PaintChooser() {
            @Override
            public Paint paintForSquare(TileProvider board, int squareX, int squareY) {
                Square square = board.getSquare(squareX, squareY);
                if (square.isMicropul()) {
                    return square.isBlack() ? tileBlackPaint : tileWhitePaint;
                } else if (square.isCatalyst()) {
                    return catalystPaint;
                }
                return null;
            }
        };
        ownerPaintChooser = new PaintChooser() {
            @Override
            public Paint paintForSquare(TileProvider board, int squareX, int squareY) {
                Square square = board.getSquare(squareX, squareY);
                if (!square.isMicropul()) return null;
                Owner owner = board.getOwner(squareX, squareY);
                if ((owner == null) || owner.equals(Owner.Nobody)) return null;
                else if (owner.equals(Owner.P1)) return p1GroupPaint;
                else if (owner.equals(Owner.P2)) return p2GroupPaint;
                else if (owner.equals(Owner.Both)) return bothGroupPaint;
                return null;//throw new RuntimeException("Unhandled owner: " + owner.toString());
            }
        };
    }

    @Override
    public void drawTile(TileProvider board, int xpos, int ypos, Rect rect, boolean isSelected, Canvas canvas) {
        this.canvas = canvas;
        tileBlackPaint.setStrokeWidth(rect.width() / 4);
        tileWhitePaint.setStrokeWidth(rect.width() / 4);
        catalystPaint.setStrokeWidth(rect.width() / 6);
        catalystSymbolPaint.setStrokeWidth(rect.width() / 20);
        p1GroupPaint.setStrokeWidth(rect.width() / 10);
        p2GroupPaint.setStrokeWidth(rect.width() / 10);
        bothGroupPaint.setStrokeWidth(rect.width() / 10);

        canvas.drawRect(rect, tileBGPaint);
        int sqx = xpos * 2;
        int sqy = ypos * 2;
        drawTileLines(board, sqx, sqy, rect, micropulPaintChooser);
        drawTileLines(board, sqx, sqy, rect, ownerPaintChooser);
        if (isSelected) canvas.drawRect(rect, tileValidPaint);
    }

    /**
     * Assumes canvas has been set.
     */
    private void drawTileLines(TileProvider board, int sqx, int sqy, Rect rect, PaintChooser paintChooser) {
        int oldRectLeft = rect.left;
        int oldRectRight = rect.right;
        int oldRectTop = rect.top;
        int oldRectBottom = rect.bottom;
        final float p4 = rect.height() / 4f;
        final float catR = catalystPaint.getStrokeWidth() * 0.75f;
        RectF rectf = new RectF();//oldRectLeft - p4, oldRectTop - p4, oldRectLeft + p4, oldRectTop + p4);
        Paint paint = null;
        if (board.getSquare(sqx, sqy).isBig()) {
            if ((paint = paintChooser.paintForSquare(board, sqx, sqy)) != null) {
                //  If we're painting micropul lines, do arches along the edges.
                if ((paint == tileBlackPaint) || (paint == tileWhitePaint)) {
                    rectf.set(oldRectLeft + p4, oldRectTop - p4, oldRectRight - p4, oldRectTop + p4);
                    canvas.drawArc(rectf, 0, 180, false, paint);
                    rectf.set(oldRectRight - p4, oldRectTop + p4, oldRectRight + p4, oldRectBottom - p4);
                    canvas.drawArc(rectf, 90, 180, false, paint);
                    rectf.set(oldRectLeft + p4, oldRectBottom - p4, oldRectRight - p4, oldRectBottom + p4);
                    canvas.drawArc(rectf, 180, 180, false, paint);
                    rectf.set(oldRectLeft - p4, oldRectTop + p4, oldRectLeft + p4, oldRectBottom - p4);
                    canvas.drawArc(rectf, 270, 180, false, paint);
                }

                rectf.set(oldRectLeft - p4, oldRectTop - p4, oldRectLeft + p4, oldRectTop + p4);
                //  Draw corner arcs.
                //  Note that the order of the arcs is different here than in
                //  the non-big-micropul case below.
                canvas.drawArc(rectf, 0, 90, false, paint);
                rectf.left = oldRectRight - p4;
                rectf.right = oldRectRight + p4;
                canvas.drawArc(rectf, 90, 90, false, paint);
                rectf.top = oldRectBottom - p4;
                rectf.bottom = oldRectBottom + p4;
                canvas.drawArc(rectf, 180, 90, false, paint);
                rectf.left = oldRectLeft - p4;
                rectf.right = oldRectLeft + p4;
                canvas.drawArc(rectf, 270, 90, false, paint);
                //  Draw final circle.
                rectf.set(oldRectLeft + p4, oldRectTop + p4, oldRectRight - p4, oldRectBottom - p4);
                float inset = rect.width() / 8f;//paint.getStrokeWidth() / 2f;
                rectf.set(oldRectLeft + inset, oldRectTop + inset,
                        oldRectRight - inset, oldRectBottom - inset);
                Paint tp = paint;
                if (tp == tileBlackPaint) tp = tileBlackVPPaint1;
                else if (tp == tileWhitePaint) tp = tileWhiteVPPaint1;
                canvas.drawArc(rectf, 0, 360, false, tp);

                if ((paint == tileBlackPaint) || (paint == tileWhitePaint)) {
                    drawVPSymbol(paint == tileBlackPaint, rectf.left + rectf.width() / 2f,
                            rectf.top + rectf.width() / 2f - catR * 2 /*catalystPaint.getStrokeWidth() * 2 /*catR*/, catR);
                    drawCatalystSymbol(board.getSquare(sqx, sqy), rectf.left + rectf.width() / 2f,
                            rectf.top + rectf.width() / 2f /* + catalystPaint.getStrokeWidth() /*catR*/, catR);
                }
            }
        } else {
            Square s1 = board.getSquare(sqx, sqy + 1);
            Square s2 = board.getSquare(sqx + 1, sqy + 1);
            if (s1.isMicropul() && s2.isMicropul() && (s1.isBlack() == s2.isBlack()) &&
                    ((paint = paintChooser.paintForSquare(board, sqx, sqy + 1)) != null)) {
                rectf.set(oldRectLeft + p4, oldRectTop - p4, oldRectRight - p4, oldRectTop + p4);
                canvas.drawArc(rectf, 0, 180, false, paint);
            }
            s1 = s2;
            s2 = board.getSquare(sqx + 1, sqy);
            if (s1.isMicropul() && s2.isMicropul() && (s1.isBlack() == s2.isBlack()) &&
                    ((paint = paintChooser.paintForSquare(board, sqx + 1, sqy)) != null)) {
                rectf.set(oldRectRight - p4, oldRectTop + p4, oldRectRight + p4, oldRectBottom - p4);
                canvas.drawArc(rectf, 90, 180, false, paint);
            }
            s1 = s2;
            s2 = board.getSquare(sqx, sqy);
            if (s1.isMicropul() && s2.isMicropul() && (s1.isBlack() == s2.isBlack()) &&
                    ((paint = paintChooser.paintForSquare(board, sqx, sqy)) != null)) {
                rectf.set(oldRectLeft + p4, oldRectBottom - p4, oldRectRight - p4, oldRectBottom + p4);
                canvas.drawArc(rectf, 180, 180, false, paint);
            }
            s1 = s2;
            s2 = board.getSquare(sqx, sqy + 1);
            if (s1.isMicropul() && s2.isMicropul() && (s1.isBlack() == s2.isBlack()) &&
                    ((paint = paintChooser.paintForSquare(board, sqx, sqy)) != null)) {
                rectf.set(oldRectLeft - p4, oldRectTop + p4, oldRectLeft + p4, oldRectBottom - p4);
                canvas.drawArc(rectf, 270, 180, false, paint);
            }

            //  Corner arcs.
            rectf.set(oldRectLeft - p4, oldRectTop - p4, oldRectLeft + p4, oldRectTop + p4);
            if ((paint = paintChooser.paintForSquare(board, sqx, sqy + 1)) != null) {
                canvas.drawArc(rectf, 0, 90, false, paint);
                if (paint == catalystPaint) {
                    drawCatalystSymbol(board.getSquare(sqx, sqy + 1), rectf.right, rectf.bottom, catR);
                } else if ((paint == tileBlackPaint) || (paint == tileWhitePaint)) {
                    drawVPSymbol(paint == tileBlackPaint, rectf.right, rectf.bottom, catR);
                }
            }
            if ((paint = paintChooser.paintForSquare(board, sqx + 1, sqy + 1)) != null) {
                rectf.left = oldRectRight - p4;
                rectf.right = oldRectRight + p4;
                canvas.drawArc(rectf, 90, 90, false, paint);
                if (paint == catalystPaint) {
                    drawCatalystSymbol(board.getSquare(sqx + 1, sqy + 1), rectf.left, rectf.bottom, catR);
                } else if ((paint == tileBlackPaint) || (paint == tileWhitePaint)) {
                    drawVPSymbol(paint == tileBlackPaint, rectf.left, rectf.bottom, catR);
                }
            }
            if ((paint = paintChooser.paintForSquare(board, sqx, sqy)) != null) {
                rectf.set(oldRectLeft - p4, oldRectBottom - p4, oldRectLeft + p4, oldRectBottom + p4);
                canvas.drawArc(rectf, 270, 90, false, paint);
                if (paint == catalystPaint) {
                    drawCatalystSymbol(board.getSquare(sqx, sqy), rectf.right, rectf.top, catR);
                } else if ((paint == tileBlackPaint) || (paint == tileWhitePaint)) {
                    drawVPSymbol(paint == tileBlackPaint, rectf.right, rectf.top, catR);
                }
            }
            if ((paint = paintChooser.paintForSquare(board, sqx + 1, sqy)) != null) {
                rectf.set(oldRectRight - p4, oldRectBottom - p4, oldRectRight + p4, oldRectBottom + p4);
                canvas.drawArc(rectf, 180, 90, false, paint);
                if (paint == catalystPaint) {
                    drawCatalystSymbol(board.getSquare(sqx + 1, sqy), rectf.left, rectf.top, catR);
                } else if ((paint == tileBlackPaint) || (paint == tileWhitePaint)) {
                    drawVPSymbol(paint == tileBlackPaint, rectf.left, rectf.top, catR);
                }
            }
        }
        rect.set(oldRectLeft, oldRectTop, oldRectRight, oldRectBottom);
    }

    /**
     * Assumes canvas has been set.
     */
    private void drawCatalystSymbol(Square square, float cx, float cy, float radius) {
        canvas.drawCircle(cx, cy, radius, catalystFillPaint);
        radius = radius / 4f;
        if (square.getTileDraws() == 1) {
            canvas.drawCircle(cx, cy, radius, catalystSymbolPaint);
        } else if (square.getTileDraws() == 2) {
            float off = radius;// * 2f;//rect.width() / 8;
            canvas.drawCircle(cx - off, cy - off, radius, catalystSymbolPaint);
            canvas.drawCircle(cx + off, cy + off, radius, catalystSymbolPaint);
        } else if (square.getExtraTurns() == 1) {
            float off = radius * 2.5f;
            canvas.drawLine(cx, cy - off, cx, cy + off, catalystSymbolPaint);
            canvas.drawLine(cx - off, cy, cx + off, cy, catalystSymbolPaint);
        } else {
            //brain damage
        }
    }

    /**
     * Assumes canvas has been set.
     */
    private void drawVPSymbol(boolean isBlack, float cx, float cy, float radius) {
        canvas.drawCircle(cx, cy, radius, isBlack ? tileBlackVPPaint1 : tileWhiteVPPaint1);
        canvas.drawCircle(cx, cy, radius * 0.75f, isBlack ? tileBlackVPPaint2 : tileWhiteVPPaint2);
        canvas.drawCircle(cx, cy, radius * 0.5f, isBlack ? tileBlackVPPaint3 : tileWhiteVPPaint3);
        float tf = radius * 0.25f;
        canvas.drawCircle(cx, cy - tf / 2f , tf, isBlack ? tileBlackVPPaint4 : tileWhiteVPPaint4);
    }

    //protected void drawValidTilePlay(Board board, int xpos, int ypos, Rect rect, Canvas canvas) {
    //ehh... was going to do something fancy here, but...
    //}

    @Override
    public void drawStones(Owner owner, int stones, Rect rect, boolean isSelected, Canvas canvas) {
        Paint stonePaint = owner.equals(Owner.P1) ? p1StonePaint : p2StonePaint;
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
}
