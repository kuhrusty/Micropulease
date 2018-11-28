package com.kuhrusty.micropul.util;

import com.kuhrusty.micropul.model.Board;
import com.kuhrusty.micropul.model.Group;
import com.kuhrusty.micropul.model.Square;
import com.kuhrusty.micropul.model.TilePlayResult;

import java.io.PrintStream;

/**
 * Garbage class for dumping out a text representation of a Board.
 */
public class BoardPrinter {
    public void print(Board board, PrintStream out) {
        out.println("===== " + board.getWidth() + " x " + board.getHeight() + ":");
        int cols = board.getWidth() * 2;
        for (int row = board.getHeight() * 2 - 1; row >= 0; --row) {
            for (int col = 0; col < cols; ++col) {
                Square sq = board.getSquare(col, row);
                String ts = null;
                if (sq.equals(Square.Black)) ts = " _  ";
                else if (sq.equals(Square.White)) ts = " _  ";
                else if (sq.isBig()) {
                    if ((col % 2) == 0) {
                        if ((row % 2) == 1) {
                            //  upper left
                            ts = " ___";
                        } else {
                            String tc = (sq.getTileDraws() == 1) ? "." : "+";
                            //  lower left
                            ts = (sq.isBlack() ? "B" : "W") + "  " + tc;
                        }
                    } else {
                        if ((row % 2) == 1) {
                            //  upper right
                            ts = "__  ";
                        } else {
                            //  lower right
                            ts = sq.isBlack() ? "  B " : "  W ";
                        }
                    }
                } else if (sq.equals(Square.SingleCatalyst)) ts = "    ";
                else if (sq.equals(Square.DoubleCatalyst)) ts = "    ";
                else if (sq.equals(Square.PlusCatalyst)) ts = "    ";
                else if (sq.equals(Square.Empty)) ts = "    ";
                else if (sq.equals(Square.Null)) ts = "____";
                if (ts == null) {
                    throw new RuntimeException("brain damage, unhandled Square " + sq);
                }
                out.print(ts);
            }
            out.println();
            for (int col = 0; col < cols; ++col) {
                Square sq = board.getSquare(col, row);
                String ts = null;
                if (sq.equals(Square.Black)) ts = "(B) ";
                else if (sq.equals(Square.White)) ts = "(W) ";
                else if (sq.isBig()) {
                    if ((col % 2) == 0) {
                        if ((row % 2) == 1) {
                            //  upper left
                            ts = sq.isBlack() ? "/BBB" : "/WWW";
                        } else {
                            //  lower left
                            ts = sq.isBlack() ? "\\BBB" : "\\WWW";
                        }
                    } else {
                        if ((row % 2) == 1) {
                            //  upper right
                            ts = sq.isBlack() ? "BB\\ " : "WW\\ ";
                        } else {
                            //  lower right
                            ts = sq.isBlack() ? "BB/ " : "WW/ ";
                        }
                    }
                } else if (sq.equals(Square.SingleCatalyst)) ts = " .  ";
                else if (sq.equals(Square.DoubleCatalyst)) ts = " :  ";
                else if (sq.equals(Square.PlusCatalyst)) ts = " +  ";
                else if (sq.equals(Square.Empty)) ts = "    ";
                else if (sq.equals(Square.Null)) ts = "____";
                if (ts == null) {
                    throw new RuntimeException("brain damage, unhandled Square " + sq);
                }
                out.print(ts);
            }
            out.println();
        }

        out.println("Groups:");
        for (int row = board.getHeight() * 2 - 1; row >= 0; --row) {
            for (int col = 0; col < cols; ++col) {
                Group gr = board.getGroup(col, row);
                if (gr.equals(Group.None)) {
                    out.print("   ");
                } else if (gr.getID() < 10) {
                    out.print(" " + gr.getID() + " ");
                } else {
                    out.print(gr.getID() + " ");
                }
            }
            out.println();
        }
        for (int ii = 0; ii < board.getGroupCount(); ++ii) {
            Group group = board.getGroupByID(ii);
            out.println("Group " + ii + ": id " + group.getID() + ", owner " +
                    group.getOwner() + ", size " + group.getSize() + ", black " +
                    group.isBlack() + ", closed " + group.isClosed());
        }
    }

    /**
     * result may be null.
     */
    public void print(TilePlayResult result, PrintStream out) {
        if ((result != null) && (result.catalystsActivated())) {
            out.print("Result: take ");
            if (result.getTiles() > 0) {
                out.print(result.getTiles() + " tile" + (result.getTiles() > 1 ? "s" : ""));
                if (result.getExtraTurns() > 0) out.print(" and ");
            }
            if (result.getExtraTurns() > 0) {
                out.print(result.getExtraTurns() + " extra turn" + (result.getExtraTurns() > 1 ? "s" : ""));
            }
            out.println();
        } else {
            out.println("Result: null");
        }
    }
}
