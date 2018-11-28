package com.kuhrusty.micropul.model;

import java.util.ArrayList;
import java.util.List;

import static com.kuhrusty.micropul.model.Square.*;

/**
 * A single tile in the game, made up of four Squares.
 */
public class Tile {
    /**
     * Creates and returns a new list of Tiles.
     */
    public static List<Tile> createTiles() {
        ArrayList<Tile> rv = new ArrayList<Tile>(48);
        //  The order of these guys is based on the micropul-Game-1.0.jpg image
        //  included in micropul-Package-English.zip.
        rv.add(new Tile(White, Empty, Empty, Empty));
        rv.add(new Tile(Black, Empty, Empty, Empty));
        rv.add(new Tile(White, Black, PlusCatalyst, Empty));
        rv.add(new Tile(Black, White, PlusCatalyst, Empty));
        rv.add(new Tile(PlusCatalyst, White, White, Black));
        rv.add(new Tile(PlusCatalyst, Black, Black, White));

        rv.add(new Tile(White, Empty, Empty, SingleCatalyst));
        rv.add(new Tile(Black, Empty, Empty, SingleCatalyst));
        rv.add(new Tile(White, Black, Empty, SingleCatalyst));
        rv.add(new Tile(Black, White, Empty, SingleCatalyst));
        rv.add(new Tile(SingleCatalyst, White, White, Black));
        rv.add(new Tile(SingleCatalyst, Black, Black, White));

        rv.add(new Tile(White, Empty, SingleCatalyst, Empty));
        rv.add(new Tile(Black, Empty, SingleCatalyst, Empty));
        rv.add(new Tile(White, White, Empty, SingleCatalyst));
        rv.add(new Tile(Black, Black, Empty, SingleCatalyst));
        rv.add(new Tile(White, White, SingleCatalyst, White));
        rv.add(new Tile(Black, Black, SingleCatalyst, Black));

        rv.add(new Tile(White, SingleCatalyst, PlusCatalyst, Empty));
        rv.add(new Tile(Black, SingleCatalyst, PlusCatalyst, Empty));
        rv.add(new Tile(White, White, DoubleCatalyst, Empty));
        rv.add(new Tile(Black, Black, DoubleCatalyst, Empty));
        rv.add(new Tile(White, White, DoubleCatalyst, Black));
        rv.add(new Tile(Black, Black, DoubleCatalyst, White));

        rv.add(new Tile(White, SingleCatalyst, Empty, SingleCatalyst));
        rv.add(new Tile(Black, SingleCatalyst, Empty, SingleCatalyst));
        rv.add(new Tile(White, Empty, Empty, White));
        rv.add(new Tile(Black, Empty, Empty, Black));
        rv.add(new Tile(White, White, White, Black));
        rv.add(new Tile(Black, Black, Black, White));

        rv.add(new Tile(White, DoubleCatalyst, Empty, SingleCatalyst));
        rv.add(new Tile(Black, DoubleCatalyst, Empty, SingleCatalyst));
        rv.add(new Tile(White, Empty, SingleCatalyst, Black));
        rv.add(new Tile(Black, Empty, SingleCatalyst, White));
        rv.add(new Tile(White, Black, Black, White));
        rv.add(new Tile(Black, White, White, Black));

        rv.add(new Tile(BigWhitePlus, BigWhitePlus, BigWhitePlus, BigWhitePlus));
        rv.add(new Tile(BigBlackPlus, BigBlackPlus, BigBlackPlus, BigBlackPlus));
        rv.add(new Tile(White, PlusCatalyst, SingleCatalyst, White));
        rv.add(new Tile(Black, PlusCatalyst, SingleCatalyst, Black));
        rv.add(new Tile(White, White, Black, Black));
        rv.add(new Tile(Black, Black, White, White));

        rv.add(new Tile(BigWhiteDot, BigWhiteDot, BigWhiteDot, BigWhiteDot));
        rv.add(new Tile(BigBlackDot, BigBlackDot, BigBlackDot, BigBlackDot));
        rv.add(new Tile(White, SingleCatalyst, SingleCatalyst, Black));
        rv.add(new Tile(Black, SingleCatalyst, SingleCatalyst, White));
        rv.add(new Tile(White, White, White, White));
        rv.add(new Tile(Black, Black, Black, Black));

        for (int ii = rv.size() - 1; ii >= 0; --ii) rv.get(ii).setID(ii);
        return rv;
    }

    /**
     * Note that we assume these are all non-null and not isNull().
     */
    public Tile(Square ul, Square ur, Square ll, Square lr) {
        this.ul = ul;
        this.ur = ur;
        this.ll = ll;
        this.lr = lr;
    }

    /**
     * Creates a copy of the given tile, which must not be null.
     */
    public Tile(Tile other) {
        this.id = other.id;
        this.rotation = other.rotation;
        this.ul = other.ul;
        this.ur = other.ur;
        this.ll = other.ll;
        this.lr = other.lr;
    }

    public Square getUpperLeft() {
        return ul;
    }

    public Square getUpperRight() {
        return ur;
    }

    public Square getLowerLeft() {
        return ll;
    }

    public Square getLowerRight() {
        return lr;
    }

    public void rotateLeft() {
        if (ul.isBig()) return;
        Square ts = ul;
        ul = ur;
        ur = lr;
        lr = ll;
        ll = ts;
        --rotation;
        if (rotation == -2) rotation = 2;
    }

    public void rotateRight() {
        if (ul.isBig()) return;
        Square ts = ur;
        ur = ul;
        ul = ll;
        ll = lr;
        lr = ts;
        ++rotation;
        if (rotation == 3) rotation = -1;
    }

    public int getID() {
        return id;
    }
    public void setID(int id) {
        this.id = id;
    }
    public int getRotation() {
        return rotation;
    }

    public void set(Square ul, Square ur, Square ll, Square lr) {
        this.ul = ul;
        this.ur = ur;
        this.ll = ll;
        this.lr = lr;
    }

    private int id = 0;
    private int rotation = 0;
    private Square ul;
    private Square ur;
    private Square ll;
    private Square lr;
}
