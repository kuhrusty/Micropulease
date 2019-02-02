package com.kuhrusty.micropul.util;

import android.content.Context;
import android.text.format.DateFormat;

import com.kuhrusty.micropul.R;
import com.kuhrusty.micropul.TestUtil;
import com.kuhrusty.mockparcel.MockParcel;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.kuhrusty.micropul.util.Leaderboard.Entry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({DateFormat.class})
public class LeaderboardTest {

    @Rule
    public ExpectedException expectThrown = ExpectedException.none();

    @Test
    public void testImmutableLists() {
        expectThrown.expect(UnsupportedOperationException.class);

        Leaderboard lb = new Leaderboard();
        lb.getBeatdowns().add(new Entry("Rusty", 666, 0l));
    }

    @Test
    public void testNullP1Name() {
        expectThrown.expect(NullPointerException.class);
        expectThrown.expectMessage("name");

        new Entry(null, 666, 0l);
    }

    @Test
    public void testSort() throws Exception {
        Leaderboard lb = new Leaderboard();
        assertEquals(10, lb.getLimit());
        assertEquals(0, lb.getSolitaireHighScores().size());
        assertEquals(0, lb.getHighScores().size());
        assertEquals(0, lb.getBeatdowns().size());
        checkParcel(lb);

        checkAdd(lb, new Entry("Rusty", 15, "DumbBot 1.0", 6, 1548598860000L), 0, 0);
        assertEquals(0, lb.getSolitaireHighScores().size());
        assertEquals(1, lb.getHighScores().size());
        assertEquals(1, lb.getBeatdowns().size());
        checkParcel(lb);

        checkAdd(lb, new Entry("Rusty", 21, "DumbBot 1.0", 18, 1548599280000L), 0, 1);
        assertEquals(0, lb.getSolitaireHighScores().size());
        assertEquals(2, lb.getHighScores().size());
        assertEquals(2, lb.getBeatdowns().size());
        checkParcel(lb);

        checkAdd(lb, new Entry("Rusty", 17, "DumbBot 1.0", 1, 1548599340000L), 1, 0);
        assertEquals(0, lb.getSolitaireHighScores().size());
        assertEquals(3, lb.getHighScores().size());
        assertEquals(3, lb.getBeatdowns().size());
        checkParcel(lb);

        check(lb.getHighScores(), false,
                "Rusty", 21, "DumbBot 1.0", 18, 1548599280000L,
                "Rusty", 17, "DumbBot 1.0", 1, 1548599340000L,
                "Rusty", 15, "DumbBot 1.0", 6, 1548598860000L);
        check(lb.getBeatdowns(), false,
                "Rusty", 17, "DumbBot 1.0", 1, 1548599340000L,
                "Rusty", 15, "DumbBot 1.0", 6, 1548598860000L,
                "Rusty", 21, "DumbBot 1.0", 18, 1548599280000L);

        checkAdd(lb, new Entry("Rusty", 15, "DumbBot 1.0", 6, 1548599760000L), 3, 2);
        checkAdd(lb, new Entry("Rusty", 21, "DumbBot 1.0", 18, 1548599820000L), 1, 4);
        checkAdd(lb, new Entry("Rusty", 17, "DumbBot 1.0", 1, 1548599880000L), 3, 1);
        check(lb.getHighScores(), false,
                "Rusty", 21, "DumbBot 1.0", 18, 1548599280000L,
                "Rusty", 21, "DumbBot 1.0", 18, 1548599820000L,
                "Rusty", 17, "DumbBot 1.0", 1, 1548599340000L,
                "Rusty", 17, "DumbBot 1.0", 1, 1548599880000L,
                "Rusty", 15, "DumbBot 1.0", 6, 1548598860000L,
                "Rusty", 15, "DumbBot 1.0", 6, 1548599760000L);
        check(lb.getBeatdowns(), false,
                "Rusty", 17, "DumbBot 1.0", 1, 1548599340000L,
                "Rusty", 17, "DumbBot 1.0", 1, 1548599880000L,
                "Rusty", 15, "DumbBot 1.0", 6, 1548598860000L,
                "Rusty", 15, "DumbBot 1.0", 6, 1548599760000L,
                "Rusty", 21, "DumbBot 1.0", 18, 1548599280000L,
                "Rusty", 21, "DumbBot 1.0", 18, 1548599820000L);
        checkParcel(lb);

        lb.setLimit(3);
        assertEquals(3, lb.getLimit());
        assertEquals(3, lb.getBeatdowns().size());
        checkParcel(lb);
        //  Add an entry; there should be no room for it.
        Entry te = new Entry("Rusty", 12, "OptiBot II", 13, 1548600000000L);
        checkAdd(lb, te, -1, -1);
        assertEquals(3, lb.getBeatdowns().size());
        lb.setLimit(6);
        //  Add the same entry; now there should be room.
        checkAdd(lb, te, 3, 3);
        assertEquals(4, lb.getBeatdowns().size());
        checkParcel(lb);
        lb.setLimit(4);
        check(lb.getHighScores(), false,
                "Rusty", 21, "DumbBot 1.0", 18, 1548599280000L,
                "Rusty", 21, "DumbBot 1.0", 18, 1548599820000L,
                "Rusty", 17, "DumbBot 1.0", 1, 1548599340000L,
                "OptiBot II", 13, "Rusty", 12, 1548600000000L);
        check(lb.getBeatdowns(), false,
                "Rusty", 17, "DumbBot 1.0", 1, 1548599340000L,
                "Rusty", 17, "DumbBot 1.0", 1, 1548599880000L,
                "Rusty", 15, "DumbBot 1.0", 6, 1548598860000L,
                "OptiBot II", 13, "Rusty", 12, 1548600000000L);

        //  Now add an entry which isn't a high enough score to make that list,
        //  but which is enough of a beatdown to qualify for *that* list.
        checkAdd(lb, new Entry("Rusty", 12, "DumbBot 1.0", 1, 1548599880000L), -1, 2);
        check(lb.getHighScores(), false,
                "Rusty", 21, "DumbBot 1.0", 18, 1548599280000L,
                "Rusty", 21, "DumbBot 1.0", 18, 1548599820000L,
                "Rusty", 17, "DumbBot 1.0", 1, 1548599340000L,
                "OptiBot II", 13, "Rusty", 12, 1548600000000L);
        check(lb.getBeatdowns(), false,
                "Rusty", 17, "DumbBot 1.0", 1, 1548599340000L,
                "Rusty", 17, "DumbBot 1.0", 1, 1548599880000L,
                "Rusty", 12, "DumbBot 1.0", 1, 1548599880000L,
                "Rusty", 15, "DumbBot 1.0", 6, 1548598860000L);
        checkParcel(lb);
    }

    //  The HTML parts of this test probably breaks in some time zones, where
    //  the timestamps in the file fall on a different day.
    @Test
    public void testRepositoryAndHTML() throws Exception {
        Context context = TestUtil.mockContext();
        PowerMockito.when(context.getString(R.string.leader_high_scores_label)).thenReturn("High Scores");
        PowerMockito.when(context.getString(R.string.leader_beatdowns_label)).thenReturn("Worst Beatdowns");
        PowerMockito.when(context.getString(R.string.leader_solitaire_label)).thenReturn("Solitaire High Scores");
        PowerMockito.when(context.getString(R.string.leader_personal_label)).thenReturn("Personal Best");
        PowerMockito.mockStatic(DateFormat.class);
        PowerMockito.when(DateFormat.getDateFormat(context)).thenReturn(new SimpleDateFormat("yyyy-MM-dd"));
        //TestUtil.mockContextFileLog = System.err;

        LeaderboardRepository lr = new LeaderboardRepository(context, "leaderboard1.txt");
        Leaderboard lb = lr.load();
        check(lb.getHighScores(), false,
                "Rusty", 21, "DumbBot 1.0", 18, 1548599280000L,
                "Rusty", 21, "DumbBot 1.0", 18, 1548599820000L,
                "Rusty", 17, "DumbBot 1.0", 1, 1548599340000L,
                "Rusty", 17, "DumbBot 1.0", 1, 1548599880000L,
                "Rusty", 15, "DumbBot 1.0", 6, 1548598860000L,
                "Rusty", 15, "DumbBot 1.0", 6, 1548599760000L);
        check(lb.getBeatdowns(), false,
                "Rusty", 17, "DumbBot 1.0", 1, 1548599340000L,
                "Rusty", 17, "DumbBot 1.0", 1, 1548599880000L,
                "Rusty", 15, "DumbBot 1.0", 6, 1548598860000L,
                "Rusty", 15, "DumbBot 1.0", 6, 1548599760000L,
                "Rusty", 21, "DumbBot 1.0", 18, 1548599280000L,
                "Rusty", 21, "DumbBot 1.0", 18, 1548599820000L);
        check(lb.getSolitaireHighScores(), true,
                "DumbBot >1.0", 45, 1548598860000L,
                "Rusty & helper", 15, 1548599280000L);
        //  How's it look in HTML?
        Leaderboard.Highlight hl = null;
        assertEquals(TestUtil.snort("leaderboard1a.html", true, false, false),
                new LeaderboardHTML().toHTML(context, lb, hl));

        //  any old change
        hl = lb.add(new Entry("Rusty\t & \n helper", 15, 1548673260000L));
        assertNotNull(hl);

        lr.save(lb);
        TestUtil.compare("src/test/resources/leaderboard2.txt",
                "build/tmp/leaderboard1.txt", false, false);
        //  how about HTML
        assertEquals(TestUtil.snort("leaderboard2a.html", true, false, false),
                new LeaderboardHTML().toHTML(context, lb, null));
        //  this time with highlighted rows
        assertEquals(TestUtil.snort("leaderboard2b.html", true, false, false),
                new LeaderboardHTML().toHTML(context, lb, hl));

        //  another arbitrary change
        hl = lb.add(new Entry("Rusty", 45, "OptiBot II", 6, 1549006200000L));
        assertNotNull(hl);
        check(lb.getBeatdowns(), false,
                "Rusty", 45, "OptiBot II", 6, 1549006200000L,
                "Rusty", 17, "DumbBot 1.0", 1, 1548599340000L,
                "Rusty", 17, "DumbBot 1.0", 1, 1548599880000L,
                "Rusty", 15, "DumbBot 1.0", 6, 1548598860000L,
                "Rusty", 15, "DumbBot 1.0", 6, 1548599760000L,
                "Rusty", 21, "DumbBot 1.0", 18, 1548599280000L,
                "Rusty", 21, "DumbBot 1.0", 18, 1548599820000L);
        lr.save(lb);
        TestUtil.compare("src/test/resources/leaderboard3.txt",
                "build/tmp/leaderboard1.txt", false, false);
        assertEquals(TestUtil.snort("leaderboard3a.html", true, false, false),
                new LeaderboardHTML().toHTML(context, lb, null));
        assertEquals(TestUtil.snort("leaderboard3b.html", true, false, false),
                new LeaderboardHTML().toHTML(context, lb, hl));

        //  So far we've only done one read; let's try another.  This should
        //  read the build/tmp/leaderboard1.txt we just wrote, write it to
        //  build/tmp/leaderboard3.txt, and compare it to the checked-in
        //  src/test/resources/leaderboard3.txt.
        context = TestUtil.mockContext("build/tmp", "build/tmp", "build/tmp");
        lr = new LeaderboardRepository(context, "leaderboard1.txt");
        lb = lr.load();
        new LeaderboardRepository(context, "leaderboard3.txt").save(lb);
        TestUtil.compare("src/test/resources/leaderboard3.txt",
                "build/tmp/leaderboard3.txt", false, false);
    }

    //  for non-solitaire entries which are good enough to make the board
    //
    //  Note that, once personal best lists were added, this stopped being...
    //  complete; calls to add() which used to return null started returning a
    //  Highlight with personal-best rows indicated.  This doesn't check those.
    private void checkAdd(Leaderboard lb, Entry entry, int expectHighScoreRow,
                          int expectBeatdownRow) {
        assertFalse(entry.isSolitaire());
        Leaderboard.Highlight hl = lb.add(entry);
        assertNotNull(hl);
        assertEquals(-1, hl.getSolitaireHighScoreRow());
        assertEquals(expectHighScoreRow, hl.getHighScoreRow());
        assertEquals(expectBeatdownRow, hl.getBeatdownRow());
        //  and, as long as we're in here, let's test Parcel.
        //MockParcel.log = System.err;
        Leaderboard.Highlight hl2 = MockParcel.parcel(hl, Leaderboard.Highlight.CREATOR);
        assertEquals(hl, hl2);
    }

    //  for solitaire entries which are good enough to make the board
    private void checkAdd(Leaderboard lb, Entry entry, int expectSolitaireRow) {
        assertTrue(entry.isSolitaire());
        Leaderboard.Highlight hl = lb.add(entry);
        assertNotNull(hl);
        assertEquals(expectSolitaireRow, hl.getSolitaireHighScoreRow());
        assertEquals(-1, hl.getHighScoreRow());
        assertEquals(-1, hl.getBeatdownRow());
        //  and, as long as we're in here, let's test Parcel.
        //MockParcel.log = System.err;
        Leaderboard.Highlight hl2 = MockParcel.parcel(hl, Leaderboard.Highlight.CREATOR);
        assertEquals(hl, hl2);
    }

    /**
     * This is for confirming that the list contains the given entries in the
     * expected order.  "expect" should contain either three elements per
     * entry (name, score, date) or five elements per entry (name1, score1,
     * name2, score2, date) depending on whether we're looking at solitaire
     * scores.
     *
     * @param got
     * @param expectSolitaire
     * @param expect
     */
    static void check(List<Entry> got, boolean expectSolitaire, Object... expect) {
        //  uncomment to see what this is doing:
        //System.err.println("here's what's in the list:");
        //for (int gidx = 0; gidx < got.size(); ++gidx) {
        //    System.err.println("  " + gidx + ": " + got.get(gidx));
        //}
        //System.err.println("here's what we expect:");
        //for (int eidx = 0; eidx < expect.length; ) {
        //    String p1 = (String)(expect[eidx++]);
        //    int p1s = ((Integer)(expect[eidx++])).intValue();
        //    String p2 = expectSolitaire ? null : (String)(expect[eidx++]);
        //    int p2s = expectSolitaire ? 0 : ((Integer)(expect[eidx++])).intValue();
        //    long ed = (Long)(expect[eidx++]);
        //    System.err.println("  " + p1 + " " + p1s + " " +
        //            (expectSolitaire ? "" : (p2 + " " + p2s + " ")) + ed);
        //}

        int eidx = 0;
        for (int gidx = 0; gidx < got.size(); ++gidx) {
            Entry te = got.get(gidx);
            String ename = (String)(expect[eidx++]);
            Integer escore = (Integer)(expect[eidx++]);
            assertEquals("entry " + gidx + " name", ename, te.getP1Name());
            assertEquals("entry " + gidx + " score", escore.intValue(), te.getP1Score());
            assertEquals("entry " + gidx + " isSolitaire", expectSolitaire, te.isSolitaire());
            if (!expectSolitaire) {
                ename = (String)(expect[eidx++]);
                escore = (Integer)(expect[eidx++]);
                assertEquals("entry " + gidx + " p2 name", ename, te.getP2Name());
                assertEquals("entry " + gidx + " p2 score", escore.intValue(), te.getP2Score());
            }
            Long xdate = (Long)(expect[eidx++]);
            assertEquals("entry " + gidx + " date", xdate.longValue(), te.getDate());
        }
        assertTrue("there are fewer entries than we expected",
                (eidx == expect.length));
    }

    private void checkParcel(Leaderboard lb) {
        Leaderboard lb2 = MockParcel.parcel(lb, Leaderboard.CREATOR);
        assertEquals(lb.getLimit(), lb2.getLimit());
        assertListsEqual(lb.getSolitaireHighScores(), lb2.getSolitaireHighScores());
        assertListsEqual(lb.getHighScores(), lb2.getHighScores());
        assertListsEqual(lb.getBeatdowns(), lb2.getBeatdowns());
        assertListsEqual(new ArrayList<String>(lb.getPersonalBestNames()),
                new ArrayList<String>(lb2.getPersonalBestNames()));
        for (String name : lb.getPersonalBestNames()) {
            assertListsEqual(lb.getPersonalBest(name), lb2.getPersonalBest(name));
        }
    }
    //  I think I could've used Hamcrest for this.
    private <E> void assertListsEqual(List<E> expect, List<E> got) {
        assertEquals(expect.size(), got.size());
        for (int ii = 0; ii < expect.size(); ++ii) {
            assertEquals("element " + ii, expect.get(ii), got.get(ii));
        }
    }

}
