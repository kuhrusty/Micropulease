package com.kuhrusty.micropul.util;

import android.content.Context;

import com.kuhrusty.micropul.R;
import com.kuhrusty.micropul.util.Leaderboard.Entry;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Well, rather than screw with Android layout stuff, how about we just generate
 * HTML and let WebView lay it out.  Real dumb, but faster and easier for me.
 */
public class LeaderboardHTML {

    private String style =
            "table { width: 100%; }\n" +
            "tr:nth-child(odd) { background-color: #eeeeff; }\n" +
            "tr.fancy { background-color: #ffbbbb; }\n" +
            "td.fancy { color: #ff0000; }\n" +
            "td.bold { font-weight: bold; }\n" +
            "h1 { color: blue; text-align: center; }\n" +
            "h2 { color: blue; text-align: center; }\n";

    /**
     * Generates HTML for showing tables of the high scores.
     *
     * @param context for getting localized String resources; must not be null.
     * @param lb the scores to display; may be empty, but must not be null.
     * @param highlight which rows to highlight; may be null.
     */
    public String toHTML(Context context, Leaderboard lb,
                         Leaderboard.Highlight highlight) {

        //  simpler if we know this isn't null
        if (highlight == null) highlight = new Leaderboard.Highlight();

        DateFormat df = android.text.format.DateFormat.getDateFormat(context);
        StringBuilder buf = new StringBuilder();
        buf.append("<html><head><style>\n");
        buf.append(style);
        buf.append("</style></head><body>\n");

        if (lb.isEmpty()) {
            buf.append("<h1>").append(context.getString(R.string.leader_no_high_scores))
                    .append("</h1></body></html>\n");
            return buf.toString();
        }
        table(buf, context.getString(R.string.leader_high_scores_label),
                lb.getHighScores(), highlight.getHighScoreRow(), false, false, df);
        table(buf, context.getString(R.string.leader_beatdowns_label),
                lb.getBeatdowns(), highlight.getBeatdownRow(), true, false, df);
        table(buf, context.getString(R.string.leader_solitaire_label),
                lb.getSolitaireHighScores(), highlight.getSolitaireHighScoreRow(),
                false, false, df);

        Set<String> names = lb.getPersonalBestNames();
        if (!names.isEmpty()) {
            buf.append("<h1>")
                    .append(escape(context.getString(R.string.leader_personal_label)))
                    .append("</h1>\n");
            for (String name : names) {
                table(buf, name, lb.getPersonalBest(name),
                        highlight.getPersonalBestRow(name), false, true, df);
            }
        }

        buf.append("</body></html>\n");
        return buf.toString();
    }

    private String escape(String in) {
        //  not great, but it doesn't have to be
        in = in.replaceAll("\\&", "&amp;");
        in = in.replaceAll("<", "&lt;");
        in = in.replaceAll(">", "&gt;");
        return in;
    }

    private void table(StringBuilder buf, String title, List<Entry> rows,
                       int highlightRow, boolean isBeatdown, boolean isPersonal,
                       DateFormat df) {
        if (rows.size() == 0) return;
        String hh = isPersonal ? "h2" : "h1";
        buf.append("<" + hh + ">" + escape(title) + "</" + hh + ">\n");
        buf.append("<table>\n");
        int row = 0;
        for (Entry te : rows) {
            row(buf, te, isBeatdown, isPersonal, (highlightRow == row++), df);
        }
        buf.append("</table>\n\n");
    }

    private void row(StringBuilder buf, Entry entry, boolean isBeatdown,
                     boolean isPersonal, boolean highlight, DateFormat fmt) {
        buf.append(highlight ? "<tr class=\"fancy\">" : "<tr>");
        if (!isPersonal) {
            buf.append("<td class=\"" + (highlight ? "bold fancy" : "bold") + "\">");
            buf.append(escape(entry.getP1Name())).append("</td>");
        }
        buf.append("<td class=\"" + (highlight ? "bold fancy" : "bold") + "\">");
        buf.append(isBeatdown ? entry.getBeatdown() : entry.getP1Score()).append("</td>");
        if (!entry.isSolitaire()) {
            buf.append("<td>" + escape(entry.getP2Name()) + "</td>");
            buf.append("<td>" + (isBeatdown ? (entry.getP1Score() + " - " +
                    entry.getP2Score()) : entry.getP2Score()) + "</td>");
        }
        buf.append("<td>" + escape(fmt.format(new Date(entry.getDate()))) + "</td>");
        buf.append("</tr>\n");
    }
}
