package com.kuhrusty.micropul.util;

import android.content.Context;
import android.util.Log;

import com.kuhrusty.micropul.util.Leaderboard.Entry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Reads & writes the Leaderboard file.
 */
public class LeaderboardRepository {
    private static final String LOGBIT = "LeaderboardRepository";

    private Context context;
    private String filename = "leaderboard.txt";

    private static final String PF_SCORES = "2";
    private static final String PF_BEATDOWNS = "b";
    private static final String PF_SOLITAIRES = "1";
    private static final String PF_PERSONAL = "p";

    /**
     *
     * @param context probably used for accessing the filesystem.  Not sure how
     *                long you can hold on to these guys, so you probably don't
     *                want to hold on to the LeaderboardRepository for very
     *                long, either.
     */
    public LeaderboardRepository(Context context) {
        this.context = context;
    }

    /**
     *
     * @param context probably used for accessing the filesystem.  Not sure how
     *                long you can hold on to these guys, so you probably don't
     *                want to hold on to the LeaderboardRepository for very
     *                long, either.
     * @param filename alternate name to use.  You probably only want to use
     *                 this one in unit tests.
     */
    public LeaderboardRepository(Context context, String filename) {
        this.context = context;
        this.filename = filename;
    }

    public void save(Leaderboard toSave) {
        Writer out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    context.openFileOutput(filename, Context.MODE_PRIVATE)));
            save(toSave, out);
        } catch (IOException ioe) {
            Log.w(LOGBIT, ioe);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ioe) {
                    Log.w(LOGBIT, ioe);  //  care *some*, as write may be bad
                }
            }
        }
    }

    /**
     * This doesn't close the Writer.
     */
    protected void save(Leaderboard toSave, Writer out) throws IOException {
        out.write("//  Micropulease leaderboard file; might get overwritten!\n");
        for (Entry te : toSave.getHighScores()) write(out, PF_SCORES, te);
        for (Entry te : toSave.getBeatdowns()) write(out, PF_BEATDOWNS, te);
        for (Entry te : toSave.getSolitaireHighScores()) write(out, PF_SOLITAIRES, te);
        for (String name : toSave.getPersonalBestNames()) {
            for (Entry te : toSave.getPersonalBest(name)) write(out, PF_PERSONAL, te);
        }
    }

    //  writes a single entry.
    private void write(Writer out, String prefix, Entry te) throws IOException {
        out.write(prefix + "\t" +
                te.getP1Name().replaceAll("\\s+", " ") + "\t" +
                te.getP1Score() + "\t");
        if (!te.isSolitaire()) {
            out.write(te.getP2Name().replaceAll("\\s+", " ") + "\t" +
                    te.getP2Score() + "\t");
        }
        out.write(te.getDate() + "\n");
    }

    public Leaderboard load() {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(context.openFileInput(filename).getFD()));
            return load(in);
        } catch (Exception ex) {
            if (!(ex instanceof FileNotFoundException)) {
                Log.e(LOGBIT, "couldn't read leaderboard from file", ex);
            }
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    Log.w(LOGBIT, ioe);  //  don't care
                }
            }
        }
    }

    protected Leaderboard load(BufferedReader in) throws IOException {
        Leaderboard rv = new Leaderboard();
        String line;
        while ((line = in.readLine()) != null) {
            if (line.startsWith("//")) continue;  //  comment
            String[] bits = line.trim().split("\\t", 6);
            boolean expectSolitaire = bits[0].equals(PF_SOLITAIRES);
            if (bits.length != (expectSolitaire ? 4 : 6)) {
                Log.w(LOGBIT, "ignoring line with confusing number of bits");
                continue;
            }
            int p1Score, p2Score = 0;
            long date;
            try {
                p1Score = Integer.parseInt(bits[2]);
                if (!expectSolitaire) p2Score = Integer.parseInt(bits[4]);
                date = Long.parseLong(bits[expectSolitaire ? 3 : 5]);
            } catch (NumberFormatException nfe) {
                Log.w(LOGBIT, "ignoring line with non-numeric score or date");
                continue;
            }
            if (expectSolitaire) {
                rv.addSolitaireHighScore(new Entry(bits[1], p1Score, date));
            } else {
                Entry te = new Entry(bits[1], p1Score, bits[3], p2Score, date);
                if (bits[0].equals(PF_SCORES)) rv.addHighScore(te);
                else if (bits[0].equals(PF_BEATDOWNS)) rv.addBeatdown(te);
                else if (bits[0].equals(PF_PERSONAL)) {
                    //  Entry constructor may have helpfully switched the order for
                    //  us.
                    if (te.getP2Name().equals(bits[1])) te = te.swap();
                    rv.addPersonalBest(te);
                } else Log.w(LOGBIT, "ignoring line with unexpected prefix");
            }
        }
        return rv;
    }
}
