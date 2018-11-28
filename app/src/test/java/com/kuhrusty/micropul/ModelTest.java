package com.kuhrusty.micropul;

import com.kuhrusty.micropul.model.Board;
import com.kuhrusty.micropul.model.Group;
import com.kuhrusty.micropul.model.Owner;
import com.kuhrusty.micropul.model.Square;
import com.kuhrusty.micropul.model.Tile;
import com.kuhrusty.micropul.model.TilePlayResult;
//import com.kuhrusty.micropul.util.BoardPrinter;

import org.junit.Test;

//import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ModelTest {

    @Test
    public void testTile() {
        List<Tile> tiles = Tile.createTiles();
        assertEquals(48, tiles.size());
        Tile tile = tiles.get(2);
        assertEquals(Square.White, tile.getUpperLeft());
        assertEquals(Square.Black, tile.getUpperRight());
        assertEquals(Square.Empty, tile.getLowerRight());
        assertEquals(Square.PlusCatalyst, tile.getLowerLeft());
        assertEquals(0, tile.getRotation());

        tile.rotateRight();
        assertEquals(Square.PlusCatalyst, tile.getUpperLeft());
        assertEquals(Square.White, tile.getUpperRight());
        assertEquals(Square.Black, tile.getLowerRight());
        assertEquals(Square.Empty, tile.getLowerLeft());
        assertEquals(1, tile.getRotation());

        tile.rotateRight();
        assertEquals(Square.Empty, tile.getUpperLeft());
        assertEquals(Square.PlusCatalyst, tile.getUpperRight());
        assertEquals(Square.White, tile.getLowerRight());
        assertEquals(Square.Black, tile.getLowerLeft());
        assertEquals(2, tile.getRotation());

        tile.rotateRight();
        assertEquals(Square.Black, tile.getUpperLeft());
        assertEquals(Square.Empty, tile.getUpperRight());
        assertEquals(Square.PlusCatalyst, tile.getLowerRight());
        assertEquals(Square.White, tile.getLowerLeft());
        assertEquals(-1, tile.getRotation());

        tile.rotateRight();
        assertEquals(Square.White, tile.getUpperLeft());
        assertEquals(Square.Black, tile.getUpperRight());
        assertEquals(Square.Empty, tile.getLowerRight());
        assertEquals(Square.PlusCatalyst, tile.getLowerLeft());
        assertEquals(0, tile.getRotation());

        tile.rotateLeft();
        assertEquals(Square.Black, tile.getUpperLeft());
        assertEquals(Square.Empty, tile.getUpperRight());
        assertEquals(Square.PlusCatalyst, tile.getLowerRight());
        assertEquals(Square.White, tile.getLowerLeft());
        assertEquals(-1, tile.getRotation());

        tile.rotateLeft();
        assertEquals(Square.Empty, tile.getUpperLeft());
        assertEquals(Square.PlusCatalyst, tile.getUpperRight());
        assertEquals(Square.White, tile.getLowerRight());
        assertEquals(Square.Black, tile.getLowerLeft());
        assertEquals(2, tile.getRotation());

        tile.rotateLeft();
        assertEquals(Square.PlusCatalyst, tile.getUpperLeft());
        assertEquals(Square.White, tile.getUpperRight());
        assertEquals(Square.Black, tile.getLowerRight());
        assertEquals(Square.Empty, tile.getLowerLeft());
        assertEquals(1, tile.getRotation());

        tile.rotateLeft();
        assertEquals(Square.White, tile.getUpperLeft());
        assertEquals(Square.Black, tile.getUpperRight());
        assertEquals(Square.Empty, tile.getLowerRight());
        assertEquals(Square.PlusCatalyst, tile.getLowerLeft());
        assertEquals(0, tile.getRotation());
    }

    @Test
    public void testBoard() {
        List<Tile> tiles = Tile.createTiles();
        Board board = new Board();
        checkBoard(board);
        //  Create a copy of the board; after we goof with the original, we'll
        //  confirm that the original is unchanged.
        Board board2 = new Board(board);

        Tile tile = tiles.get(0);
        assertEquals(Square.White, tile.getUpperLeft());
        assertEquals(Square.Empty, tile.getUpperRight());
        assertEquals(Square.Empty, tile.getLowerRight());
        assertEquals(Square.Empty, tile.getLowerLeft());
        checkIsValidPlay(board, tile, 0, 0, "Tile overlaps existing tile.");

        checkIsValidPlay(board, tile, -1, 1, "No matching micropul.");
        checkIsValidPlay(board, tile, 0, 1, "No matching micropul.");
        checkIsValidPlay(board, tile, 1, 1, "No matching micropul.");
        checkIsValidPlay(board, tile, 1, 0, null);
        checkIsValidPlay(board, tile, 1, -1, "No matching micropul.");
        checkIsValidPlay(board, tile, 0, -1, "Conflicting micropul.");
        checkIsValidPlay(board, tile, -1, -1, "No matching micropul.");
        checkIsValidPlay(board, tile, -1, -1, "No matching micropul.");

        tile.rotateRight();
        checkIsValidPlay(board, tile, 0, 0, "Tile overlaps existing tile.");
        checkIsValidPlay(board, tile, -1, 1, "No matching micropul.");
        checkIsValidPlay(board, tile, 0, 1, "No matching micropul.");
        checkIsValidPlay(board, tile, 1, 1, "No matching micropul.");
        checkIsValidPlay(board, tile, 1, 0, "No matching micropul.");
        checkIsValidPlay(board, tile, 1, -1, "No matching micropul.");
        checkIsValidPlay(board, tile, 0, -1, null);
        checkIsValidPlay(board, tile, -1, -1, "No matching micropul.");
        checkIsValidPlay(board, tile, -1, -1, "No matching micropul.");
//XXX seems suspicious that we got a "Conflicting micropul" reason the first time,
//XXX but not the second time, when we rotated the tile.

        TilePlayResult result = board.considerResult(tile, 0, -1);
        assertNull(result);

        checkConsiderResult(board, tiles.get(10), 1, 0, 1, 0);
        checkConsiderResult(board, tiles.get(10), 0, -1, 1, 0);
        checkConsiderResult(board, tiles.get(20), 1, 0, 2, 0);
        checkConsiderResult(board, tiles.get(20), 1, 0, 2, 0);
        checkConsiderResult(board, tiles.get(2), 1, 0, 0, 1);

        checkPlayTile(board, tiles.get(2), 1, 0, 0, 1);
//BoardPrinter bp = new BoardPrinter();
//bp.print(board, System.out);
        assertEquals(1, board.getHeight());
        assertEquals(2, board.getWidth());
        assertEquals(4, board.getGroupCount());
        checkGroup(board.getGroupByID(1), 1, Owner.Nobody, 2, 2, true, false);
        checkGroup(board.getGroupByID(2), 2, Owner.Nobody, 3, 3, false, false);
        checkGroup(board.getGroupByID(3), 3, Owner.Nobody, 1, 1, true, false);
        checkGroup(board.getGroupByID(4), 0, Owner.Nobody, 0, 0, false, false);

        //  copy, because we're going to come back to this in a minute, to do
        //  things differently.
        Board board3 = new Board(board);

        //  Set the owner of group 1 to P1, the owner of group 3 to P2, play
        //  tiles which merge the group, and confirm that A) it's merged
        //  correctly, and B) it's owned by Both.
        board.playStone(board.getGroupByID(1), Owner.P1);
        board.playStone(board.getGroupByID(3), Owner.P2);
        checkGroup(board.getGroupByID(1), 1, Owner.P1, 2, 2, true, false);
        checkGroup(board.getGroupByID(2), 2, Owner.Nobody, 3, 3, false, false);
        checkGroup(board.getGroupByID(3), 3, Owner.P2, 1, 1, true, false);
        checkGroup(board.getGroupByID(4), 0, Owner.Nobody, 0, 0, false, false);
        tile = new Tile(tiles.get(17));  //  copy, as we're about to screw with it
        tile.rotateLeft();
        checkPlayTile(board, tile, 0, 1, 1, 0);
//bp.print(board, System.out);
        checkGroup(board.getGroupByID(1), 1, Owner.P1, 5, 5, true, false);
        checkGroup(board.getGroupByID(2), 2, Owner.Nobody, 3, 3, false, false);
        checkGroup(board.getGroupByID(3), 3, Owner.P2, 1, 1, true, false);
        checkGroup(board.getGroupByID(4), 0, Owner.Nobody, 0, 0, false, false);

        tile = new Tile(tiles.get(29));  //  copy, as we're about to screw with it
        tile.rotateRight();
        checkPlayTile(board, tile, 1, 1, 1, 0);
//bp.print(board, System.out);
        checkGroup(board.getGroupByID(1), 1, Owner.Both, 9, 9, true, false);
        checkGroup(board.getGroupByID(2), 2, Owner.Nobody, 4, 4, false, false);
        checkGroup(board.getGroupByID(3), 0, Owner.Nobody, 0, 0, false, false);
        checkGroup(board.getGroupByID(4), 0, Owner.Nobody, 0, 0, false, false);

        //  Now go back to board3 and play the same sequence of tiles, but this
        //  time only group 1 is owned by P1; group 3 is owned by nobody.
        board = new Board(board3);
        board.playStone(board.getGroupByID(1), Owner.P1);
        checkGroup(board.getGroupByID(1), 1, Owner.P1, 2, 2, true, false);
        checkGroup(board.getGroupByID(2), 2, Owner.Nobody, 3, 3, false, false);
        checkGroup(board.getGroupByID(3), 3, Owner.Nobody, 1, 1, true, false);
        checkGroup(board.getGroupByID(4), 0, Owner.Nobody, 0, 0, false, false);
        tile = new Tile(tiles.get(17));  //  copy, as we're about to screw with it
        tile.rotateLeft();
        checkPlayTile(board, tile, 0, 1, 1, 0);
//bp.print(board, System.out);
        checkGroup(board.getGroupByID(1), 1, Owner.P1, 5, 5, true, false);
        checkGroup(board.getGroupByID(2), 2, Owner.Nobody, 3, 3, false, false);
        checkGroup(board.getGroupByID(3), 3, Owner.Nobody, 1, 1, true, false);
        checkGroup(board.getGroupByID(4), 0, Owner.Nobody, 0, 0, false, false);

        tile = new Tile(tiles.get(29));  //  copy, as we're about to screw with it
        tile.rotateRight();
        checkPlayTile(board, tile, 1, 1, 1, 0);
//bp.print(board, System.out);
        checkGroup(board.getGroupByID(1), 1, Owner.P1, 9, 9, true, false);
        checkGroup(board.getGroupByID(2), 2, Owner.Nobody, 4, 4, false, false);
        checkGroup(board.getGroupByID(3), 0, Owner.Nobody, 0, 0, false, false);
        checkGroup(board.getGroupByID(4), 0, Owner.Nobody, 0, 0, false, false);

        //  Now go back to board3 again and play the same sequence of tiles, but
        //  this time only group 3 is owned by P1; group 1 is owned by nobody.
        board = new Board(board3);
        board.playStone(board.getGroupByID(3), Owner.P1);
        checkGroup(board.getGroupByID(1), 1, Owner.Nobody, 2, 2, true, false);
        checkGroup(board.getGroupByID(2), 2, Owner.Nobody, 3, 3, false, false);
        checkGroup(board.getGroupByID(3), 3, Owner.P1, 1, 1, true, false);
        checkGroup(board.getGroupByID(4), 0, Owner.Nobody, 0, 0, false, false);
        tile = new Tile(tiles.get(17));  //  copy, as we're about to screw with it
        tile.rotateLeft();
        checkPlayTile(board, tile, 0, 1, 1, 0);
//bp.print(board, System.out);
        checkGroup(board.getGroupByID(1), 1, Owner.Nobody, 5, 5, true, false);
        checkGroup(board.getGroupByID(2), 2, Owner.Nobody, 3, 3, false, false);
        checkGroup(board.getGroupByID(3), 3, Owner.P1, 1, 1, true, false);
        checkGroup(board.getGroupByID(4), 0, Owner.Nobody, 0, 0, false, false);

        tile = new Tile(tiles.get(29));  //  copy, as we're about to screw with it
        tile.rotateRight();
        checkPlayTile(board, tile, 1, 1, 1, 0);
//bp.print(board, System.out);
        checkGroup(board.getGroupByID(1), 1, Owner.P1, 9, 9, true, false);
        checkGroup(board.getGroupByID(2), 2, Owner.Nobody, 4, 4, false, false);
        checkGroup(board.getGroupByID(3), 0, Owner.Nobody, 0, 0, false, false);
        checkGroup(board.getGroupByID(4), 0, Owner.Nobody, 0, 0, false, false);

        //  Now let's see group 2 get closed.
        tile = tiles.get(27);
        checkPlayTile(board, tile, 0, -1, 0, 0);
//bp.print(board, System.out);
        checkGroup(board.getGroupByID(1), 1, Owner.P1, 10, 10,true, false);
        checkGroup(board.getGroupByID(2), 2, Owner.Nobody, 4, 4, false, true);
        checkGroup(board.getGroupByID(3), 3, Owner.Nobody, 1, 1, true, false);
        checkGroup(board.getGroupByID(4), 0, Owner.Nobody, 0, 0, false, false);

        //  Now let's slap down a tile with a new group which is closed.
        tile = new Tile(tiles.get(3));  //  copy, as we're about to screw with it
        tile.rotateLeft();
        checkPlayTile(board, tile, 1, 0, 0, 1);
//bp.print(board, System.out);
        checkGroup(board.getGroupByID(1), 1, Owner.P1, 10, 10, true, false);
        checkGroup(board.getGroupByID(2), 2, Owner.Nobody, 4, 4, false, true);
        checkGroup(board.getGroupByID(3), 3, Owner.Nobody, 2, 2, true, false);
        checkGroup(board.getGroupByID(4), 4, Owner.Nobody, 1, 1, false, true);
        checkGroup(board.getGroupByID(5), 0, Owner.Nobody, 0, 0, false, false);

        //  Do we handle big micropuls?
        tile = tiles.get(43);
        checkPlayTile(board, tile, 1, -1, 1, 1);
//bp.print(board, System.out);
        checkGroup(board.getGroupByID(1), 1, Owner.P1, 10, 10, true, false);
        checkGroup(board.getGroupByID(2), 2, Owner.Nobody, 4, 4, false, true);
        checkGroup(board.getGroupByID(3), 3, Owner.Nobody, 6, 3, true, false);
        checkGroup(board.getGroupByID(4), 4, Owner.Nobody, 1, 1, false, true);
        checkGroup(board.getGroupByID(5), 0, Owner.Nobody, 0, 0, false, false);

        tile = tiles.get(37);
        checkPlayTile(board, tile, 0, 0, 1, 1);
//bp.print(board, System.out);
        checkGroup(board.getGroupByID(1), 1, Owner.P1, 10, 10, true, false);
        checkGroup(board.getGroupByID(2), 2, Owner.Nobody, 4, 4, false, true);
        checkGroup(board.getGroupByID(3), 3, Owner.Nobody, 10, 4, true, false);
        checkGroup(board.getGroupByID(4), 4, Owner.Nobody, 1, 1, false, true);
        checkGroup(board.getGroupByID(5), 0, Owner.Nobody, 0, 0, false, false);

        //  Now back to checking that very early copy.
        checkBoard(board2);
    }

    @Test
    public void testIssue6() {
        List<Tile> tiles = Tile.createTiles();
        Board board = new Board();
        board.playTile(tiles.get(40), 0, 0);
        assertEquals(1, board.getHeight());
        assertEquals(1, board.getWidth());
        assertEquals(3, board.getGroupCount());
        checkGroup(board.getGroupByID(1), 1, Owner.Nobody, 2, 2, false, false);
        checkGroup(board.getGroupByID(2), 2, Owner.Nobody, 2, 2, true, false);
        checkGroup(board.getGroupByID(3), 0, Owner.Nobody, 0, 0, false, false);

        Tile tile = new Tile(tiles.get(14));
        tile.rotateRight();
        assertEquals(Square.Empty, tile.getUpperLeft());
        assertEquals(Square.White, tile.getUpperRight());
        assertEquals(Square.White, tile.getLowerRight());
        assertEquals(Square.SingleCatalyst, tile.getLowerLeft());
        checkIsValidPlay(board, tile, 0, 1, null);
        checkPlayTile(board, tile, 0, 1, 1, 0);
        assertEquals(2, board.getHeight());
        assertEquals(1, board.getWidth());
        assertEquals(3, board.getGroupCount());
        checkGroup(board.getGroupByID(1), 1, Owner.Nobody, 4, 4, false, false);
        checkGroup(board.getGroupByID(2), 2, Owner.Nobody, 2, 2, true, false);
        checkGroup(board.getGroupByID(3), 0, Owner.Nobody, 0, 0, false, false);

        tile = tiles.get(24);
        assertEquals(Square.White, tile.getUpperLeft());
        assertEquals(Square.SingleCatalyst, tile.getUpperRight());
        assertEquals(Square.SingleCatalyst, tile.getLowerRight());
        assertEquals(Square.Empty, tile.getLowerLeft());
        checkIsValidPlay(board, tile, 1, 1, null);
        checkPlayTile(board, tile, 1, 1, 0, 0);
        assertEquals(2, board.getHeight());
        assertEquals(2, board.getWidth());
        assertEquals(3, board.getGroupCount());
        checkGroup(board.getGroupByID(1), 1, Owner.Nobody, 5, 5, false, false);
        checkGroup(board.getGroupByID(2), 2, Owner.Nobody, 2, 2, true, false);
        checkGroup(board.getGroupByID(3), 0, Owner.Nobody, 0, 0, false, false);

        tile = tiles.get(2);
        assertEquals(Square.White, tile.getUpperLeft());
        assertEquals(Square.Black, tile.getUpperRight());
        assertEquals(Square.Empty, tile.getLowerRight());
        assertEquals(Square.PlusCatalyst, tile.getLowerLeft());
        checkIsValidPlay(board, tile, 1, 0, null);
        checkPlayTile(board, tile, 1, 0, 1, 1);
        assertEquals(2, board.getHeight());
        assertEquals(2, board.getWidth());
        assertEquals(4, board.getGroupCount());
        checkGroup(board.getGroupByID(1), 1, Owner.Nobody, 6, 6, false, false);
        checkGroup(board.getGroupByID(2), 2, Owner.Nobody, 2, 2, true, false);
        checkGroup(board.getGroupByID(3), 3, Owner.Nobody, 1, 1, true, false);
        checkGroup(board.getGroupByID(4), 0, Owner.Nobody, 0, 0, false, false);
    }

    private void checkGroup(Group group, int expectID, Owner expectOwner,
                            int expectSize, int expectValue, boolean expectBlack,
                            boolean expectClosed) {
        assertEquals(expectID, group.getID());
        assertEquals(expectOwner, group.getOwner());
        assertEquals(expectSize, group.getSize());
        assertEquals(expectValue, group.getPointValue());
        assertEquals(expectBlack, group.isBlack());
        assertEquals(expectClosed, group.isClosed());
    }

    private void checkBoard(Board board) {
        assertEquals(1, board.getHeight());
        assertEquals(1, board.getWidth());
        assertEquals(Square.White, board.getSquare(1, 0));
        assertEquals(Square.White, board.getSquare(1, 1));
        assertEquals(Square.Black, board.getSquare(0, 0));
        assertEquals(Square.Black, board.getSquare(0, 1));
        assertEquals(Square.Null, board.getSquare(-1, 1));
        assertEquals(Square.Null, board.getSquare(1, -1));
        assertEquals(Square.Null, board.getSquare(1000, -1000));

        assertEquals(Owner.Nobody, board.getOwner(1, 0));
        assertEquals(Owner.Nobody, board.getOwner(1, 1));
        assertEquals(Owner.Nobody, board.getOwner(0, 0));
        assertEquals(Owner.Nobody, board.getOwner(0, 1));
        assertEquals(Owner.Nobody, board.getOwner(-1, 1));
        assertEquals(Owner.Nobody, board.getOwner(1, -1));
        assertEquals(Owner.Nobody, board.getOwner(1000, -1000));
        assertEquals(Owner.Nobody, board.getOwner(1000, -1000));

        assertEquals(2, board.getGroup(1, 0).getID());
        assertEquals(2, board.getGroup(1, 1).getID());
        assertEquals(1, board.getGroup(0, 0).getID());
        assertEquals(1, board.getGroup(0, 1).getID());
        assertEquals(0, board.getGroup(-1, 1).getID());
        assertEquals(0, board.getGroup(1, -1).getID());
        assertEquals(0, board.getGroup(1000, -1000).getID());
        assertEquals(0, board.getGroup(1000, -1000).getID());
    }

    private void checkIsValidPlay(Board board, Tile tile, int xpos, int ypos, String expectReason) {
        if (expectReason != null) {
            StringBuilder err = new StringBuilder();
            assertFalse(board.isValidPlay(tile, xpos, ypos, err));
            assertEquals(expectReason, err.toString());
        } else {
            assertTrue(board.isValidPlay(tile, xpos, ypos, null));
        }
    }
    private void checkConsiderResult(Board board, Tile tile, int xpos, int ypos, int tiles, int turns) {
        TilePlayResult result = board.considerResult(tile, xpos, ypos);
        assertNotNull(result);
        assertEquals(tiles, result.getTiles());
        assertEquals(turns, result.getExtraTurns());
    }
    private void checkPlayTile(Board board, Tile tile, int xpos, int ypos, int tiles, int turns) {
        StringBuilder err = new StringBuilder();
        boolean rv = board.isValidPlay(tile, xpos, ypos, err);
        assertEquals("", err.toString());
        assertTrue(rv);
        TilePlayResult result = board.playTile(tile, xpos, ypos);
        if ((tiles > 0) || (turns > 0)) {
            assertNotNull(result);
            assertEquals(tiles, result.getTiles());
            assertEquals(turns, result.getExtraTurns());
        } else {
            assertNull(result);
        }
    }
}