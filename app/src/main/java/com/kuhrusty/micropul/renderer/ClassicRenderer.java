package com.kuhrusty.micropul.renderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;

import com.kuhrusty.micropul.R;

/**
 * This uses micropul-Game-1.0.jpg for its tile image.
 */
public class ClassicRenderer extends BaseImageRenderer {

    public ClassicRenderer(Context context) {
        super(context, context.getResources().getString(R.string.classic_renderer_name),
                R.drawable.preview_classic, R.drawable.micropul_game_1_0);
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
        p1GroupPaint = newPaint(Color.RED, 63);
        p2GroupPaint = newPaint(Color.BLUE, 63);
        bothGroupPaint = newPaint(0xff00ff, 63);
    }
}
