package com.kuhrusty.micropul.bot;

import android.content.res.Resources;

import com.kuhrusty.micropul.MoveListener;
import com.kuhrusty.micropul.R;
import com.kuhrusty.micropul.model.Board;
import com.kuhrusty.micropul.model.Group;
import com.kuhrusty.micropul.model.Opponent;
import com.kuhrusty.micropul.model.Owner;
import com.kuhrusty.micropul.model.Player;
import com.kuhrusty.micropul.model.Tile;
import com.kuhrusty.micropul.model.TilePlayResult;

/**
 * Draws a tile if it can; then makes the move which increases its supply the
 * most (although it doesn't consider plus catalysts, and doesn't try to expand
 * opponent's groups).  Will also make a halfhearted attempt to claim closed
 * groups.
 */
public class OptiBot implements Bot {

    public OptiBot(Resources res) {
        this.res = res;
    }

    @Override
    public String getName() {
        return res.getString(R.string.optibot_name);
    }

    @Override
    public String getDescription() {
        return res.getString(R.string.optibot_descr);
    }

    private static class TilePlay {
        void set(Tile tile, int xpos, int ypos, TilePlayResult result) {
            tileID = tile.getID();
            rotation = tile.getRotation();
            this.xpos = xpos;
            this.ypos = ypos;
            if (result != null) {
                extraTiles = result.getTiles();
                extraTurn = result.getExtraTurns() > 0;
            } else {
                extraTiles = 0;
                extraTurn = false;
            }
        }
        boolean isBetter(TilePlayResult other) {
            return (other != null) &&
                   ((other.getTiles() > extraTiles) ||
                    ((other.getTiles() == extraTiles) && (extraTurn == false) &&
                     (other.getExtraTurns() > 0)));
        }
        int tileID;
        int rotation;
        int xpos;
        int ypos;
        int extraTiles;
        boolean extraTurn;
    }

    @Override
    public void takeTurn(Player self, Opponent opponent, Board board, MoveListener listener) {
        //  Always draw a tile if we can.
        if ((self.getTilesInHand() < 6) && (self.getTilesInSupply() > 0)) {
            listener.drawTile(self);
            return;
        }

        //  Checking for unclaimed, closed groups is fast...
        int bestGroup = -1;
        int bestGroupScore = 0;
        if (self.getStonesRemaining() > 0) {
            for (int ii = 0; ii < board.getGroupCount(); ++ii) {
                Group tg = board.getGroupByID(ii);
                if (tg.isClosed() && (tg.getOwner().equals(Owner.Nobody)) &&
                        (tg.getPointValue() > bestGroupScore)) {
                    bestGroup = ii;
                    bestGroupScore = tg.getPointValue();
                }
            }
        }

        TilePlay best = null;

        for (Tile tile : self.getTiles()) {
            for (int rotation = 0; rotation < 4; ++rotation) {
                for (int ypos = board.getHeight(); ypos >= -1; --ypos) {
                    for (int xpos = -1; xpos <= board.getWidth(); ++xpos) {
                        if (board.isValidPlay(tile, xpos, ypos, null)) {
                            TilePlayResult tpr = board.considerResult(tile, xpos, ypos);
                            if (best == null) {
                                //  first valid move we've found
                                best = new TilePlay();
                                best.set(tile, xpos, ypos, tpr);  //  tpr may be null
                            } else if (best.isBetter(tpr)) {
                                best.set(tile, xpos, ypos, tpr);  //  tpr may be null
                            }
                        }
                    }
                }
                tile.rotateRight();
            }
        }

        if (best == null) {
            //  did we have a stone play?
            if (bestGroup != -1) {
                listener.placeStone(self, bestGroup);
            } else {
                listener.concede(self, "Can't figure out a move!");
            }
            return;
        }

        //  If the best tile's change in score equals the best stone's, we
        //  should see whether the opponent has stones or tiles in hand.
        int tileScore = best.extraTiles * 2 - 1;
        if (bestGroup != -1) {
            if (bestGroupScore == tileScore) {
                //  If they don't have any stones left, no hurry.
                if (opponent.getStonesRemaining() == 0) bestGroup = -1;
            } else if (bestGroupScore < tileScore) {
                bestGroup = -1;
            }
        }
        if (bestGroup != -1) {
            //  OK, we think this was the better move.
            listener.placeStone(self, bestGroup);
            return;
        }

        Tile tt = self.removeTileByID(best.tileID);
        if (tt == null) {
            listener.concede(self, "Confused!");
        }
        while (tt.getRotation() != best.rotation) {
            tt.rotateRight();  //  probably a better way to do this
        }
        listener.playTile(self, tt, best.xpos, best.ypos);
    }

    private Resources res;
}
