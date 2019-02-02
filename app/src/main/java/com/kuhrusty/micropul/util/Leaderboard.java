package com.kuhrusty.micropul.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * Stores the top n scores.
 *
 * <p>This started off real simple; then I got a little carried away and started
 * storing high scores, solitaire high scores, worst beatdowns, and per-player
 * high scores.</p>
 */
public class Leaderboard implements Parcelable {
    /**
     * One entry (players, scores, dates) in a leaderboard.
     */
    public static class Entry {
        private String p1;
        private String p2;
        private int p1Score;
        private int p2Score;
        private long date;

        /**
         * Creates a new solitaire entry.
         * @param name must not be null.
         * @param score
         * @param date ms since 1/1/70 UTC.
         */
        public Entry(String name, int score, long date) {
            if (name == null) {
                throw new NullPointerException("name");
            }
            p1 = name;
            p1Score = score;
            this.date = date;
        }

        /**
         * Creates a new two-player entry.  After being passed in, the player
         * with the higher score will be player 1, and the lower score will be
         * player 2.  If they're tied, the order will stay the same.
         *
         * @param p1Name must not be null.
         * @param p1Score
         * @param p2Name must not be nul.
         * @param p2Score
         * @param date ms since 1/1/70 UTC.
         */
        public Entry(String p1Name, int p1Score, String p2Name, int p2Score,
                     long date) {
            if (p1Name == null) throw new NullPointerException("p1Name");
            if (p2Name == null) throw new NullPointerException("p2Name");
            if (p2Score > p1Score) {
                //  swap them
                this.p1 = p2Name;
                this.p1Score = p2Score;
                this.p2 = p1Name;
                this.p2Score = p1Score;
            } else {
                this.p1 = p1Name;
                this.p1Score = p1Score;
                this.p2 = p2Name;
                this.p2Score = p2Score;
            }
            this.date = date;
        }

        //  careful, this does not implement Parcelable.
        private Entry(Parcel in) {
            p1 = in.readString();
            p2 = in.readString();
            p1Score = in.readInt();
            p2Score = in.readInt();
            date = in.readLong();
        }
        private void writeToParcel(Parcel out) {
            out.writeString(p1);
            out.writeString(p2);
            out.writeInt(p1Score);
            out.writeInt(p2Score);
            out.writeLong(date);
        }

        /**
         * Returns a new entry with player 1 and player 2 switched, even if
         * player 2 had a lower score.  Turns out that clever behavior in the
         * 2-player constructor was not always helpful.
         */
        Entry swap() {
            if (p2 == null) throw new NullPointerException();
            Entry rv = new Entry(p2, p2Score, date);
            rv.p2 = p1;
            rv.p2Score = p1Score;
            return rv;
        }

        public boolean isSolitaire() {
            return (p2 == null);
        }
        public String getP1Name() {
            return p1;
        }
        public int getP1Score() {
            return p1Score;
        }
        /**
         * Returns null if this is a solitaire game.
         */
        public String getP2Name() {
            return p2;
        }
        /**
         * Probably returns 0 if this is a solitaire game.
         */
        public int getP2Score() {
            return p2Score;
        }
        /**
         * Returns the difference between the two players' scores.
         */
        public int getBeatdown() {
            return p1Score - p2Score;
        }
        public long getDate() {
            return date;
        }

        /**
         * Used for dumping contents in unit tests, not user-visible stuff.
         */
        @Override
        public String toString() {
            return p1 + " " + p1Score + " " + (isSolitaire() ? "" :
                    (p2 + " " + p2Score + " ")) + date;
        }

        /**
         * Used in unit tests.
         */
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Entry)) return false;
            //  bad, but like I said, just for unit tests
            return toString().equals(other.toString());
        }
    }

    /**
     * When a new Entry is added to the leaderboard, this will be returned to
     * tell you where it wound up.
     */
    public static class Highlight implements Parcelable {
        private int solitaireRow = -1;
        private int highScoreRow = -1;
        private int beatdownRow = -1;
        private int personal1Row = -1;
        private int personal2Row = -1;
        private String personal1Name = null;
        private String personal2Name = null;

        /**
         * Normally you should get these guys from Leaderboard.add(), but this
         * creates an empty one.
         */
        public Highlight() {
        }

        /**
         * Returns the index of the row which was just added to the solitaire
         * leader board (0 is highest score), or -1 if no row was just added.
         */
        public int getSolitaireHighScoreRow() {
            return solitaireRow;
        }
        /**
         * Returns the index of the row which was just added to the 2-player
         * leader board (0 is highest score), or -1 if no row was just added.
         */
        public int getHighScoreRow() {
            return highScoreRow;
        }
        /**
         * Returns the index of the row which was just added to the beatdown
         * leader board (0 is highest score), or -1 if no row was just added.
         */
        public int getBeatdownRow() {
            return beatdownRow;
        }

        /**
         * Returns the index of the row which was just added to the given
         * player's personal high scores (0 is highest score), or -1 if no row
         * was just added.
         */
        public int getPersonalBestRow(String name) {
            if (name.equals(personal1Name)) return personal1Row;
            if (name.equals(personal2Name)) return personal2Row;
            return -1;
        }

        /**
         * Used for dumping contents in unit tests, not user-visible stuff.
         */
        @Override
        public String toString() {
            return "Highlight[1p " + solitaireRow + ",2p " + highScoreRow +
                    ",b " + beatdownRow +
                    ((personal1Name != null) ?
                        ("," + personal1Name + " " + personal1Row) : "") +
                    ((personal2Name != null) ?
                            ("," + personal2Name + " " + personal2Row) : "") + "]";
        }

        /**
         * Used in unit tests.
         */
        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Highlight)) return false;
            Highlight oh = (Highlight)other;
            return (solitaireRow == oh.solitaireRow) &&
                   (highScoreRow == oh.highScoreRow) &&
                   (beatdownRow == oh.beatdownRow) &&
                   ((personal1Name == oh.personal1Name) ||
                           ((personal1Name != null) && personal1Name.equals(oh.personal1Name))) &&
                   ((personal2Name == oh.personal2Name) ||
                           ((personal2Name != null) && personal2Name.equals(oh.personal2Name))) &&
                   (personal1Row == oh.personal1Row) &&
                   (personal2Row == oh.personal2Row);
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(solitaireRow);
            parcel.writeInt(highScoreRow);
            parcel.writeInt(beatdownRow);
            parcel.writeInt(personal1Row);
            if (personal1Row != -1) parcel.writeString(personal1Name);
            parcel.writeInt(personal2Row);
            if (personal2Row != -1) parcel.writeString(personal2Name);
        }
        public static final Creator<Highlight> CREATOR = new Creator<Highlight>() {
            public Highlight createFromParcel(Parcel in) {
                Highlight rv = new Highlight();
                rv.solitaireRow = in.readInt();
                rv.highScoreRow = in.readInt();
                rv.beatdownRow = in.readInt();
                rv.personal1Row = in.readInt();
                if (rv.personal1Row != -1) rv.personal1Name = in.readString();
                rv.personal2Row = in.readInt();
                if (rv.personal2Row != -1) rv.personal2Name = in.readString();
                return rv;
            }
            public Highlight[] newArray(int size) {
                return new Highlight[size];
            }
        };
        @Override
        public int describeContents() {
            return 0;
        }
    }

    /**
     * Orders Entries by score, high score first; ties broken by older date.
     */
    public static final Comparator<Entry> ScoreComparator = new Comparator<Entry>() {
        @Override
        public int compare(Entry le, Entry re) {
            if (le.getP1Score() < re.getP1Score()) return -1;
            if (le.getP1Score() > re.getP1Score()) return 1;
            //  scores are the same, so compare dates.  Note that smaller dates
            //  are older, and therefore greater.
            if (le.getDate() < re.getDate()) return 1;
            if (le.getDate() > re.getDate()) return -1;
            return 0;
        }
    };

    /**
     * Orders Entries by difference in score, high difference first; ties broken
     * by older date.
     */
    public static final Comparator<Entry> BeatdownComparator = new Comparator<Entry>() {
        @Override
        public int compare(Entry le, Entry re) {
            int lds = le.getP1Score() - le.getP2Score();
            int rds = re.getP1Score() - re.getP2Score();
            if (lds < rds) return -1;
            if (lds > rds) return 1;
            //  scores are the same, so compare dates.  Note that smaller dates
            //  are older, and therefore greater.
            if (le.getDate() < re.getDate()) return 1;
            if (le.getDate() > re.getDate()) return -1;
            return 0;
        }
    };

    /**
     * Creates a new, empty Leaderboard.
     */
    public Leaderboard() {
    }

    //  how many scores in each category we'll store.
    private int limit = 10;
    private ArrayList<Entry> solitaire = new ArrayList<>(limit + 1);
    private ArrayList<Entry> highScores = new ArrayList<>(limit + 1);
    private ArrayList<Entry> beatdowns = new ArrayList<>(limit + 1);
    private TreeMap<String, List<Entry>> personal = new TreeMap<>();

    /**
     * How many scores in each category we'll store.
     */
    public int getLimit() {
        return limit;
    }
    /**
     * How many scores in each category we'll store.  If the new limit is
     * greater than the  number of scores we have in some category, the excess
     * will be discarded.
     */
    public void setLimit(int limit) {
        if (limit < this.limit) {
            //  no ArrayList method for trimming to size!?
            trim(solitaire, limit);
            trim(highScores, limit);
            trim(beatdowns, limit);
            for (List<Entry> tl : personal.values()) trim(tl, limit);
        }
        this.limit = limit;
    }

    private static void trim(List list, int size) {
        if (list == null) return;
        for (int ii = list.size() - 1; ii >= size; --ii) {
            list.remove(ii);
        }
    }

    /**
     * Adds a new Entry to the Leaderboard.  If it's not a high enough score, it
     * will be discarded.
     *
     * @param entry must not be null.
     * @return a Highlight showing which row(s) in the Leaderboard changed, null
     *         if the entry was discarded because the leaderboard was already at
     *         its limit and this entry was not a high enough score to record.
     */
    public Highlight add(Entry entry) {
        if (entry.isSolitaire()) {
            return addToList(solitaire, null, ScoreComparator, entry, null);
        }
        Highlight rv = addToList(highScores, null, ScoreComparator, entry, null);
        rv = addToList(beatdowns, null, BeatdownComparator, entry, rv);
        rv = addToList(getOrCreatePersonalList(entry.p1), entry.p1, ScoreComparator, entry, rv);
        rv = addToList(getOrCreatePersonalList(entry.p2), entry.p2, ScoreComparator, entry.swap(), rv);
        return rv;
    }

    private List<Entry> getOrCreatePersonalList(String name) {
        List<Entry> rv = personal.get(name);
        if (rv == null) {
            rv = new ArrayList<>(limit + 1);
            personal.put(name, rv);
        }
        return rv;
    }

    /**
     * Discards the score if it's not high enough to make the list.
     *
     * @param list the list we're considering adding to.
     * @param personalBestName non-null if this is a personal-best list.
     * @param cmp used to figure out whether this entry makes the list.
     * @param entry this is what we're adding to the list.
     * @param rv the Hightlight to add to, or null.
     * @return the Highlight which was passed in, or a new one if necessary, or
     *         null.
     */
    private Highlight addToList(List<Entry> list, String personalBestName,
                                Comparator<Entry> cmp, Entry entry, Highlight rv) {
        if (limit < 1) return rv;
        //  find out where in the list it should go
        int insertAt = list.size();
        while (insertAt > 0) {
            if (cmp.compare(list.get(insertAt - 1), entry) >= 0) break;
            --insertAt;
        }
        //  If we go after the last element, and the list is already full, bail.
        if ((insertAt >= limit)) return rv;
        list.add(insertAt, entry);
        if (list.size() > limit) list.remove(list.size() - 1);
        if (rv == null) rv = new Highlight();
        //  well, this is rough.  I miss pointers and pointer-to-member.
        if (personalBestName != null) {
            if (rv.personal1Row == -1) {
                rv.personal1Name = personalBestName;
                rv.personal1Row = insertAt;
            } else {
                rv.personal2Name = personalBestName;
                rv.personal2Row = insertAt;
            }
        } else if (list == solitaire) rv.solitaireRow = insertAt;
        else if (list == highScores) rv.highScoreRow = insertAt;
        else if (list == beatdowns) rv.beatdownRow = insertAt;
        else throw new RuntimeException("BRAIN DAMAGE, can't figure out list");
        return rv;
    }

    /**
     * Returns the list of high solitaire scores in order, or an empty list,
     * never null.  Note that the returned list is unmodifiable.
     */
    public List<Entry> getSolitaireHighScores() {
        return Collections.unmodifiableList(solitaire);
    }

    /**
     * Returns the list of high 2-player scores in order, or an empty list,
     * never null.  Note that the returned list is unmodifiable.
     */
    public List<Entry> getHighScores() {
        return Collections.unmodifiableList(highScores);
    }

    /**
     * Returns the list of worst beatdowns in order, or an empty list, never
     * null.  Note that the returned list is unmodifiable.
     */
    public List<Entry> getBeatdowns() {
        return Collections.unmodifiableList(beatdowns);
    }

    /**
     * Returns the list of personal best scores for the given player name, or an
     * empty list, never null.  Note that the returned list is unmodifiable.
     *
     * @param playerName must not be null.
     */
    public List<Entry> getPersonalBest(String playerName) {
        List<Entry> rv = personal.get(playerName);
        return (rv != null) ? Collections.unmodifiableList(rv) : Collections.EMPTY_LIST;
    }

    /**
     * Returns the set of players who have a list of personal best scores, or an
     * empty set; never null.
     */
    public Set<String> getPersonalBestNames() {
        return personal.keySet();
    }

    /**
     * Used when reading from file.  This doesn't check our size limit.
     */
    void addSolitaireHighScore(Entry entry) {
        if (!entry.isSolitaire()) throw new IllegalArgumentException();
        solitaire.add(entry);
    }

    /**
     * Used when reading from file.  This doesn't check our size limit.
     */
    void addHighScore(Entry entry) {
        if (entry.isSolitaire()) throw new IllegalArgumentException();
        highScores.add(entry);
    }

    /**
     * Used when reading from file.  This doesn't check our size limit.
     */
    void addBeatdown(Entry entry) {
        if (entry.isSolitaire()) throw new IllegalArgumentException();
        beatdowns.add(entry);
    }

    /**
     * Used when reading from file.  This doesn't check our size limit.
     */
    void addPersonalBest(Entry entry) {
        if (entry.isSolitaire()) throw new IllegalArgumentException();
        List<Entry> tl = getOrCreatePersonalList(entry.p1);
        tl.add(entry);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(limit);
        writeEntries(solitaire, parcel);
        writeEntries(highScores, parcel);
        writeEntries(beatdowns, parcel);
        parcel.writeInt(personal.size());
        for (String name : personal.keySet()) {
            parcel.writeString(name);
            writeEntries(personal.get(name), parcel);
        }
    }
    public static final Creator<Leaderboard> CREATOR = new Creator<Leaderboard>() {
        public Leaderboard createFromParcel(Parcel in) {
            Leaderboard rv = new Leaderboard();
            rv.limit = in.readInt();
            readEntries(rv.solitaire, in);
            readEntries(rv.highScores, in);
            readEntries(rv.beatdowns, in);
            int nc = in.readInt();
            for (int ii = 0; ii < nc; ++ii) {
                String name = in.readString();
                List<Entry> tl = new ArrayList<Entry>(rv.limit + 1);
                rv.personal.put(name, tl);
                readEntries(tl, in);
            }
            return rv;
        }
        public Leaderboard[] newArray(int size) {
            return new Leaderboard[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }
    private static void writeEntries(List<Entry> list, Parcel out) {
        out.writeInt(list.size());
        for (int ii = 0; ii < list.size(); ++ii) {
            list.get(ii).writeToParcel(out);
        }
    }
    private static void readEntries(List<Entry> list, Parcel in) {
        int count = in.readInt();
        list.clear();
        for (int ii = 0; ii < count; ++ii) {
            list.add(new Entry(in));
        }
    }
}
