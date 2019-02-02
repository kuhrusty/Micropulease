package com.kuhrusty.micropul;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.kuhrusty.micropul.util.Leaderboard;
import com.kuhrusty.micropul.util.LeaderboardHTML;

/**
 * This doesn't read or write the leaderboard; all it does is display what it
 * was passed in its intent.
 */
public class LeaderboardActivity extends AppCompatActivity {
    private static final String LOGBIT = "LeaderboardActivity";

    public static final String INTENT_LEADERBOARD = "LeaderboardActivity.leaderboard";
    public static final String INTENT_HIGHLIGHT = "LeaderboardActivity.highlight";

    private Leaderboard leaderboard;
    private Leaderboard.Highlight highlight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        Intent intent = getIntent();
        if (intent != null) {
            leaderboard = intent.getParcelableExtra(INTENT_LEADERBOARD);
            //  we really want a leaderboard, but it's fine if the highlight
            //  is null.
            highlight = intent.getParcelableExtra(INTENT_HIGHLIGHT);
        }

        WebView wv = findViewById(R.id.webView);
        if (leaderboard != null) {
            LeaderboardHTML lhtml = new LeaderboardHTML();
            String html = lhtml.toHTML(this, leaderboard, highlight);
            //Log.d(LOGBIT, "html:\n" + html);
            //wv.loadData(html, "text/html", null);
            //  No idea why that stopped working & had to be replaced with this:
            wv.loadDataWithBaseURL(null, html, "text/html", null, null);
        }
    }
}
