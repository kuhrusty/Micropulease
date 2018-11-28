package com.kuhrusty.micropul.model;

/**
 * A group of contiguous micropul.
 */
public class Group {

    /**
     * An immutable Group instance which some methods return instead of null.
     */
    public static final Group None = new Group(0, false) {
        //  I miss C++... real languages have const
        @Override
        public void setOwner(Owner owner) {
            throw new RuntimeException("immutable");
        }
        @Override
        public void setClosed(boolean closed) {
            throw new RuntimeException("immutable");
        }
        @Override
        public void grow(int size, int points) {
            throw new RuntimeException("immutable");
        }
        @Override
        public void grow(Group mergingFrom) {
            throw new RuntimeException("immutable");
        }
   };

    /**
     * Returns an Owner, never null.
     */
    public Owner getOwner() {
        return owner;
    }
    public void setOwner(Owner owner) {
        if (owner == null) throw new NullPointerException();
        this.owner = owner;
    }

    public int getID() {
        return id;
    }

    /**
     * True if this is a group of black micropul, false if white.
     */
    public boolean isBlack() {
        return isBlack;
    }

    /**
     * True if this group is closed (that is, can't be added to, and is worth
     * points).
     */
    public boolean isClosed() {
        return closed;
    }
    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    /**
     * The number of squares covered by this group.  Note that this will not be
     * the same as the <i>point value</i> of the group if it contains big micropuls.
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the point value of this group, <i>if</i> it were complete.  This
     * will be different than the size if the group contains big micropuls.
     */
    public int getPointValue() {
if (quarterPoints != 0) throw new RuntimeException("GACKK group " + id + " getPointValue() called with quarterPoints " + quarterPoints);
        return value;
    }

    /**
     * Well, this is a horrible kludge.  Originally this was just recording the
     * size of the group (the number of squares); but when you account for big
     * micropuls, adding one square to the group doesn't add one point to its
     * value; it adds a <i>quarter</i> of a point.  But you can't represent that
     * with ints unless I take quarter points or something... so, when you're
     * adding a single square, grow(1, 1) means we got one square bigger, and
     * one point more valuable; grow(1, 0) means we got one square bigger, and
     * <i>one quarter</i> of a point more valuable.  We keep track of how many
     * quarter-points we received (theoretically, it should always come out to
     * a multiple of four) and turn that into a full point when we receive four.
     */
    public void grow(int size, int score) {
        this.size += size;
        this.value += score;
        if (score == 0) {
            quarterPoints += 1;
            if (quarterPoints == 4) {
                ++value;
                quarterPoints = 0;
            }
        }
    }

    public void grow(Group mergingFrom) {
        size += mergingFrom.size;
        value += mergingFrom.value;
        quarterPoints += mergingFrom.quarterPoints;
        if (quarterPoints > 0) {
            value += (quarterPoints / 4);
            quarterPoints = (quarterPoints % 4);
        }
    }

    public Group(int id, boolean isBlack) {
        this.id = id;
        owner = Owner.Nobody;
        this.isBlack = isBlack;
        closed = false;
        size = 0;
    }

    /**
     * Creates a deep copy of the given Group, which must not be null.
     */
    public Group(Group other) {
        owner = other.owner;
        id = other.id;
        this.isBlack = other.isBlack;
        closed = other.closed;
        size = other.size;
        value = other.value;
        quarterPoints = other.quarterPoints;
    }

    private Owner owner;
    private final int id;
    private final boolean isBlack;
    private boolean closed;
    private int size;
    private int value;
    private int quarterPoints = 0;
}
