package com.kuhrusty.micropul.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.kuhrusty.micropul.model.Owner;
import com.kuhrusty.micropul.model.TileProvider;

/**
 * This fills owned groups with squares using the given Paint.
 */
public class SolidFillOwnerRenderer implements OwnerRenderer {
    private Paint p1Paint;
    private Paint p2Paint;
    private Paint bothPaint;

    /**
     * @param p1Paint will be used to paint a crude square over groups owned by
     *                player 1.
     * @param p2Paint will be used to paint a crude square over groups owned by
     *                player 2.
     * @param bothPaint will be used to paint a crude square over groups owned
     *                  by both players.
     */
    public SolidFillOwnerRenderer(Paint p1Paint, Paint p2Paint, Paint bothPaint) {
        this.p1Paint = p1Paint;
        this.p2Paint = p2Paint;
        this.bothPaint = bothPaint;
    }

    @Override
    public void drawOwners(TileProvider tp, int sqx, int sqy, Rect rect, Canvas canvas) {
        Paint paint;
        if (tp.getSquare(sqx, sqy).isBig()) {
            paint = ownerToPaint(tp.getOwner(sqx, sqy));
            if (paint != null) {
                canvas.drawRect(rect, paint);
            }
            return;
        }
        float p2 = rect.height() / 2;
        if (tp.getSquare(sqx, sqy + 1).isMicropul() &&
                ((paint = ownerToPaint(tp.getOwner(sqx, sqy + 1))) != null)) {
            canvas.drawRect(rect.left, rect.top, rect.left + p2, rect.top + p2, paint);
        }
        if (tp.getSquare(sqx + 1, sqy + 1).isMicropul() &&
                ((paint = ownerToPaint(tp.getOwner(sqx + 1, sqy + 1))) != null)) {
            canvas.drawRect(rect.left + p2, rect.top, rect.right, rect.top + p2, paint);
        }
        if (tp.getSquare(sqx, sqy).isMicropul() &&
                ((paint = ownerToPaint(tp.getOwner(sqx, sqy))) != null)) {
            canvas.drawRect(rect.left, rect.top + p2, rect.left + p2, rect.bottom, paint);
        }
        if (tp.getSquare(sqx + 1, sqy).isMicropul() &&
                ((paint = ownerToPaint(tp.getOwner(sqx + 1, sqy))) != null)) {
            canvas.drawRect(rect.left + p2, rect.top + p2, rect.right, rect.bottom, paint);
        }
    }

    private Paint ownerToPaint(Owner owner) {
        if ((owner == null) || owner.equals(Owner.Nobody)) return null;
        if (owner.equals(Owner.P1)) return p1Paint;
        if (owner.equals(Owner.P2)) return p2Paint;
        if (owner.equals(Owner.Both)) return bothPaint;
        //Log.w(LOGBIT, "unhandled owner " + owner);
        return null;
    }
}
