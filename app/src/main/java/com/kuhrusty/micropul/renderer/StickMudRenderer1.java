package com.kuhrusty.micropul.renderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import com.kuhrusty.micropul.R;

/**
 * This uses Archinerd's hand-painted Stick/Mud Farmer re-theme image for its
 * tiles.
 */
public class StickMudRenderer1 extends BaseImageRenderer {

    public StickMudRenderer1(Context context) {
        super(context, context.getResources().getString(R.string.stick_mud_renderer_name),
                R.drawable.preview_stick1, R.drawable.stick_tiles);
    }

    @Override
    public void prepare() {
        super.prepare();
        bgPaint = newPaint(Color.GRAY);
        bgGridPaint = newPaint(Color.LTGRAY, Paint.Style.STROKE);
        bgGridPaint.setStrokeWidth(1.0f);
        tileValidPaint = newPaint(Color.YELLOW, 63);
        p1StonePaint = newPaint(Color.RED);
        p2StonePaint = newPaint(Color.BLUE);
        ownerRenderer = new BorderOwnerRenderer(newPaint(Color.RED, 127),
                newPaint(Color.BLUE, 127), newPaint(Color.DKGRAY, 127));
    }
}
