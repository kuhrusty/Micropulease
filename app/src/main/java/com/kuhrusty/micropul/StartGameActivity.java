package com.kuhrusty.micropul;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.kuhrusty.micropul.renderer.BWRenderer;
import com.kuhrusty.micropul.renderer.CarthaginianRenderer1;
import com.kuhrusty.micropul.renderer.ClassicRenderer;
import com.kuhrusty.micropul.renderer.StickMudRenderer1;
import com.kuhrusty.micropul.renderer.StickMudRenderer2;
import com.kuhrusty.micropul.util.Leaderboard;
import com.kuhrusty.micropul.util.LeaderboardRepository;

/**
 * A not-very-pretty activity which just gathers the information needed to
 * launch PlayGameActivity.
 */
public class StartGameActivity extends AppCompatActivity {
    private static final String LOGBIT = "StartGameActivity";

    private static final int RESULT_SAVE_SCORE = RESULT_FIRST_USER + 1;

    private static TileRenderer[] renderers = null;

    static TileRenderer[] getTileRenderers(Context context) {
        if (renderers == null) {
            renderers = new TileRenderer[] {
                    new ClassicRenderer(context),
                    new BWRenderer(context.getResources()),
                    new CarthaginianRenderer1(context.getResources()),
                    new StickMudRenderer1(context),
                    new StickMudRenderer2(context),
            };
        }
        return renderers;
    }

    private PlayerType selectedPlayerType = null;
    private TileRenderer selectedRenderer = null;
    private Leaderboard leaderboard = null;
    private Leaderboard.Highlight highlight = null;

    private static final String PREF_P1_NAME = "PREF_P1_NAME";
    private static final String PREF_P2_NAME = "PREF_P2_NAME";
    private static final String PREF_P2_TYPE = "PREF_P2_TYPE";
    static final String PREF_RENDERER = "PREF_RENDERER";
    private String prefP1Name = null;
    private String prefP2Name = null;
    private String prefP2Type = null;
    private String prefRenderer = null;

    /**
     * Attempts to load the last saved player names, player type, and renderer
     * from preferences.
     */
    private void loadPrefs() {
        prefP1Name = getResources().getString(R.string.p1_default_name);
        prefP2Name = getResources().getString(R.string.p2_default_name);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp != null) {
            prefP1Name = sp.getString(PREF_P1_NAME, prefP1Name);
            prefP2Name = sp.getString(PREF_P2_NAME, prefP2Name);
            prefP2Type = sp.getString(PREF_P2_TYPE, null);
            prefRenderer = sp.getString(PREF_RENDERER, null);
        }
    }

    private void savePrefs() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp != null) {
            SharedPreferences.Editor pe = sp.edit();
            pe.putString(PREF_P1_NAME, prefP1Name);
            pe.putString(PREF_P2_NAME, prefP2Name);
            pe.putString(PREF_P2_TYPE, prefP2Type);
            pe.putString(PREF_RENDERER, prefRenderer);
            pe.apply();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_game);

        loadPrefs();

        ((TextView)findViewById(R.id.nameText)).setText(prefP1Name);
        ((TextView)findViewById(R.id.p2NameText)).setText(prefP2Name);

        Spinner opponentType = findViewById(R.id.p2TypeSpinner);
        //  blugh.
        ArrayAdapter<PlayerType> ptAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, PlayerType.getPlayerTypes(getResources()));
        ptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        opponentType.setAdapter(ptAdapter);
        opponentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                Log.d(LOGBIT, "opponentType item selected");
                PlayerType[] pta = PlayerType.getPlayerTypes(getResources());
                if (pos < pta.length) {
                    selectedPlayerType = pta[pos];
                    TextView tv = findViewById(R.id.p2TypeDescr);
                    tv.setText(selectedPlayerType.getDescription());
                    findViewById(R.id.p2NameLabel).setVisibility(selectedPlayerType.isHotSeat() ? View.VISIBLE : View.GONE);
                    findViewById(R.id.p2NameText).setVisibility(selectedPlayerType.isHotSeat() ? View.VISIBLE : View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(LOGBIT, "opponentType, no item selected");
                selectedPlayerType = null;
            }
        });
        if (prefP2Type != null) {
            for (int ii = 0; ii < ptAdapter.getCount(); ++ii) {
                if (prefP2Type.equals(ptAdapter.getItem(ii).toString())) {
                    opponentType.setSelection(ii);
                    break;
                }
            }
        }

        final ImageView preview = findViewById(R.id.themeSample);
        Spinner renderer = findViewById(R.id.themeSpinner);
        //  blugh.
        ArrayAdapter<TileRenderer> trAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getTileRenderers(this));
        trAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        renderer.setAdapter(trAdapter);
        renderer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                Log.d(LOGBIT, "renderer item selected");
                TileRenderer[] tra = getTileRenderers(StartGameActivity.this);
                if (pos < tra.length) {
                    selectedRenderer = tra[pos];
                    int id = selectedRenderer.getPreviewDrawableID();
                    if (id == 0) id = R.drawable.preview_none;
                    preview.setImageDrawable(ContextCompat.getDrawable(StartGameActivity.this, id));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(LOGBIT, "renderer, no item selected");
                selectedRenderer = null;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //  Did they change renderers during the game?
        loadPrefs();
        Spinner renderer = findViewById(R.id.themeSpinner);
        if ((renderer != null) && (prefRenderer != null)) {
            for (int ii = 0; ii < renderer.getCount(); ++ii) {
                if (prefRenderer.equals(renderer.getAdapter().getItem(ii).toString())) {
                    renderer.setSelection(ii);
                    break;
                }
            }
        }
    }

    public void doStartGame(View view) {
        //  get the player name, opponent type, possibly opponent name, theme
        prefP1Name = ((EditText)findViewById(R.id.nameText)).getText().toString();
        prefP2Name = ((EditText)findViewById(R.id.p2NameText)).getText().toString();

        Intent intent = new Intent(this, PlayGameActivity.class);
        intent.putExtra(PlayGameActivity.INTENT_PLAYER1_NAME, prefP1Name);
        String name2 = prefP2Name;
        if (selectedPlayerType != null) {
            prefP2Type = selectedPlayerType.toString();
            intent.putExtra(PlayGameActivity.INTENT_PLAYER2_TYPE, prefP2Type);
            if (selectedPlayerType.isBot()) name2 = selectedPlayerType.toString();
        }
        intent.putExtra(PlayGameActivity.INTENT_PLAYER2_NAME, name2);
        if (selectedRenderer != null) {
            prefRenderer = selectedRenderer.toString();
            intent.putExtra(PlayGameActivity.INTENT_RENDERER_NAME, prefRenderer);
        }
        savePrefs();
        //  We start the PlayGameActivity, telling it we want the final scores
        //  back; when we receive them, we see if they're good enough to add to
        //  the leaderboard, and then launch the LeaderboardActivity if so.
        startActivityForResult(intent, RESULT_SAVE_SCORE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_SAVE_SCORE) {
            if (resultCode == RESULT_OK) {
                String p1n = data.getStringExtra(PlayGameActivity.INTENT_PLAYER1_NAME);
                int p1s = data.getIntExtra(PlayGameActivity.INTENT_PLAYER1_SCORE, 0);
                String p2n = data.getStringExtra(PlayGameActivity.INTENT_PLAYER2_NAME);
                int p2s = data.getIntExtra(PlayGameActivity.INTENT_PLAYER2_SCORE, 0);
                long now = System.currentTimeMillis();
                Log.d(LOGBIT, "RESULT_SAVE_SCORE RESULT_OK, p1 " + p1n +
                        " " + p1s + ", p2 " + p2n + " " + p2s);
                if (p1n == null) {
                    Log.d(LOGBIT, "got null player1 name, no point continuing");
                    return;
                }
                if (leaderboard == null) {
                    loadLeaderboard();
                }
                Leaderboard.Entry le = (p2n != null) ?
                        new Leaderboard.Entry(p1n, p1s, p2n, p2s, now) :
                        new Leaderboard.Entry(p1n, p1s, now);
                highlight = leaderboard.add(le);
                if (highlight != null) {
                    //  The file should be small, but still probably shouldn't
                    //  do this on the UI thread.
                    new LeaderboardRepository(this).save(leaderboard);
                    openLeaderboard(null);
                }
                //  else they didn't make the cut.
            } else {
                Log.d(LOGBIT, "RESULT_SAVE_SCORE, instead of RESULT_OK, got " + resultCode);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Called when the Leaderboard button is hit, or when we get a result back
     */
    public void openLeaderboard(View view) {
        if (leaderboard == null) loadLeaderboard();
        Intent intent = new Intent(this, LeaderboardActivity.class);
        intent.putExtra(LeaderboardActivity.INTENT_LEADERBOARD, leaderboard);
        if (highlight != null) {
            intent.putExtra(LeaderboardActivity.INTENT_HIGHLIGHT, highlight);
        }
        startActivity(intent);
    }

    /**
     * Called when the Help button is hit.
     */
    public void openHelp(View view) {
        startActivity(new Intent(this, HelpActivity.class));
    }

    /**
     * This just attempts to read the Leaderboard from file, and returns a new
     * empty one if that fails.  Rather than doing this on the UI thread in
     * onActivityResult() and openLeaderboard(), we should probably load this on
     * a new thread in onCreate().
     */
    private void loadLeaderboard() {
        if (leaderboard == null) {
            leaderboard = new LeaderboardRepository(this).load();
            if (leaderboard == null) {
                Log.w(LOGBIT, "no leaderboard loaded, starting new one");
                leaderboard = new Leaderboard();
            }
        }
    }
}
