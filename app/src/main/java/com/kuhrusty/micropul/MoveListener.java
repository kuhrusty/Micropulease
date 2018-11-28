package com.kuhrusty.micropul;

import com.kuhrusty.micropul.model.Player;
import com.kuhrusty.micropul.model.Tile;

/**
 * This is passed to Bot.takeTurn(), and is the way the bot announces that it's
 * decided on a move.
 */
public interface MoveListener {
    /**
     * Notifies the listener that the player has decided to play a tile.
     *
     * @param player the Player instance which was passed to takeTurn().
     * @param tile the Tile being played.
     * @param xpos the X position in the Board which was passed to takeTurn(),
     *             in tiles (not squares).
     * @param ypos the Y position in the Board which was passed to takeTurn(),
     *             in tiles (not squares).
     */
    void playTile(Player player, Tile tile, int xpos, int ypos);

    /**
     * Notifies the listener that the player has decided to draw a tile from
     * their supply.
     *
     * @param player the Player instance which was passed to takeTurn().
     */
    void drawTile(Player player);

    /**
     * Notifies the listener that the player has decided to place a stone on an
     * unclaimed group.
     *
     * @param player the Player instance which was passed to takeTurn().
     * @param groupID the ID of the Group, taken from the Board.
     */
    void placeStone(Player player, int groupID);

    /**
     * Notifies the listener that the player has decided to give up.
     *
     * @param player the Player instance which was passed to takeTurn().
     * @param reason may be null.
     */
    void concede(Player player, String reason);
}
