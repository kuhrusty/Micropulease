package com.kuhrusty.micropul.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.kuhrusty.micropul.model.TileProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * This takes a singe image, splits it up into tiles, and draws those on the
 * screen.
 */
public abstract class BaseImageRenderer extends BaseBoardPlus2Renderer {
    private static final String LOGBIT = "BaseImageRenderer";

    private Context context;
    private int tileImageID;
    private List<Bitmap> tiles;

    /**
     * @param context must not be null.  Used for loading images.
     * @param name the name to display in the "start game" activity.
     * @param previewID the resource ID of a preview image to display in the
     *                  "start game" activity, or 0 or R.drawable.preview_none
     *                  for a default "no preview" image.
     * @param tileImageID must be the resource ID of an image which has the
     *                    tiles in the same arrangement as micropul-Game-1.0.jpg.
     */
    public BaseImageRenderer(Context context, String name, int previewID,
                             int tileImageID) {
        super(name, previewID);
        this.context = context;
        this.tileImageID = tileImageID;
    }

    @Override
    public void prepare() {
        BitmapFactory.Options bfo = new BitmapFactory.Options();
        bfo.inScaled = false;  //  issue #14
        Bitmap allTiles = BitmapFactory.decodeResource(context.getResources(), tileImageID, bfo);
        tiles = imageToTiles(allTiles);
    }

    /**
     * Assumes the given Bitmap is a 6 x 8 image of tiles IN THE SAME ORDER AS
     * micropul-Game-1.0.jpg, and splits it up into 48 squares.
     */
    private List<Bitmap> imageToTiles(Bitmap allTiles) {
        List<Bitmap> rv = new ArrayList<>(48);
        //  One reason to use the width instead of the height is that the
        //  original image has extra stuff at the bottom which we want to trim
        //  off.
        int tileSize = allTiles.getWidth() / 6;
        for (int row = 0; row < 8; ++row) {
            for (int col = 0; col < 6; ++col) {
                rv.add(Bitmap.createBitmap(allTiles, col * tileSize, row * tileSize, tileSize, tileSize));
            }
        }
        return rv;
    }

    @Override
    public void drawTile(TileProvider board, int xpos, int ypos, Rect rect, boolean isSelected, Canvas canvas) {
        int tileID = board.getTileID(xpos, ypos);
        int rotation = board.getTileRotation(xpos, ypos);
        int oldRectLeft = rect.left;
        int oldRectTop = rect.top;

        Bitmap tb = ((tileID >= 0) && (tileID < tiles.size())) ? tiles.get(tileID) : null;
        if (tb == null) return;
        if (rotation != 0) {
            canvas.save();
            if (rotation == -1) {
                canvas.translate(rect.left, rect.top + rect.height());
                canvas.rotate(-90f);
            } else if (rotation == 1) {
                canvas.translate(rect.left + rect.width(), rect.top);
                canvas.rotate(90f);
            } else if (rotation == 2) {
                canvas.translate(rect.left + rect.width(), rect.top + rect.height());
                canvas.rotate(180f);
            } // else brain damage

            //  We translated, so slide the rect to the origin.
            rect.right -= rect.left;
            rect.left = 0;
            rect.bottom -= rect.top;
            rect.top = 0;
        }
        canvas.drawBitmap(tb, null, rect, null);
        if (isSelected) canvas.drawRect(rect, tileValidPaint);
        if (rotation != 0) {
            canvas.restore();
            //  put this back for drawGroups()
            rect.set(oldRectLeft, oldRectTop, rect.right + oldRectLeft, rect.bottom + oldRectTop);
        }
        drawGroups(board, xpos * 2, ypos * 2, rect, canvas);
    }
}
