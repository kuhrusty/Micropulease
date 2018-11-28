package com.kuhrusty.micropul;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
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

/**
 * A not-very-pretty activity which just gathers the information needed to
 * launch PlayGameActivity.
 */
public class StartGameActivity extends AppCompatActivity {
    private static final String LOGBIT = "StartGameActivity";

    private static TileRenderer[] renderers = null;

    static TileRenderer[] getTileRenderers(Resources res) {
        if (renderers == null) {
            renderers = new TileRenderer[] {
                    new BWRenderer(res),
                    new CarthaginianRenderer1(res),
            };
        }
        return renderers;
    }

    private PlayerType selectedPlayerType = null;
    private TileRenderer selectedRenderer = null;

    private static final String PREF_P1_NAME = "PREF_P1_NAME";
    private static final String PREF_P2_NAME = "PREF_P2_NAME";
    private static final String PREF_P2_TYPE = "PREF_P2_TYPE";
    private static final String PREF_RENDERER = "PREF_RENDERER";
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

        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        if (sp != null) {
            prefP1Name = sp.getString(PREF_P1_NAME, prefP1Name);
            prefP2Name = sp.getString(PREF_P2_NAME, prefP2Name);
            prefP2Type = sp.getString(PREF_P2_TYPE, null);
            prefRenderer = sp.getString(PREF_RENDERER, null);
        }
    }

    private void savePrefs() {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
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

//erkk, how do we restore the selected p2type & renderer from saved prefs?
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

        final ImageView preview = findViewById(R.id.themeSample);
        Spinner renderer = findViewById(R.id.themeSpinner);
        //  blugh.
        ArrayAdapter<TileRenderer> trAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getTileRenderers(getResources()));
        trAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        renderer.setAdapter(trAdapter);
        renderer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                Log.d(LOGBIT, "renderer item selected");
                TileRenderer[] tra = getTileRenderers(getResources());
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
        //startActivityForResult(intent, RESULT_SAVE_SCORE_OR_WHATEVER;
        startActivity(intent);
    }
}
