package com.kuhrusty.micropul.bot;

import android.content.res.Resources;
import android.util.Log;

import com.kuhrusty.micropul.MoveListener;
import com.kuhrusty.micropul.R;
import com.kuhrusty.micropul.model.Board;
import com.kuhrusty.micropul.model.Group;
import com.kuhrusty.micropul.model.Owner;
import com.kuhrusty.micropul.model.Player;
import com.kuhrusty.micropul.model.Tile;
import com.kuhrusty.micropul.model.TilePlayResult;

/**
 * This one might be kind of expensive, for only looking at single moves; it
 * has the board recalculate the players' scores after each move, and looks at
 * that (which should keep it from closing opponent groups).
 */
public class OptiBot2 implements Bot {
    private static final String LOGBIT = "OptiBot2";

    public OptiBot2(Resources res) {
        this.res = res;
    }

    @Override
    public String getName() {
        return res.getString(R.string.optibot2_name);
    }

    @Override
    public String getDescription() {
        return res.getString(R.string.optibot2_descr);
    }

    private static class TilePlay {
        void set(Tile tile, int xpos, int ypos, int relative) {
            tileID = tile.getID();
            rotation = tile.getRotation();
            this.xpos = xpos;
            this.ypos = ypos;
            this.relativeScore = relative;
        }
        int tileID;
        int rotation;
        int xpos;
        int ypos;
        int relativeScore;
    }

    @Override
    public void takeTurn(Player self, Player opponent, Board board, MoveListener listener) {
        boolean canDrawTile = (self.getTilesInHand() < 6) && (self.getTilesInSupply() > 0);
        int originalScore = self.calculateScore(board);
        int originalOpponentScore = opponent.calculateScore(board);
        Log.d(LOGBIT, "takeTurn(), canDrawTile " + canDrawTile +
                ", scores " + originalScore + " - " + originalOpponentScore);

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
                    Log.d(LOGBIT, "group " + bestGroup + " is worth " + bestGroupScore);
                }
            }
        }

        TilePlay best = null;

        for (Tile tile : self.getTiles()) {
            for (int rotation = 0; rotation < 4; ++rotation) {
                for (int ypos = board.getHeight(); ypos >= -1; --ypos) {
                    for (int xpos = -1; xpos <= board.getWidth(); ++xpos) {
                        if (board.isValidPlay(tile, xpos, ypos, null)) {
                            Log.d(LOGBIT, "considering tile " + tile.getID() +
                                    ", rotation " + tile.getRotation() + ", at " +
                                    xpos + ", " + ypos);
                            Board proposed = new Board(board);
                            TilePlayResult tpr = proposed.playTile(tile, xpos, ypos);
                            //  -1 because of the tile we're playing.
                            int mydiff = self.calculateScore(proposed) - originalScore - 1;
                            int opdiff = opponent.calculateScore(proposed) - originalOpponentScore;
                            if (tpr != null) {
                                //  Board.playTile() doesn't update the Player's
                                //  supply, so account for those points here.
                                mydiff += 2 * tpr.getTiles();
                            }
                            int relative = mydiff - opdiff;
                            Log.d(LOGBIT, "  mydiff " + mydiff + ", opdiff " +
                                    opdiff + ", relative " + relative);
                            if (best == null) {
                                //  first valid move we've found
                                Log.d(LOGBIT, "    first valid move found, keeping it");
                                best = new TilePlay();
                                best.set(tile, xpos, ypos, relative);
                            } else if (best.relativeScore < relative) {
                                Log.d(LOGBIT, "    beats previous best " + best.relativeScore);
                                best.set(tile, xpos, ypos, relative);
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
                Log.d(LOGBIT, "DECIDED: no tile play, placing stone for " +
                        bestGroupScore + " points");
                listener.placeStone(self, bestGroup);
            } else if (canDrawTile) {
                Log.d(LOGBIT, "DECIDED: no tile play, no stone play, drawing tile");
                listener.drawTile(self);
            } else {
                listener.concede(self, "Can't figure out a move!");
            }
            return;
        }

        //  If we're only going to get one point out of this, and we *can* draw
        //  a tile, do that instead.
        if (canDrawTile && (best.relativeScore <= 1) &&
            ((bestGroup == -1) || (bestGroupScore == 1))) {
            Log.d(LOGBIT, "DECIDED: best tile play is " + best.relativeScore +
                    ", no group play worth more than 1, drawing tile");
            listener.drawTile(self);
            return;
        }

        //  If the best tile's change in score equals the best stone's, we
        //  should see whether the opponent has stones or tiles in hand.
        if (bestGroup != -1) {
            if (bestGroupScore == best.relativeScore) {
                //  If they don't have any stones left, no hurry.
                if (opponent.getStonesRemaining() == 0) bestGroup = -1;
            } else if (bestGroupScore < best.relativeScore) {
                bestGroup = -1;
            }
        }
        if (bestGroup != -1) {
            //  OK, we think this was the better move.
            Log.d(LOGBIT, "DECIDED: best tile play is " + best.relativeScore +
                    ", placing stone on group " + bestGroup + " for " + bestGroupScore);
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
        Log.d(LOGBIT, "DECIDED: best tile play is " + best.relativeScore +
                ", playing tile " + tt.getID() + " rotation " + tt.getRotation() +
                " at " + best.xpos + ", " + best.ypos);
        listener.playTile(self, tt, best.xpos, best.ypos);
    }

    private Resources res;
}
