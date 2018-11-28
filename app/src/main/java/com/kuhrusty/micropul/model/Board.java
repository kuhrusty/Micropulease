package com.kuhrusty.micropul.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This just has the current board state.
 *
 * <p>Note that some methods take their coordinates in <i>tiles,</i> while
 * others take their coordinates in <i>squares;</i> each tile is made up of
 * four squares.  (0, 0) is always the lower-left corner, so square (4, 2) is
 * the lower left corner of tile (2, 1).</p>
 */
public class Board implements TileProvider {

    //  So, here's my thinking on whether to store one 2D array of SquareAndGroup
    //  objects vs. one 2D array of Squares and a second 2D array of Groups.
    //  My claim is that the second way leads to fewer allocated objects (since
    //  each element in the squares array is an enum element, and each element
    //  in the groups array is a reference to the smaller-than-the-number-of-
    //  played-squares set of Group objects), and is not really more complex;
    //  however, that extremely minor savings is offset by the extra set of
    //  Group arrays.  So, it's probably a wash, and makes resizing the 2D
    //  array slightly simpler, because you don't have to do it twice.
    /**
     * Rather than store a collection of squares, and a collection of groups,
     * we'll stuff them together into one object and store one collection of
     * those.
     */
    private static class SquareAndGroup {
        Square square;
        Group group;
        public SquareAndGroup(Square square, Group group) {
            if (square == null) throw new NullPointerException();
            this.square = square;
            this.group = group;
        }
    }

    /**
     * Creates a new Board and initializes it with a single starting tile.
     */
    public Board() {
        groups = new ArrayList<>();
        groups.add(Group.None);
        Group b1 = new Group(groups.size(), true);
        b1.grow(2, 2);
        groups.add(b1);
        Group w1 = new Group(groups.size(), false);
        w1.grow(2, 2);
        groups.add(w1);
        squares = new SquareAndGroup[2][2];
        squares[0][0] = new SquareAndGroup(Square.Black, b1);
        squares[0][1] = new SquareAndGroup(Square.Black, b1);
        squares[1][0] = new SquareAndGroup(Square.White, w1);
        squares[1][1] = new SquareAndGroup(Square.White, w1);
        height = 1;
        width = 1;
    }

    /**
     * Creates a deep copy of the other board.
     */
    public Board(Board other) {
        height = other.height;
        width = other.width;
        int h2 = height * 2;
        int w2 = width * 2;
        squares = new SquareAndGroup[w2][];
        for (int ii = 0; ii < w2; ++ii) {
            squares[ii] = new SquareAndGroup[h2];
            //  this was fine when squares was Square[][], but broke when we
            //  switched to SquareAndGroup[][].
            //System.arraycopy(other.squares[ii], 0, squares[ii], 0, h2);
            for (int jj = 0; jj < h2; ++jj) {
                SquareAndGroup sag = other.squares[ii][jj];
                if (sag != null) squares[ii][jj] = new SquareAndGroup(sag.square, sag.group);
            }
        }
        groups = new ArrayList<>(other.groups.size());
        for (int ii = 0; ii < other.groups.size(); ++ii) {
            if (other.groups.get(ii).equals(Group.None)) {
                groups.add(Group.None);
            } else {
                groups.add(new Group(other.groups.get(ii)));
            }
        }
        //  go back and replace all Group references with the new Group instances
        onAllSquares(new GroupReplacer());
    }

    private interface SquareOperator {
        /**
         * Called by onAllSquares() for each non-null square.
         *
         * @param sg the SquareAndGroup instance, never null.
         * @param xpos the X position, in squares (not tiles).
         * @param ypos the Y position, in squares (not tiles).
         * @return true if processing should continue on remaining squares,
         *         false if all necessary work is done.
         */
        boolean process(SquareAndGroup sg, int xpos, int ypos);
    }

    /**
     * Calls process() on all SquareAndGroup instances in the board.
     */
    private void onAllSquares(SquareOperator op) {
        for (int xpos = 0; xpos < squares.length; ++xpos) {
            for (int ypos = 0; ypos < squares[xpos].length; ++ypos) {
                if (squares[xpos][ypos] != null) {
                    if (!op.process(squares[xpos][ypos], xpos, ypos)) return;
                }
            }
        }
    }

    /**
     * Replaces all Group references in the squares 2D array with the Group
     * instances in the groups list.  This is done during deep copies, so that
     * the new Board has new Group instances, not references back to the Board
     * which was copied from.
     */
    private class GroupReplacer implements SquareOperator {
        @Override
        public boolean process(SquareAndGroup sg, int xpos, int ypos) {
            if (sg.group != null) sg.group = groups.get(sg.group.getID());
            return true;
        }
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Square getSquare(int squareX, int squareY) {
        if ((squareX < 0) || (squareX >= squares.length) ||
            (squareY < 0) || (squareY >= squares[squareX].length)) {
            return Square.Null;
        }
        SquareAndGroup rv = squares[squareX][squareY];
        return (rv != null) ? rv.square : Square.Null;
    }

    @Override
    public Owner getOwner(int squareX, int squareY) {
        if ((squareX < 0) || (squareX >= squares.length) ||
            (squareY < 0) || (squareY >= squares[squareX].length)) {
            return Owner.Nobody;
        }
        SquareAndGroup rv = squares[squareX][squareY];
        return ((rv != null) && (rv.group != null)) ? rv.group.getOwner() : Owner.Nobody;
    }

    @Override
    public Group getGroup(int squareX, int squareY) {
        if ((squareX < 0) || (squareX >= squares.length) ||
            (squareY < 0) || (squareY >= squares[squareX].length)) {
            return Group.None;
        }
        SquareAndGroup rv = squares[squareX][squareY];
        return ((rv != null) && (rv.group != null)) ? rv.group : Group.None;
    }

    //  ugh.
    private static class MicropulCheckResult {
        boolean matchingMicropul = false;
        boolean conflictingMicropul = false;
    }

    /**
     * Returns false if the proposed position overlaps an existing tile, if a
     * micropul is not touching an existing micropul of the same color, or if a
     * micropul is touching a micropul of the same color.
     *
     * @param tile must not be null.
     * @param xpos is in tiles, not squares.
     * @param ypos is in tiles, not squares.
     * @param reason may be null; if not, and this returns false, a
     *               human-readable the reason will be appended to it.
     */
    public boolean isValidPlay(Tile tile, int xpos, int ypos,
                               StringBuilder reason) {
        //  confirm no overlaps.
        if (!getSquare(xpos * 2, ypos * 2).isNull()) {
//XXX i18n
            if (reason != null) reason.append("Tile overlaps existing tile.");
            return false;
        }

        MicropulCheckResult result = new MicropulCheckResult();
        int x2 = xpos * 2;
        int y2 = ypos * 2;
        checkMicropuls(tile.getUpperLeft(), x2, y2 + 1, -1, 1, result);
        if (!result.conflictingMicropul) {
            checkMicropuls(tile.getUpperRight(), x2 + 1, y2 + 1, 1, 1, result);
        }
        if (!result.conflictingMicropul) {
            checkMicropuls(tile.getLowerRight(), x2 + 1, y2, 1, -1, result);
        }
        if (!result.conflictingMicropul) {
            checkMicropuls(tile.getLowerLeft(), x2, y2, -1, -1, result);
        }
        if (result.conflictingMicropul) {
//XXX i18n
            if (reason != null) reason.append("Conflicting micropul.");
            return false;
        }
        if (!result.matchingMicropul) {
//XXX i18n
            if (reason != null) reason.append("No matching micropul.");
            return false;
        }
        return true;
    }

    /**
     * Assumes isValidPlay() has already returned true.  If any catalysts are
     * activated, this returns a TilePlayResult; otherwise it returns null.
     *
     * @param tile must not be null.
     * @param xpos in tiles, not squares.
     * @param ypos in tiles, not squares.
     */
    public TilePlayResult considerResult(Tile tile, int xpos, int ypos) {
        TilePlayResult rv = new TilePlayResult();
        int x2 = xpos * 2;
        int y2 = ypos * 2;
        if (tile.getUpperLeft().isBig()) {
            //  in order to be placed, it *must* be adjacent to a micropul which
            //  activates its catalyst.
            rv.activateCatalyst(tile.getUpperLeft());
        } else {
            if (tile.getUpperLeft().isCatalyst()) {
                checkCatalyst(tile.getUpperLeft(), x2, y2 + 1, -1, 1, rv);
            }
            if (tile.getUpperRight().isCatalyst()) {
                checkCatalyst(tile.getUpperRight(), x2 + 1, y2 + 1, 1, 1, rv);
            }
            if (tile.getLowerRight().isCatalyst()) {
                checkCatalyst(tile.getLowerRight(), x2 + 1, y2, 1, -1, rv);
            }
            if (tile.getLowerLeft().isCatalyst()) {
                checkCatalyst(tile.getLowerLeft(), x2, y2, -1, -1, rv);
            }
        }
        Square ts;
        boolean activatedBig = false;

        if (tile.getUpperLeft().isMicropul()) {
            ts = getSquare(x2, y2 + 2);
            if (ts.isCatalyst()) {
                rv.activateCatalyst(ts);
                if (ts.isBig()) activatedBig = true;
            }
        }

        if (tile.getUpperRight().isMicropul()) {
            if (!activatedBig) {
                ts = getSquare(x2 + 1, y2 + 2);
                if (ts.isCatalyst()) {
                    rv.activateCatalyst(ts);
                }
            }
            activatedBig = false;
            ts = getSquare(x2 + 2, y2 + 1);
            if (ts.isCatalyst()) {
                rv.activateCatalyst(ts);
                if (ts.isBig()) activatedBig = true;
            }
        } else {
            activatedBig = false;
        }

        if (tile.getLowerRight().isMicropul()) {
            if (!activatedBig) {
                ts = getSquare(x2 + 2, y2);
                if (ts.isCatalyst()) {
                    rv.activateCatalyst(ts);
                }
            }
            activatedBig = false;
            ts = getSquare(x2 + 1, y2 - 1);
            if (ts.isCatalyst()) {
                rv.activateCatalyst(ts);
                if (ts.isBig()) activatedBig = true;
            }
        } else {
            activatedBig = false;
        }

        if (tile.getLowerLeft().isMicropul()) {
            if (!activatedBig) {
                ts = getSquare(x2, y2 - 1);
                if (ts.isCatalyst()) {
                    rv.activateCatalyst(ts);
                }
            }
            activatedBig = false;
            ts = getSquare(x2 - 1, y2);
            if (ts.isCatalyst()) {
                rv.activateCatalyst(ts);
                if (ts.isBig()) activatedBig = true;
            }
        } else {
            activatedBig = false;
        }

        if (tile.getUpperLeft().isMicropul()) {
            if (!activatedBig) {
                ts = getSquare(x2 - 1, y2 + 1);
                if (ts.isCatalyst()) {
                    rv.activateCatalyst(ts);
                }
            }
        }

        return rv.catalystsActivated() ? rv : null;
    }

    //  xpos and ypos are in squares, not tiles
    private void checkMicropuls(Square square, int xpos, int ypos,
                                int xoff, int yoff,
                                MicropulCheckResult result) {
        if (!square.isMicropul()) return;  //  nothing to check
        Square ts = getSquare(xpos + xoff, ypos);
        if (ts.isMicropul()) {
            if (ts.isBlack() == square.isBlack()) {
                result.matchingMicropul = true;
            } else {
                result.conflictingMicropul = true;
                return;
            }
        }
        ts = getSquare(xpos, ypos + yoff);
        if (ts.isMicropul()) {
            if (ts.isBlack() == square.isBlack()) {
                result.matchingMicropul = true;
            } else {
                result.conflictingMicropul = true;
            }
        }
    }

    //  xpos and ypos are in squares, not tiles
    private void checkCatalyst(Square square, int xpos, int ypos,
                               int xoff, int yoff,
                               TilePlayResult result) {
        if (!square.isCatalyst()) return;  //  nothing to check
        Square ts = getSquare(xpos + xoff, ypos);
        if (ts.isMicropul()) {
            result.activateCatalyst(square);
            return;
        }
        ts = getSquare(xpos, ypos + yoff);
        if (ts.isMicropul()) {
            result.activateCatalyst(square);
        }
    }

    /**
     * Adds the given tile at the given position.  This assumes isValidPlay()
     * has already returned true.  Returns a TilePlayResult, or null if the play
     * resulted in no catalysts being activated.
     *
     * @param tile must not be null, and must already have been confirmed to be
     *             a valid play at this position.
     * @param xpos in tiles, not squares.
     * @param ypos in tiles, not squares.
     * @return
     */
    public TilePlayResult playTile(Tile tile, int xpos, int ypos) {
        if ((xpos < 0) || (xpos >= width)) {
            //  We have to make the board wider.
            int newWidth = width + 1;
            int offset = 0;
            if (xpos < 0) {
                //  adding columns to the left
                if (xpos != -1) {
                    throw new RuntimeException("brain damage, xpos == " + xpos);
                }
                offset = 2;
                xpos += 1;  //  target square has moved over
            } else if (xpos >= width) {
                //  adding columns to the right
                if (xpos != width) {
                    throw new RuntimeException("brain damage, xpos == " + xpos);
                }
            }
            SquareAndGroup[][] ta = squares;
            squares = new SquareAndGroup[newWidth * 2][];
            for (int ii = 0; ii < width * 2; ++ii) {
                squares[ii + offset] = ta[ii];
            }
            if (offset == 0) {
                squares[width * 2] = new SquareAndGroup[height * 2];
                squares[width * 2 + 1] = new SquareAndGroup[height * 2];
            } else {
                squares[0] = new SquareAndGroup[height * 2];
                squares[1] = new SquareAndGroup[height * 2];
            }
            width = newWidth;
        }
        //  this could be an "else if", as no move can make the board taller
        //  *and* wider at once.
        if ((ypos < 0) || (ypos >= height)) {
            //  We have to make the board taller.
            int newHeight = height + 1;
            int offset = 0;
            if (ypos < 0) {
                //  adding rows to the bottom
                if (ypos != -1) {
                    throw new RuntimeException("brain damage, ypos == " + ypos);
                }
                offset = 2;
                ypos += 1;  //  target square has moved up
            } else if (ypos >= height) {
                //  adding rows to the top
                if (ypos != height) {
                    throw new RuntimeException("brain damage, ypos == " + ypos);
                }
            }
            for (int ii = 0; ii < width * 2; ++ii) {
                SquareAndGroup[] ta = squares[ii];
                squares[ii] = new SquareAndGroup[newHeight * 2];
                System.arraycopy(ta, 0, squares[ii], offset, height * 2);
            }
            height = newHeight;
        }
        //  Slap the new squares into the board.
        squares[xpos * 2][ypos * 2 + 1] = new SquareAndGroup(tile.getUpperLeft(), null);
        squares[xpos * 2 + 1][ypos * 2 + 1] = new SquareAndGroup(tile.getUpperRight(), null);
        squares[xpos * 2][ypos * 2] = new SquareAndGroup(tile.getLowerLeft(), null);
        squares[xpos * 2 + 1][ypos * 2] = new SquareAndGroup(tile.getLowerRight(), null);

        //  issue #6 - during initialization, we're playing the start tile into
        //  the middle of the board, stomping the original initial tile, which
        //  screws up our idea of what groups exist.
        if ((getWidth() == 1) && (getHeight() == 1)) {
            while (groups.size() > 1) groups.remove(groups.size() - 1);
        }

        Set<Integer> groupsToCheckForClosure = new TreeSet<Integer>();
        checkGroupChange(xpos * 2, ypos * 2 + 1, groupsToCheckForClosure);
        checkGroupChange(xpos * 2 + 1, ypos * 2 + 1, groupsToCheckForClosure);
        checkGroupChange(xpos * 2, ypos * 2, groupsToCheckForClosure);
        checkGroupChange(xpos * 2 + 1, ypos * 2, groupsToCheckForClosure);

        for (Integer id : groupsToCheckForClosure) {
            onAllSquares(new GroupClosureChecker(getGroupByID(id.intValue())));
        }

        return considerResult(tile, xpos, ypos);
    }

    public void playStone(Group group, Owner owner) {
        if (!(owner.equals(Owner.P1) || owner.equals(Owner.P2))) {
            throw new IllegalPlayException("playStone() needs P1 or P2");
        }
        if (group.equals(Group.None)) {
            throw new IllegalPlayException("playStone() on a non-group");
        }
        if (!group.getOwner().equals(Owner.Nobody)) {
            throw new IllegalPlayException("playStone() on a group which is already owned");
        }
        group.setOwner(owner);
    }

    /**
     * This looks at the whole board to see whether the given group is closed.
     */
    private class GroupClosureChecker implements SquareOperator {
        private final Group group;
        private int squaresToCheck;
        public GroupClosureChecker(Group toCheck) {
            group = toCheck;
            squaresToCheck = group.getSize();
        }
        @Override
        public boolean process(SquareAndGroup sg, int xpos, int ypos) {
            //  Is this square in our group?  If not, skip it.
            if ((sg.group == null) || (sg.group.getID() != group.getID())) {
                return squaresToCheck > 0;
            }
            if (getGroup(xpos, ypos + 1).equals(Group.None)) {
                if (getSquare(xpos, ypos + 1).isNull()) {
                    return false;  //  group is still open
                }
            }
            if (getGroup(xpos + 1, ypos).equals(Group.None)) {
                if (getSquare(xpos + 1, ypos).isNull()) {
                    return false;  //  group is still open
                }
            }
            if (getGroup(xpos, ypos - 1).equals(Group.None)) {
                if (getSquare(xpos, ypos - 1).isNull()) {
                    return false;  //  group is still open
                }
            }
            if (getGroup(xpos - 1, ypos).equals(Group.None)) {
                if (getSquare(xpos - 1, ypos).isNull()) {
                    return false;  //  group is still open
                }
            }
            //  we've checked all four sides, and we're still here, so this
            //  square, at least, is closed.
            --squaresToCheck;
            if (squaresToCheck == 0) {
                group.setClosed(true);
                return false;
            }
            return true;
        }
    }

    /**
     * When two groups are joined, the lower-numbered one replaces the higher;
     * this is what updates each affected square's reference to its group.
     */
    private static class SingleGroupReplacer implements SquareOperator {
        private final Group lookingFor;
        private final Group replaceWith;
        private int squaresToCheck;
        public SingleGroupReplacer(Group lookingFor, Group replaceWith) {
            this.lookingFor = lookingFor;
            this.replaceWith = replaceWith;
            squaresToCheck = lookingFor.getSize();
        }
        @Override
        public boolean process(SquareAndGroup sg, int xpos, int ypos) {
            if (sg.group == lookingFor) {
                sg.group = replaceWith;
                --squaresToCheck;
            }
            return squaresToCheck > 0;
        }
    }


    private void checkGroupChange(int xpos, int ypos, Set<Integer> groupsToCheckForClosure) {
        SquareAndGroup square = squares[xpos][ypos];
        if (square.square.isMicropul()) {
            Group otherGroup = getGroup(xpos, ypos + 1);
            if ((!otherGroup.equals(Group.None)) && (otherGroup.isBlack() == square.square.isBlack())) {
                square.group = otherGroup;
                otherGroup.grow(1, square.square.isBig() ? 0 : 1);
                groupsToCheckForClosure.add(Integer.valueOf(otherGroup.getID()));
            }
//refactor this; lame to have three copies of the same 25 lines
            otherGroup = getGroup(xpos + 1, ypos);
            if (!otherGroup.equals(Group.None)) {
                Square otherSquare = getSquare(xpos + 1, ypos);
                if (square.group != null) {
                    if (square.group.getID() == otherGroup.getID()) {
                        //  great, this square has already been handled.
                    } else if (square.square.isBlack() == otherSquare.isBlack()) {
                        //  we're in one group, and the adjacent square we're
                        //  checking is in a different group of the same color...
                        //  merge the higher-numbered group down into the lower.
                        int survivor = mergeGroups(square.group.getID(), otherGroup.getID());
                        groupsToCheckForClosure.add(survivor);
                    } else {
                        //  we're in one group, and the adjacent square is in a
                        //  different group, but of a different color; if the
                        //  other group wasn't closed before, it might be now.
                        groupsToCheckForClosure.add(otherGroup.getID());
                    }
                } else if (otherGroup.isBlack() == square.square.isBlack()) {
                    //  add this square to the other group.
                    square.group = otherGroup;
                    otherGroup.grow(1, square.square.isBig() ? 0 : 1);
                    groupsToCheckForClosure.add(Integer.valueOf(otherGroup.getID()));
                }
            }

            otherGroup = getGroup(xpos, ypos - 1);
            if (!otherGroup.equals(Group.None)) {
                Square otherSquare = getSquare(xpos, ypos - 1);
                if (square.group != null) {
                    if (square.group.getID() == otherGroup.getID()) {
                        //  great, this square has already been handled.
                    } else if (square.square.isBlack() == otherSquare.isBlack()) {
                        //  we're in one group, and the adjacent square we're
                        //  checking is in a different group of the same color...
                        //  merge the higher-numbered group down into the lower.
                        int survivor = mergeGroups(square.group.getID(), otherGroup.getID());
                        groupsToCheckForClosure.add(survivor);
                    } else {
                        //  we're in one group, and the adjacent square is in a
                        //  different group, but of a different color; if the
                        //  other group wasn't closed before, it might be now.
                        groupsToCheckForClosure.add(otherGroup.getID());
                    }
                } else if (otherGroup.isBlack() == square.square.isBlack()) {
                    //  add this square to the other group.
                    square.group = otherGroup;
                    otherGroup.grow(1, square.square.isBig() ? 0 : 1);
                    groupsToCheckForClosure.add(Integer.valueOf(otherGroup.getID()));
                }
            }

            otherGroup = getGroup(xpos - 1, ypos);
            if (!otherGroup.equals(Group.None)) {
                Square otherSquare = getSquare(xpos - 1, ypos);
                if (square.group != null) {
                    if (square.group.getID() == otherGroup.getID()) {
                        //  great, this square has already been handled.
                    } else if (square.square.isBlack() == otherSquare.isBlack()) {
                        //  we're in one group, and the adjacent square we're
                        //  checking is in a different group of the same color...
                        //  merge the higher-numbered group down into the lower.
                        int survivor = mergeGroups(square.group.getID(), otherGroup.getID());
                        groupsToCheckForClosure.add(survivor);
                    } else {
                        //  we're in one group, and the adjacent square is in a
                        //  different group, but of a different color; if the
                        //  other group wasn't closed before, it might be now.
                        groupsToCheckForClosure.add(otherGroup.getID());
                    }
                } else if (otherGroup.isBlack() == square.square.isBlack()) {
                    //  add this square to the other group.
                    square.group = otherGroup;
                    otherGroup.grow(1, square.square.isBig() ? 0 : 1);
                    groupsToCheckForClosure.add(Integer.valueOf(otherGroup.getID()));
                }
            }

            if (square.group == null) {
                //  none of the adjacent squares have a group we can join; we
                //  are a new group of one.
                square.group = new Group(groups.size(), square.square.isBlack());
                square.group.grow(1, square.square.isBig() ? 0 : 1);
                groups.add(square.group);
                groupsToCheckForClosure.add(Integer.valueOf(square.group.getID()));
            }
        } else {
            //  Not a group; add all adjacent groups to groupsToCheckForClosure.
            Group group;
            if (!(group = getGroup(xpos, ypos + 1)).equals(Group.None)) {
                groupsToCheckForClosure.add(Integer.valueOf(group.getID()));
            }
            if (!(group = getGroup(xpos + 1, ypos)).equals(Group.None)) {
                groupsToCheckForClosure.add(Integer.valueOf(group.getID()));
            }
            if (!(group = getGroup(xpos, ypos - 1)).equals(Group.None)) {
                groupsToCheckForClosure.add(Integer.valueOf(group.getID()));
            }
            if (!(group = getGroup(xpos - 1, ypos)).equals(Group.None)) {
                groupsToCheckForClosure.add(Integer.valueOf(group.getID()));
            }
        }
    }

    /**
     * This grows the lower-numbered group by the size of the higher-numbered
     * group, replaces all references to the higher-numbered group with the
     * lower-numbered group, and removes the higher-numbered group from the
     * groups list.
     *
     * @return the ID of the lower group.
     */
    private int mergeGroups(int g1, int g2) {
        Group mergeFrom, mergeTo;
        if (g1 > g2) {
            mergeFrom = groups.get(g1);
            mergeTo = groups.get(g2);
        } else {
            mergeFrom = groups.get(g2);
            mergeTo = groups.get(g1);
        }
        mergeTo.grow(mergeFrom);
        if (mergeTo.getOwner().equals(Owner.P1)) {
            if (mergeFrom.getOwner().equals(Owner.P2) ||
                mergeFrom.getOwner().equals(Owner.Both)) {
                mergeTo.setOwner(Owner.Both);
            }  //  otherwise, leave it
        } else if (mergeTo.getOwner().equals(Owner.P2)) {
            if (mergeFrom.getOwner().equals(Owner.P1) ||
                    mergeFrom.getOwner().equals(Owner.Both)) {
                mergeTo.setOwner(Owner.Both);
            }  //  otherwise, leave it
        } else if (mergeTo.getOwner().equals(Owner.Nobody)) {
            mergeTo.setOwner(mergeFrom.getOwner());  //  may also be Nobody
        }
        onAllSquares(new SingleGroupReplacer(mergeFrom, mergeTo));
        if (mergeFrom.getID() == (groups.size() - 1)) {
            groups.remove(mergeFrom.getID());
        } else {
            groups.set(mergeFrom.getID(), Group.None);
        }
        return mergeTo.getID();
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public Group getGroupByID(int id) {
        return (id < groups.size()) ? groups.get(id) : Group.None;
    }

    private int height;  //  in tiles, not squares
    private int width;  //  in tiles, not squares
    private SquareAndGroup[][] squares;
    private List<Group> groups;
}
