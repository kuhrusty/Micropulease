package com.kuhrusty.micropul.model;

import com.kuhrusty.micropul.bot.Bot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This stores the Board and some additional game state stuff, like the size of
 * the core.
 */
public class GameState {

    private Player p1;
    private Player p2;
    private Bot p1Bot;
    private Bot p2Bot;
    private Board board;
    //  As tiles are moved from the core to players' supply, we don't actually
    //  move the tiles over; we just say, you now have n tiles in your supply,
    //  and we reduce coreSize accordingly.  When they draw from their supply,
    //  we remove from unseenTiles.
    private int coreSize;
    private List<Tile> unseenTiles;

    public GameState() {
    }

    /**
     * Creates a deep copy of the given GameState, except for the Bot instances,
     * because I'm not actually sure what to do about those.  (They probably
     * shouldn't be included in the GameState anyway.)
     */
    public GameState(GameState other) {
        p1 = new Player(other.p1);
        p2 = new Player(other.p2);
//not sure what to do about bots
p1Bot = other.p1Bot;
p2Bot = other.p2Bot;
        board = new Board(other.board);
        coreSize = other.coreSize;
        unseenTiles = new ArrayList<>(other.unseenTiles.size());
        for (int ii = 0; ii < other.unseenTiles.size(); ++ii) {
            unseenTiles.add(new Tile(other.unseenTiles.get(ii)));
        }
    }

    /**
     * Prepares the board, shuffles the tiles, deals them out.
     *
     * @param p1 The name of the first player.
     * @param p2 The name of the second player.
     * @param p1Bot The Bot instance which handles plays for the first player,
     *              or null.
     * @param p2Bot The Bot instance which handles plays for the second player,
     *              or null.
     */
    public void initGame(String p1, String p2, Bot p1Bot, Bot p2Bot) {
        this.p1 = new Player(Owner.P1, p1);
        this.p2 = new Player(Owner.P2, p2);
        this.p1Bot = p1Bot;
        this.p2Bot = p2Bot;
        unseenTiles = Tile.createTiles();
        board = new Board();
        Tile startTile = unseenTiles.remove(40);
        board.playTile(startTile, 0, 0);

        //  Temporarily uncomment this if you want to generate a preview for a
        //  new renderer you're working on.
        //if (true) {
        //    Tile tile = unseenTiles.remove(41);  //  ID 42, but 40 was removed
        //    board.playTile(tile, 0, 1);
        //    tile = unseenTiles.remove(4);
        //    tile.rotateLeft();
        //    board.playTile(tile, 1, 1);
        //    tile = unseenTiles.remove(30);  //  ID 31, but 4 was removed
        //    tile.rotateLeft();
        //    board.playTile(tile, 1, 0);
        //}

        Collections.shuffle(unseenTiles);
        for (int ii = 0; ii < 6; ++ii) {
            this.p1.addToHand(draw());
            this.p2.addToHand(draw());
        }
        coreSize = unseenTiles.size();
    }

    /**
     * Returns the number of tiles remaining in the core.
     */
    public int coreSize() {
        return coreSize;
    }

    /**
     * Reduces the core size by one, and returns true if that was possible,
     * false if it was already empty.
     */
    public boolean drawFromCore() {
        if (coreSize > 0) {
            --coreSize;
            return true;
        }
        return false;
    }

    /**
     * Call this when a tile is drawn from a player's supply; it returns the
     * new tile.
     */
    public Tile draw() {
        return unseenTiles.remove(unseenTiles.size() - 1);
    }

    public Board getBoard() {
        return board;
    }
    public Player getPlayer1() {
        return p1;
    }
    public Player getPlayer2() {
        return p2;
    }
    public Bot getPlayer1Bot() { return p1Bot; }
    public Bot getPlayer2Bot() { return p2Bot; }

    /**
     * Returns the Player instance corresponding to the given Owner (which must
     * be Owner.P1 or Owner.P2).
     */
    public Player getPlayer(Owner owner) {
        if (owner.equals(Owner.P1)) return p1;
        else if (owner.equals(Owner.P2)) return p2;
        else throw new IllegalArgumentException(owner.toString());
    }
}
