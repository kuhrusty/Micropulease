package com.kuhrusty.micropul;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.kuhrusty.micropul.bot.Bot;
import com.kuhrusty.micropul.model.Board;
import com.kuhrusty.micropul.model.GameState;
import com.kuhrusty.micropul.model.Group;
import com.kuhrusty.micropul.model.IllegalPlayException;
import com.kuhrusty.micropul.model.IntCoordinates;
import com.kuhrusty.micropul.model.Owner;
import com.kuhrusty.micropul.model.Player;
import com.kuhrusty.micropul.model.Tile;
import com.kuhrusty.micropul.model.TilePlayResult;
import com.kuhrusty.micropul.renderer.BWRenderer;

/**
 * <p>There are two places where a player's turn is really done: in drawTile(),
 * when they draw a tile from their supply--no going back after that--and in
 * confirmPlay(), when they confirm that the tile or stone they've played is
 * what they wanted to do.  At that point, either we switch the display for the
 * other player and pop up a "hand the device to the other player" dialog, or we
 * set our status to "waiting for the other player" and tell the bot to make a
 * move.</p>
 */
public class PlayGameActivity extends AppCompatActivity {
    private static final String LOGBIT = "PlayGameActivity";

    /**
     * Player 1's name.
     */
    public static final String INTENT_PLAYER1_NAME = "name1";
    public static final String INTENT_PLAYER2_NAME = "name2";
    public static final String INTENT_PLAYER2_TYPE = "type2";
    public static final String INTENT_RENDERER_NAME = "renderer";

    private Owner currentPlayer = Owner.P1;
    private GameState game = null;
    //  when they make a move, we save the game state; if they cancel we restore.
    private GameState savedGame = null;
    private boolean extraTurn = false;

    private TextView youStatus;
    private TextView opponentStatus;
    private TextView status;
    private TextView coreRemaining;
    private TileView[] tileView = new TileView[6];
    private TileView selectedTile = null;
    private TileView stonesView;
    private View handView;
    private BoardView boardView;
    private View drawButton;
    private View okButton;
    private View cancelButton;

    //  This may be silly, but... regardless of how long a bot takes, wait this
    //  long (in ms) before returning control to the player.  If the sleep time
    //  is 1500ms, and the bot takes 200ms to make its move, we'll sleep for
    //  another 1300ms.
    private long botSleepTimeMS = 1500L;
    private long lastBotStart = 0L;

    private final View.OnTouchListener tileTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (savedGame != null) {
                //  We're waiting for them to confirm or cancel a move.
                return false;
            }
            if (selectedTile != view) {
                if (selectedTile != null) selectedTile.setSelected(false);
                selectedTile = (TileView)view;
                selectedTile.setSelected(true);
                boardView.setSelectedTile(selectedTile.getTile());
                boardView.setSelectedStone(selectedTile == stonesView);
                boardView.invalidate();
            } else if (selectedTile.getTile() != null) {
                selectedTile.getTile().rotateRight();
                selectedTile.invalidate();
                boardView.setSelectedTile(selectedTile.getTile());
                boardView.invalidate();
            }
            return false;
        }
    };

    private final View.OnTouchListener boardTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (savedGame != null) {
                //  We're waiting for them to confirm or cancel a move.
                return false;
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                return selectedTile != null;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                Tile tile;
                if ((selectedTile != null) && ((tile = selectedTile.getTile()) != null)) {
                    //  map the touch location to a tile position, see if it's
                    //  valid, then begin the move.
                    IntCoordinates bp = boardView.touchToTilePosition(motionEvent.getX(), motionEvent.getY());
                    if (bp != null) {
                        if (game.getBoard().isValidPlay(tile, bp.xpos, bp.ypos, null)) {
                            proposeTilePlay(tile, bp.xpos, bp.ypos);
                        }
                    }
                } else if (selectedTile == stonesView) {
                    //  map the touch location to a square position, see if it's
                    //  valid, then begin the move.
                    IntCoordinates bp = boardView.touchToSquarePosition(motionEvent.getX(), motionEvent.getY());
                    if (bp != null) {
                        Log.d(LOGBIT, "got stone touch " + bp.xpos + ", " + bp.ypos + ", LET'S DO THIS");
                        Group group = game.getBoard().getGroup(bp.xpos, bp.ypos);
                        if (group.getOwner().equals(Owner.Nobody) && (!group.equals(Group.None))) {
                            proposeStonePlay(group);
                        }
                    } else {
                        Log.d(LOGBIT, "got null coordinates");
                    }
                }
            }
            return false;
        }
    };

    private void proposeTilePlay(Tile tile, int xpos, int ypos) {
        savedGame = new GameState(game);
        if (game.getPlayer(currentPlayer).removeTileByID(tile.getID()) == null) {
            Log.w(LOGBIT, "player played tile " + tile.getID() + ", which they don't have!?");
        }
        TilePlayResult tpr = game.getBoard().playTile(tile, xpos, ypos);
        if (tpr != null) {
            for (int ii = 0; ii < tpr.getTiles(); ++ii) {
                if (game.drawFromCore()) {
                    game.getPlayer(currentPlayer).setTilesInSupply(game.getPlayer(currentPlayer).getTilesInSupply() + 1);
                }
            }
            if ((game.coreSize() > 0) && (tpr.getExtraTurns() > 0)) {
                extraTurn = true;
            }
        }
        selectedTile.setTile(null);
        selectedTile.setSelected(false);
        selectedTile.setVisibility(View.INVISIBLE);
        selectedTile = null;
        boardView.setSelectedTile(null);
        boardView.invalidate();

        drawButton.setEnabled(false);
        cancelButton.setEnabled(true);
        okButton.setEnabled(true);
        updateStatus();
    }

    private void proposeStonePlay(Group group) {
        savedGame = new GameState(game);
        game.getBoard().playStone(group, currentPlayer);
        boardView.setSelectedStone(false);
        int remaining = game.getPlayer(currentPlayer).getStonesRemaining() - 1;
        selectedTile.setSelected(false);
        selectedTile = null;
        game.getPlayer(currentPlayer).setStonesRemaining(remaining);
        boardView.invalidate();

        drawButton.setEnabled(false);
        cancelButton.setEnabled(true);
        okButton.setEnabled(true);
        updateStatus();
    }

    /**
     * Uhh, draw a tile from the supply, not... draw... a picture... of the tile
     */
    public void drawTile(View view) {
        Log.d(LOGBIT, "drawTile()");

        Player tp = game.getPlayer(currentPlayer);
        tp.addToHand(game.draw());
        tp.setTilesInSupply(tp.getTilesInSupply() - 1);

currentPlayer = (currentPlayer == Owner.P1) ? Owner.P2 : Owner.P1;
Player switchingToPlayer = (currentPlayer == Owner.P1) ? game.getPlayer1() : game.getPlayer2();
        cancelButton.setEnabled(false);
        okButton.setEnabled(false);
        if ((switchingToPlayer != null) && switchingToPlayer.isHotSeat()) {
            switchPlayer(switchingToPlayer.getName());
            updateStatus();
        } else {
            updateStatus();
            notifyBot(true);
        }
    }

    public void confirmPlay(View view) {
        Log.d(LOGBIT, "confirmPlay()");
        savedGame = null;

        Player switchingToPlayer = null;
        if (!extraTurn) {
            currentPlayer = (currentPlayer == Owner.P1) ? Owner.P2 : Owner.P1;
            switchingToPlayer = game.getPlayer(currentPlayer);
        }
        extraTurn = false;
        cancelButton.setEnabled(false);
        okButton.setEnabled(false);
        if (game.coreSize() == 0) {
            gameOver();
        } else if (switchingToPlayer == null) {
            //  still this player's turn.
            updateStatus();
        } else if (switchingToPlayer.isHotSeat()) {
            switchPlayer(switchingToPlayer.getName());
            updateStatus();
        } else {
            updateStatus();
            notifyBot(true);
        }
    }

    public void cancelPlay(View view) {
        Log.d(LOGBIT, "cancelPlay()");
        game = savedGame;
        savedGame = null;
        boardView.setGame(game);
        extraTurn = false;

        cancelButton.setEnabled(false);
        okButton.setEnabled(false);
        updateStatus();
    }

    /**
     * It would be nice if, after hitting OK, this let the player view the final
     * board state; instead it just croaks the Activity.
     */
    private void gameOver() {
        int p1score = game.getPlayer1().calculateScore(game.getBoard());
        int p2score = game.getPlayer2().calculateScore(game.getBoard());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (p1score == p2score) {
            builder.setTitle(R.string.game_over_tie_title);
            builder.setMessage(getString(R.string.game_over_tie_message, p1score));
        } else if (p1score > p2score) {
            builder.setTitle(getString(R.string.game_over_title,
                    game.getPlayer1().getName()));
            builder.setMessage(getString(R.string.game_over_message,
                    game.getPlayer1().getName(), p1score, game.getPlayer2().getName(), p2score));
        } else {
            builder.setTitle(getString(R.string.game_over_title,
                    game.getPlayer2().getName()));
            builder.setMessage(getString(R.string.game_over_message,
                    game.getPlayer2().getName(), p2score, game.getPlayer1().getName(), p1score));
        }
        builder.setPositiveButton(R.string.switch_player_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Hides the hand area, pops up a dialog.
     */
    private void switchPlayer(String newPlayerName) {
        handView.setVisibility(View.INVISIBLE);
        boardView.setSelectedTile(null);
        boardView.setSelectedStone(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.switch_player_message, newPlayerName));
        builder.setTitle(R.string.switch_player_title);
        builder.setPositiveButton(R.string.switch_player_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                handView.setVisibility(View.VISIBLE);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String name = null;
        String name2 = null;
        String type2 = null;
        String rendererName = null;

        Intent intent;
        if (savedInstanceState != null) {
            //  see https://github.com/kuhrusty/Micropulease/issues/3
//            name = savedInstanceState.getString(INTENT_PLAYER1_NAME);
//            name2 = savedInstanceState.getString(INTENT_PLAYER2_NAME);
//            type2 = savedInstanceState.getString(INTENT_PLAYER2_TYPE);
//            rendererName = savedInstanceState.getString(INTENT_RENDERER_NAME);
//            String currentPlayer = savedInstanceState.getString(STATE_CURRENT_PLAYER);
//            if (currentPlayer != null) {
//                this.currentPlayer = Owner.valueOf(currentPlayer);
//            }
//            game = savedInstanceState.getParcelable(STATE_GAME);
//            savedGame = savedInstanceState.getParcelable(STATE_SAVED_GAME);
//            extraTurn = savedInstanceState.getBoolean(STATE_EXTRA_TURN);
        } else if ((intent = getIntent()) != null) {
            name = intent.getStringExtra(INTENT_PLAYER1_NAME);
            name2 = intent.getStringExtra(INTENT_PLAYER2_NAME);
            type2 = intent.getStringExtra(INTENT_PLAYER2_TYPE);
            rendererName = intent.getStringExtra(INTENT_RENDERER_NAME);
        }
        if (name == null) name = getResources().getString(R.string.p1_default_name);
        if (name2 == null) name2 = getResources().getString(R.string.p2_default_name);

        Bot bot = null, bot2 = null;

        PlayerType p1type = PlayerType.getPlayerTypes(getResources())[0];
        PlayerType p2type = null;
        if (type2 != null) {
            PlayerType[] pta = PlayerType.getPlayerTypes(getResources());
            for (int ii = 0; ii < pta.length; ++ii) {
                if (type2.equals(pta[ii].toString())) {
                    p2type = pta[ii];
                    if (p2type.isBot()) {
                        bot2 = p2type.instantiateBot(getResources());
                    }
                    break;
                }
            }
        }
        if (p2type == null) p2type = PlayerType.getPlayerTypes(getResources())[0];

        TileRenderer renderer = null;
        if (rendererName != null) {
            TileRenderer[] tra = StartGameActivity.getTileRenderers(this);
            for (int ii = 0; ii < tra.length; ++ii) {
                if (rendererName.equals(tra[ii].toString())) {
                    renderer = tra[ii];
                    break;
                }
            }
        }
        if (renderer == null) {
            Log.w(LOGBIT, "no renderer found, creating default...");
            renderer = new BWRenderer(getResources());
        }
        renderer.prepare();

        if (game == null) {
            game = new GameState();
            game.initGame(name, name2, bot, bot2);
            game.getPlayer1().setHotSeat(p1type.isHotSeat());
            game.getPlayer2().setHotSeat(p2type.isHotSeat());
        }

        setContentView(R.layout.activity_play_game);

        youStatus = findViewById(R.id.youStatus);
        opponentStatus = findViewById(R.id.opponentStatus);
        status = findViewById(R.id.status);
        coreRemaining = findViewById(R.id.coreRemaining);
        tileView[0] = findViewById(R.id.tile0);
        tileView[1] = findViewById(R.id.tile1);
        tileView[2] = findViewById(R.id.tile2);
        tileView[3] = findViewById(R.id.tile3);
        tileView[4] = findViewById(R.id.tile4);
        tileView[5] = findViewById(R.id.tile5);
        stonesView = findViewById(R.id.stones);
        handView = findViewById(R.id.handView);
        boardView = findViewById(R.id.boardView);
        for (int ii = 0; ii < tileView.length; ++ii) {
            tileView[ii].setRenderer(renderer);
            tileView[ii].setOnTouchListener(tileTouchListener);
        }
        stonesView.setRenderer(renderer);
        stonesView.setOnTouchListener(tileTouchListener);
        stonesView.setStonesRemaining(currentPlayer, 3);
        boardView.setGame(game);
        boardView.setRenderer(renderer);
        boardView.setOnTouchListener(boardTouchListener);

        drawButton = findViewById(R.id.drawTileButton);
        drawButton.setEnabled(false);
        cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setEnabled(false);
        okButton = findViewById(R.id.okButton);
        okButton.setEnabled(false);

        updateStatus();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        //  see https://github.com/kuhrusty/Micropulease/issues/3
//        state.putString(GAME_STATE_KEY, mGameState);
//        state.putString(TEXT_VIEW_KEY, mTextView.getText());
//
//        name = savedInstanceState.getString(INTENT_PLAYER1_NAME);
//        name2 = savedInstanceState.getString(INTENT_PLAYER2_NAME);
//        type2 = savedInstanceState.getString(INTENT_PLAYER2_TYPE);
//        rendererName = savedInstanceState.getString(INTENT_RENDERER_NAME);
//        String currentPlayer = savedInstanceState.getString(STATE_CURRENT_PLAYER);
//        if (currentPlayer != null) {
//            this.currentPlayer = Owner.valueOf(currentPlayer);
//        }
//        game = savedInstanceState.getParcelable(STATE_GAME);
//        savedGame = savedInstanceState.getParcelable(STATE_SAVED_GAME);
//        extraTurn = savedInstanceState.getBoolean(STATE_EXTRA_TURN);

        super.onSaveInstanceState(state);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.confirm_exit_title)
                .setMessage(R.string.confirm_exit_message)
                .setPositiveButton(R.string.confirm_exit_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.confirm_exit_cancel, null)
                .show();
    }

    private void updateStatus() {
        Log.d(LOGBIT, "updateStatus, current player " + currentPlayer +
                ", hotseat " + game.getPlayer(currentPlayer).isHotSeat());
        Player cp = game.getPlayer(currentPlayer);
        Player op = (currentPlayer == Owner.P1) ? game.getPlayer2() : game.getPlayer1();
        //  well... "cp" used to mean "current player," but if the current
        //  player is a bot, we want to leave it showing the human's stuff.
        boolean yourTurn = true;
        if (!cp.isHotSeat()) {
            yourTurn = false;
            cp = op;
            op = game.getPlayer(currentPlayer);
        }

        youStatus.setText(getResources().getString(R.string.you_status,
                cp.getName(), cp.getTilesInHand(), cp.getTilesInSupply(),
                cp.getStonesRemaining(), cp.calculateScore(game.getBoard())));
        opponentStatus.setText(getResources().getString(R.string.opponent_status,
                op.getName(), op.getTilesInHand(), op.getTilesInSupply(),
                op.getStonesRemaining(), op.calculateScore(game.getBoard())));
        if (yourTurn) {
            status.setText(getResources().getString(R.string.your_turn_status, cp.getName()));
        } else {
            status.setText(getResources().getString(R.string.their_turn_status, op.getName()));
        }
        coreRemaining.setText(getResources().getString(R.string.core_remaining, game.coreSize()));
        int ii = 0;
        while (ii < cp.getTilesInHand()) {
            Log.d(LOGBIT, "setting tile view " + ii + " to " + cp.getTile(ii).getID());
            tileView[ii].setTile(cp.getTile(ii));
            tileView[ii].setVisibility(View.VISIBLE);
            tileView[ii].invalidate();
            ++ii;
        }
        while (ii < 6) {
            Log.d(LOGBIT, "hiding tile view " + ii);
            tileView[ii].setTile(null);
            tileView[ii].setVisibility(View.INVISIBLE);
            ++ii;
        }
        stonesView.setStonesRemaining(cp.getOwner(), cp.getStonesRemaining());
        if (cp.getStonesRemaining() == 0) {
            stonesView.setVisibility(View.INVISIBLE);
        } else {
            stonesView.setVisibility(View.VISIBLE);
            stonesView.invalidate();
        }
        boardView.invalidate();

        drawButton.setEnabled(yourTurn && (savedGame == null) &&
                (cp.getTilesInHand() < 6) && (cp.getTilesInSupply() > 0));
    }

    /**
     * Tells a bot it's their turn.
     *
     * @param newThread true if this should be done on a new thread; false if
     *                  we're already on a bot's thread, and this is a second or
     *                  later move.
     */
    private void notifyBot(boolean newThread) {
        Player cp, op;
        Bot bot;
        if (currentPlayer.equals(Owner.P1)) {
            cp = game.getPlayer1();
            op = game.getPlayer2();
            bot = game.getPlayer1Bot();
        } else {
            cp = game.getPlayer2();
            op = game.getPlayer1();
            bot = game.getPlayer2Bot();
        }

        final Bot fb = bot;
        final Player fp = new Player(cp);
        final Player fo = new Player(op);
        fo.prepareForOpponent();
        final Board fb2 = new Board(game.getBoard());
        final MoveListener fml = new MoveListenerImpl(currentPlayer);
        if (newThread) {
            Thread botThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    lastBotStart = System.currentTimeMillis();
                    try {
                        fb.takeTurn(fp, fo, fb2, fml);
                    } catch (IllegalPlayException ipe) {
                        fml.concede(fp, ipe.getMessage());
                        return;
                    }
                }
            }, "Bot");
            botThread.start();
        } else {
            try {
                fb.takeTurn(fp, fo, fb2, fml);
            } catch (IllegalPlayException ipe) {
                fml.concede(fp, ipe.getMessage());
            }
        }
    }

    private class MoveListenerImpl implements com.kuhrusty.micropul.MoveListener {
        private boolean calledBack = false;
        private Owner expectPlayer;
        public MoveListenerImpl(Owner who) {
            this.expectPlayer = who;
        }

        @Override
        public void playTile(Player player, Tile tile, int xpos, int ypos) {
            Log.d(LOGBIT, "MoveListener got playTile(tile " +
                    tile.getID() + ", " + tile.getRotation() + ")!!!");
            checkPlayer(player);

            //  Make sure the given player's hand contains the given tile
            player = game.getPlayer(expectPlayer);
            Tile foundTile = game.getPlayer(expectPlayer).removeTileByID(tile.getID());
            if (foundTile == null) {
//XXX i18n
                throw new IllegalPlayException(player.getName() + " doesn't have tile " + tile.getID());
            }
            //  rotate the tile, make sure it's a valid play
            switch (tile.getRotation()) {
                case -1: foundTile.rotateLeft(); break;
                case 2: foundTile.rotateRight();
                case 1: foundTile.rotateRight();
            }
            StringBuilder buf = new StringBuilder();
            if (!game.getBoard().isValidPlay(foundTile, xpos, ypos, buf)) {
                throw new IllegalPlayException("Illegal play: " + buf.toString());
            }

            boolean extraTurn = false;
            TilePlayResult tpr = game.getBoard().playTile(foundTile, xpos, ypos);
            if (tpr != null) {
                for (int ii = 0; ii < tpr.getTiles(); ++ii) {
                    if (game.drawFromCore()) {
                        game.getPlayer(expectPlayer).setTilesInSupply(game.getPlayer(expectPlayer).getTilesInSupply() + 1);
                    }
                }
                if ((game.coreSize() > 0) && (tpr.getExtraTurns() > 0)) {
                    extraTurn = true;
                }
            }
            if (extraTurn) {
                notifyBot(false);
            } else {
                returnToOtherPlayer();
            }
        }

        @Override
        public void drawTile(Player player) {
            Log.d(LOGBIT, "MoveListener got drawTile()!!!");
            checkPlayer(player);
            Player tp = game.getPlayer(expectPlayer);
            if (tp.getTilesInHand() >= 6) {
//XXX i18n
                throw new IllegalPlayException(tp.getName() + " tried to draw with 6 tiles in hand!");
            }
            if (tp.getTilesInSupply() < 1) {
//XXX i18n
                throw new IllegalPlayException(tp.getName() + " tried to draw with no tiles in supply!");
            }
            tp.addToHand(game.draw());
            tp.setTilesInSupply(tp.getTilesInSupply() - 1);
            returnToOtherPlayer();
        }

        @Override
        public void placeStone(Player player, int groupID) {
            Log.d(LOGBIT, "MoveListener got placeStone()!!!");
            checkPlayer(player);
            Player tp = game.getPlayer(expectPlayer);
            int remaining = tp.getStonesRemaining();
            if (remaining < 1) {
//XXX i18n
                throw new IllegalPlayException(tp.getName() + " tried to place a stone, but has none available!");
            }
            Group group = game.getBoard().getGroupByID(groupID);
            if (group.equals(Group.None)) {
//XXX i18n
                throw new IllegalPlayException(tp.getName() + " tried to place a stone, but gave an invalid group!");
            }
            if (!group.getOwner().equals(Owner.Nobody)) {
//XXX i18n
                throw new IllegalPlayException(tp.getName() + " tried to place a stone on a group which is already owned!");
            }
            game.getBoard().playStone(group, expectPlayer);
            tp.setStonesRemaining(remaining - 1);
            returnToOtherPlayer();
        }

        @Override
        public void concede(Player player, final String reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PlayGameActivity.this);
                    builder.setTitle(getString(R.string.concede_title, game.getPlayer(expectPlayer).getName()));
                    if ((reason != null) && (reason.length() > 0)) {
                        builder.setMessage(getString(R.string.concede_reason_message, game.getPlayer(expectPlayer).getName(), reason));
                    } else {
                        builder.setMessage(getString(R.string.concede_message, game.getPlayer(expectPlayer).getName()));
                    }
                    builder.setPositiveButton(R.string.concede_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }

        //  because this is called at the start of every listener method other
        //  than concede(), let's also do the bot sleep here.
        private void checkPlayer(Player gotPlayer) {
            if (calledBack) {
                throw new IllegalPlayException("multiple moves attempted");
            }
            calledBack = true;
            if (!expectPlayer.equals(gotPlayer.getOwner())) {
                throw new IllegalPlayException("expectPlayer " + expectPlayer +
                        ", got " + gotPlayer.getOwner());
            }

            if (botSleepTimeMS > 0) {
                long sleepTime = botSleepTimeMS - (System.currentTimeMillis() - lastBotStart);
                if ((sleepTime > 0) && (sleepTime <= botSleepTimeMS)) {
                    try {
                        Log.d(LOGBIT, "Sleeping for " + sleepTime + "ms");
                        Thread.currentThread().sleep(sleepTime);
                    } catch (InterruptedException ie) {
                    }
                }
            }

        }

        private void returnToOtherPlayer() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (selectedTile != null) {
                        selectedTile.setSelected(false);
                        selectedTile = null;
                    }
                    boardView.setSelectedTile(null);
                    currentPlayer = currentPlayer.equals(Owner.P1) ? Owner.P2 : Owner.P1;
                    updateStatus();
                    if (game.coreSize() == 0) {
                        gameOver();
                        return;
                    }
                }
            });
        }
    }
}
