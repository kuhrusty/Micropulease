package com.kuhrusty.micropul.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.kuhrusty.micropul.R;
import com.kuhrusty.micropul.model.Square;
import com.kuhrusty.micropul.model.TileProvider;

/**
 * This uses images from Archinerd's Stick/Mud Farmer re-theme at
 * https://boardgamegeek.com/filepage/35772/stickmud-farmer-re-theme.  Each tile
 * is drawn by slapping down various sprites for micropul, catalysts, etc.
 *
 * <p>I know Drawable has support for selecting different images depending on the
 * state of some value in your application, and it might be interesting to try a
 * version of this which takes advantage of that, but ... I didn't.</p>
 *
 * <p>You could do a much better, fun job than this code does; really, you want
 * buildings always right-side-up (which this does), and nice edges on every
 * micropul which is adjacent to something other than micropuls of the same
 * color (which this doesn't quite do).  Also, rather than using
 * BorderOwnerRenderer, I wanted to have wall or fence images, but I didn't
 * spend enough time to come up with a scheme I was happy with.</p>
 */
public class StickMudRenderer2 extends BaseBoardPlus2Renderer {
    private static final String LOGBIT = "StickMudRenderer2";

    private Context context;
    private Bitmap tileBGs[];
    private Bitmap singleCatalysts[];
    private Bitmap doubleCatalysts[];
    private Bitmap plusCatalyst[];
    //  real classy
    private Bitmap tile36;
    private Bitmap tile37;
    private Bitmap tile42;
    private Bitmap tile43;
    private static class Micropuls {
        //  see explodeNines() for more on these guys.
        Bitmap upperLeftS[];
        Bitmap upperS[];
        Bitmap upperRightS[];
        Bitmap leftS[];
        Bitmap centerS[];
        Bitmap rightS[];
        Bitmap lowerLeftS[];
        Bitmap lowerS[];
        Bitmap lowerRightS[];
    }
    private Micropuls black;
    private Micropuls white;

    public StickMudRenderer2(Context context) {
        super(context.getResources().getString(R.string.stick_mud2_renderer_name),
                R.drawable.preview_stick2);
        this.context = context;
    }

    @Override
    public void prepare() {
        bgPaint = newPaint(0xffc8dd5c);
        bgGridPaint = newPaint(0xff9d6f3d, Paint.Style.STROKE);
        bgGridPaint.setStrokeWidth(1.0f);
        tileValidPaint = newPaint(Color.YELLOW, 127);
        p1StonePaint = newPaint(Color.RED);
        p2StonePaint = newPaint(Color.BLUE);
        ownerRenderer = new BorderOwnerRenderer(newPaint(Color.RED, 127),
                newPaint(Color.BLUE, 127), newPaint(Color.DKGRAY, 127));

        tileBGs = quarter(R.drawable.stick_bg4);
        singleCatalysts = quarter(R.drawable.stick_c1_4);
        doubleCatalysts = quarter(R.drawable.stick_c2_4);
        plusCatalyst = new Bitmap[1];
        plusCatalyst[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.stick_cp_1);
        Bitmap[] tba = quarter(R.drawable.stick_big);
        //  we, uhh, "know" the order of the images in that file:
        tile36 = tba[0];
        tile37 = tba[1];
        tile42 = tba[2];
        tile43 = tba[3];

        black = new Micropuls();
        explodeNines(R.drawable.stick_black9, black);

        white = new Micropuls();
        explodeNines(R.drawable.stick_white9, white);
    }

    //  Breaks the given Bitmap resource into quarters, and returns them as an
    //  array of smaller Bitmaps:  0  1
    //                             2  3
    private Bitmap[] quarter(int resID) {
        Bitmap[] rv = new Bitmap[4];
        Bitmap tb = BitmapFactory.decodeResource(context.getResources(), resID);
        if (tb.getHeight() != tb.getWidth()) {
            Log.w(LOGBIT, "quarter() expected square image, got " + tb.getWidth() + " x " + tb.getHeight());
            return rv;
        }
        int tileSize = tb.getWidth() / 2;  //  we assume it's even, ha ha.
        rv[0] = Bitmap.createBitmap(tb, 0, 0, tileSize, tileSize);
        rv[1] = Bitmap.createBitmap(tb, tileSize, 0, tileSize, tileSize);
        rv[2] = Bitmap.createBitmap(tb, 0, tileSize, tileSize, tileSize);
        rv[3] = Bitmap.createBitmap(tb, tileSize, tileSize, tileSize, tileSize);
        return rv;
    }

    //  This breaks a Bitmap resource into one or more sets of 9 pieces, with
    //  sets side-by-side (so you could have 3x3, or 6x3, or 9x3...).  The
    //  source image I was working from was 440 pixels for a tile, 220 for a
    //  square; I shrank them 50% for the final PNG.
    //
    //  --------------------------------------------------------------------------
    //  |        |    |        |        |    |        |        |    |        |
    //  |        |    |        |        |    |        |        |    |        |
    //  |        |    |        |        |    |        |        |    |        |
    //  |        |    |        |        |    |        |        |    |        | 2 squares
    //  |     xxx|xxxx|xxx     |     xxx|xxxx|xxx     |     xxx|xxxx|xxx     |
    //  |     xxx|xxxx|xxx     |     xxx|xxxx|xxx     |     xxx|xxxx|xxx     |
    //  --------------------------------------------------------------------------
    //  |     xxx|xxxx|xxx     |     xxx|xxxx|xxx     |     xxx|xxxx|xxx     |
    //  |     xxx|xxxx|xxx     |     xxx|xxxx|xxx     |     xxx|xxxx|xxx     | 1 square
    //  |     xxx|xxxx|xxx     |     xxx|xxxx|xxx     |     xxx|xxxx|xxx     |
    //  --------------------------------------------------------------------------
    //  |     xxx|xxxx|xxx     |     xxx|xxxx|xxx     |     xxx|xxxx|xxx     |
    //  |     xxx|xxxx|xxx     |     xxx|xxxx|xxx     |     xxx|xxxx|xxx     |
    //  |        |    |        |        |    |        |        |    |        |
    //  |        |    |        |        |    |        |        |    |        |
    //  |        |    |        |        |    |        |        |    |        |
    //  |        |    |        |        |    |        |        |    |        |
    //  ----------------------------------------------------------------------
    //  |        |                      |    |
    //  2 squares                      1 square
    private void explodeNines(int resID, Micropuls mc) {
        Bitmap tb = BitmapFactory.decodeResource(context.getResources(), resID);
        int nines = tb.getWidth() / tb.getHeight();
        mc.upperLeftS = new Bitmap[nines];
        mc.upperS = new Bitmap[nines];
        mc.upperRightS = new Bitmap[nines];
        mc.leftS = new Bitmap[nines];
        mc.centerS = new Bitmap[nines];
        mc.rightS = new Bitmap[nines];
        mc.lowerLeftS = new Bitmap[nines];
        mc.lowerS = new Bitmap[nines];
        mc.lowerRightS = new Bitmap[nines];
        int squareSize = tb.getHeight() / 5;
        int tileSize = squareSize * 2;

        for (int nine = 0; nine < nines; ++nine) {
            int s0 = nine * tb.getHeight();
            int s2 = s0 + squareSize * 2;
            int s3 = s0 + squareSize * 3;
            mc.upperLeftS[nine] =  Bitmap.createBitmap(tb, s0, s0, tileSize, tileSize);
            mc.upperS[nine] =      Bitmap.createBitmap(tb, s2, s0, squareSize, tileSize);
            mc.upperRightS[nine] = Bitmap.createBitmap(tb, s3, s0, tileSize, tileSize);
            mc.leftS[nine] =       Bitmap.createBitmap(tb, s0, s2, tileSize, squareSize);
            mc.centerS[nine] =     Bitmap.createBitmap(tb, s2, s2, squareSize, squareSize);
            mc.rightS[nine] =      Bitmap.createBitmap(tb, s3, s2, tileSize, squareSize);
            mc.lowerLeftS[nine] =  Bitmap.createBitmap(tb, s0, s3, tileSize, tileSize);
            mc.lowerS[nine] =      Bitmap.createBitmap(tb, s2, s3, squareSize, tileSize);
            mc.lowerRightS[nine] = Bitmap.createBitmap(tb, s3, s3, tileSize, tileSize);
        }
    }

    @Override
    public void drawTile(TileProvider board, int xpos, int ypos, Rect rect, boolean isSelected, Canvas canvas) {
        int oldRectLeft = rect.left;
        int oldRectRight = rect.right;
        int oldRectTop = rect.top;
        int oldRectBottom = rect.bottom;
        int sqx = xpos * 2;
        int sqy = ypos * 2;

        //  Choose a background image.  This presumes tileBGs was a 2x2 image.
        Bitmap img = tileBGs[(xpos % 2) + (((ypos % 2) == 1) ? 2 : 0)];
        canvas.drawBitmap(img, null, rect, null);

        if (board.getSquare(sqx, sqy).isBig()) {
            int tileID = board.getTileID(xpos, ypos);
            img = null;
            switch (tileID) {
                case 36: img = tile36; break;
                case 37: img = tile37; break;
                case 42: img = tile42; break;
                case 43: img = tile43; break;
            }
            if (img != null) canvas.drawBitmap(img, null, rect, null);
            ownerRenderer.drawOwners(board, sqx, sqy, rect, canvas);
        } else {
            Square sq, sq2;
            boolean vneighbor, hneighbor;
            Micropuls color;
            Bitmap[] bma;
            int squareSize = rect.width() / 2;

            //  Because of shadows etc., the order we draw the squares is
            //  upper right, upper left, lower right, lower left.
            sq = board.getSquare(sqx + 1, sqy + 1);
            if (sq.isMicropul()) {
                color = sq.isBlack() ? black : white;
                vneighbor = ((sq2 = board.getSquare(sqx + 1, sqy)).isMicropul() &&
                             (sq2.isBlack() == sq.isBlack()));
                hneighbor = ((sq2 = board.getSquare(sqx, sqy + 1)).isMicropul() &&
                             (sq2.isBlack() == sq.isBlack()));
                if (vneighbor && hneighbor) {
                    bma = color.centerS;
                    rect.set(oldRectLeft + squareSize, oldRectTop, oldRectRight, oldRectTop + squareSize);
                } else if (hneighbor) {
                    bma = color.lowerS;
                    rect.set(oldRectLeft + squareSize, oldRectTop, oldRectRight, oldRectBottom);
                } else if (vneighbor) {
                    bma = color.leftS;
                    rect.set(oldRectLeft, oldRectTop, oldRectRight, oldRectTop +  squareSize);
                } else {
                    bma = color.lowerLeftS;
                    //rect.set(oldRectLeft, oldRectTop, oldRectRight, oldRectBottom);
                }
                //XXX  Here, and below, I'm hard-coding 0, because I happen to
                //XXX  know I didn't do more than one set of micropul images.
                //XXX  Fix this.
                canvas.drawBitmap(bma[0], null, rect, null);
            } else if (sq.isCatalyst()) {
                rect.set(oldRectLeft + squareSize, oldRectTop, oldRectRight, oldRectTop + squareSize);
                drawCatalyst(board, sq, sqx + 1, sqy + 1, rect, canvas);
            }

            //  upper left.
            sq = board.getSquare(sqx, sqy + 1);
            if (sq.isMicropul()) {
                color = sq.isBlack() ? black : white;
                vneighbor = ((sq2 = board.getSquare(sqx, sqy)).isMicropul() &&
                        (sq2.isBlack() == sq.isBlack()));
                hneighbor = ((sq2 = board.getSquare(sqx + 1, sqy + 1)).isMicropul() &&
                        (sq2.isBlack() == sq.isBlack()));
                if (vneighbor && hneighbor) {
                    bma = color.centerS;
                    rect.set(oldRectLeft, oldRectTop, oldRectLeft + squareSize, oldRectTop + squareSize);
                } else if (hneighbor) {
                    bma = color.lowerS;
                    rect.set(oldRectLeft, oldRectTop, oldRectLeft + squareSize, oldRectBottom);
                } else if (vneighbor) {
                    bma = color.rightS;
                    rect.set(oldRectLeft, oldRectTop, oldRectRight, oldRectTop +  squareSize);
                } else {
                    bma = color.lowerRightS;
                    rect.set(oldRectLeft, oldRectTop, oldRectRight, oldRectBottom);
                }
                //XXX see comment above about hard-coding 0.
                canvas.drawBitmap(bma[0], null, rect, null);
            } else if (sq.isCatalyst()) {
                rect.set(oldRectLeft, oldRectTop, oldRectLeft + squareSize, oldRectTop + squareSize);
                drawCatalyst(board, sq, sqx, sqy + 1, rect, canvas);
            }

            //  lower right.
            sq = board.getSquare(sqx + 1, sqy);
            if (sq.isMicropul()) {
                color = sq.isBlack() ? black : white;
                vneighbor = ((sq2 = board.getSquare(sqx + 1, sqy + 1)).isMicropul() &&
                        (sq2.isBlack() == sq.isBlack()));
                hneighbor = ((sq2 = board.getSquare(sqx, sqy)).isMicropul() &&
                        (sq2.isBlack() == sq.isBlack()));
                if (vneighbor && hneighbor) {
                    bma = color.centerS;
                    rect.set(oldRectLeft + squareSize, oldRectTop + squareSize, oldRectRight, oldRectBottom);
                } else if (hneighbor) {
                    bma = color.upperS;
                    rect.set(oldRectLeft + squareSize, oldRectTop, oldRectRight, oldRectBottom);
                } else if (vneighbor) {
                    bma = color.leftS;
                    rect.set(oldRectLeft, oldRectTop + squareSize, oldRectRight, oldRectBottom);
                } else {
                    bma = color.upperLeftS;
                    rect.set(oldRectLeft, oldRectTop, oldRectRight, oldRectBottom);
                }
                //XXX see comment above about hard-coding 0.
                canvas.drawBitmap(bma[0], null, rect, null);
            } else if (sq.isCatalyst()) {
                rect.set(oldRectLeft + squareSize, oldRectTop + squareSize, oldRectRight, oldRectBottom);
                drawCatalyst(board, sq, sqx + 1, sqy, rect, canvas);
            }

            //  lower left.
            sq = board.getSquare(sqx, sqy);
            if (sq.isMicropul()) {
                color = sq.isBlack() ? black : white;
                vneighbor = ((sq2 = board.getSquare(sqx, sqy + 1)).isMicropul() &&
                        (sq2.isBlack() == sq.isBlack()));
                hneighbor = ((sq2 = board.getSquare(sqx + 1, sqy)).isMicropul() &&
                        (sq2.isBlack() == sq.isBlack()));
                if (vneighbor && hneighbor) {
                    bma = color.centerS;
                    rect.set(oldRectLeft, oldRectTop + squareSize, oldRectLeft + squareSize, oldRectBottom);
                } else if (hneighbor) {
                    bma = color.upperS;
                    rect.set(oldRectLeft, oldRectTop, oldRectLeft + squareSize, oldRectBottom);
                } else if (vneighbor) {
                    bma = color.rightS;
                    rect.set(oldRectLeft, oldRectTop + squareSize, oldRectRight, oldRectBottom);
                } else {
                    bma = color.upperRightS;
                    rect.set(oldRectLeft, oldRectTop, oldRectRight, oldRectBottom);
                }
                //XXX see comment above about hard-coding 0.
                canvas.drawBitmap(bma[0], null, rect, null);
            } else if (sq.isCatalyst()) {
                rect.set(oldRectLeft, oldRectTop + squareSize, oldRectLeft + squareSize, oldRectBottom);
                drawCatalyst(board, sq, sqx, sqy, rect, canvas);
            }

            rect.set(oldRectLeft, oldRectTop, oldRectRight, oldRectBottom);
            ownerRenderer.drawOwners(board, sqx, sqy, rect, canvas);
        }
        rect.set(oldRectLeft, oldRectTop, oldRectRight, oldRectBottom);
        if (isSelected) canvas.drawRect(rect, tileValidPaint);
    }

    private void drawCatalyst(TileProvider board, Square sq, int sqx, int sqy,
                              Rect rect, Canvas canvas) {
        Bitmap overlay = null;
        if (sq.getTileDraws() == 1) {
            //  hmm, this means tiles with two single catalysts use the same
            //  house image for both...
            overlay = singleCatalysts[board.getSquareTileID(sqx, sqy) % singleCatalysts.length];
        } else if (sq.getTileDraws() == 2) {
            overlay = doubleCatalysts[board.getSquareTileID(sqx, sqy) % doubleCatalysts.length];
        } else if (sq.getExtraTurns() == 1) {
            overlay = plusCatalyst[board.getSquareTileID(sqx, sqy) % singleCatalysts.length];
        } else {
            //Log.w(LOGBIT, "don't know what to do what to do with " + square);
        }
        if (overlay != null) {
            canvas.drawBitmap(overlay, null, rect, null);
        }
    }
}
