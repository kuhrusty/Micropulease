package com.kuhrusty.micropul.bot;

import com.kuhrusty.micropul.MoveListener;
import com.kuhrusty.micropul.model.Board;
import com.kuhrusty.micropul.model.Player;

public interface Bot {
    /**
     * Returns the name which will be displayed in the start-game screen, and as
     * the player name during games.
     */
    String getName();

    /**
     * Returns a brief description of the bot for display in the start-game
     * screen.
     */
    String getDescription();

    /**
     * Called when the bot should make a move.  When it decides what it's going
     * to do, it should call listener.playTile(), drawTile(), placeStone(), or
     * concede(), passing the Player instance which was passed in.  Everything
     * which gets passed in is a copy and can be modified.
     *
     * <p>Note that this will be called on a non-UI thread, so you don't have
     * to worry about doing that yourself.  When you decide on your move and
     * notify the listener, the listener will take care of notifying the UI
     * thread.</p>
     *
     * @param self information about the bot player.
     * @param opponent information about the opponent.
     * @param board the board state.
     * @param listener the listener who should be notified when the move is
     *                 decided on.
     */
    void takeTurn(Player self, Player opponent, Board board, MoveListener listener);
}
