package com.kuhrusty.micropul.bot;

import android.content.res.Resources;

import com.kuhrusty.micropul.MoveListener;
import com.kuhrusty.micropul.R;
import com.kuhrusty.micropul.model.Board;
import com.kuhrusty.micropul.model.Player;
import com.kuhrusty.micropul.model.Tile;

/**
 * The dumbest possible AI: makes the first move it can find.
 */
public class DumbBot implements Bot {

    public DumbBot(Resources res) {
        this.res = res;
    }

    @Override
    public String getName() {
        return res.getString(R.string.dumbbot_name);
    }

    @Override
    public String getDescription() {
        return res.getString(R.string.dumbbot_descr);
    }

    @Override
    public void takeTurn(Player self, Player opponent, Board board, MoveListener listener) {
        if (self.getTilesInHand() == 0) {
            if (self.getTilesInSupply() > 0) {
                listener.drawTile(self);
            } else {
                listener.concede(self, null);
            }
            return;
        }
        for (Tile tile : self.getTiles()) {
            for (int rotation = 0; rotation < 4; ++rotation) {
                for (int ypos = board.getHeight(); ypos >= -1; --ypos) {
                    for (int xpos = -1; xpos <= board.getWidth(); ++xpos) {
                        if (board.isValidPlay(tile, xpos, ypos, null)) {
                            listener.playTile(self, tile, xpos, ypos);
                            return;
                        }
                    }
                }
                //  still here, so try rotating it & trying again.
                tile.rotateRight();
            }
        }
        //  still here, so try drawing a new tile.
        if (self.getTilesInSupply() > 0) {
            listener.drawTile(self);
        } else {
            listener.concede(self, null);  //  gurkk!!
        }
    }

    private Resources res;
}
